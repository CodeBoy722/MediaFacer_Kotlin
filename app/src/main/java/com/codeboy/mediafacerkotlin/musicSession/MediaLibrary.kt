package com.codeboy.mediafacerkotlin.musicSession

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.Observer
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.*
import androidx.media3.ui.PlayerNotificationManager
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.utils.MusicDataUtil
import com.codeboy.mediafacerkotlin.viewModels.AudioViewModel
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaLibrary : MediaLibraryService(), Player.Listener {

    private val librarySessionCallback = CustomMediaLibrarySessionCallback()
    private lateinit var player: ExoPlayer
    private lateinit var mediaLibrarySession: MediaLibrarySession
    private lateinit var playerNotification: PlayerNotificationManager
    private lateinit var customCommands: List<CommandButton>
    private var customLayout = ImmutableList.of<CommandButton>()

    private var trackPosition = 0
    private var playbackPosition = 0L // for seekbar if available
    private var musicList: ArrayList<AudioContent> = ArrayList()
    private var mediaItems = ArrayList<MediaItem>()
    private lateinit var currentTrack: MediaItem

    private val observer = Observer<ArrayList<AudioContent>> { it ->
        //Live data value has changed
        musicList = it
        PlaybackProtocol.setMusicList(musicList)
        MusicDataUtil(this).saveLastPlaylist(musicList)
        setupUpMusicList(musicList, 0)
    }

    companion object {
        private const val SEARCH_QUERY_PREFIX_COMPAT = "androidx://media3-session/playFromSearch"
        private const val SEARCH_QUERY_PREFIX = "androidx://media3-session/setMediaUri"
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON = "android.media3.session.demo.SHUFFLE_ON"
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF = "android.media3.session.demo.SHUFFLE_OFF"
        private const val CUSTOM_COMMAND_REPEAT_OFF = "codeboy.session.repeat.off"
        private const val CUSTOM_COMMAND_REPEAT_ONCE = "codeboy.session.repeat.once"
        private const val CUSTOM_COMMAND_REPEAT_ALL = "codeboy.session.repeat.all"

        private lateinit var currentShuffle: CommandButton
        private lateinit var currentRepeat: CommandButton

        private const val CUSTOM_COMMAND_NEW_PLAYLIST = "mediafacer.action.newPlaylist"
        private const val CUSTOM_COMMAND_STOP_PLAYER = "mediafacer.action.stop"
        private const val NOTIFICATION_ID = 1010
        private const val CHANNEL_ID = "media_facer_channel"
        private var isRunning = false
        fun isStarted(): Boolean {
            return isRunning
        }
    }



    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        MediaItemTree.mediaFacerInitializeMediaTree()
        customCommands =
            listOf(
                getShuffleCommandButton(
                    SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON, Bundle.EMPTY)
                ),
                getShuffleCommandButton(
                    SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF, Bundle.EMPTY)
                ),
                getRepeatModeCommandButton(CUSTOM_COMMAND_REPEAT_ALL),
                getRepeatModeCommandButton(CUSTOM_COMMAND_REPEAT_ONCE),
                getRepeatModeCommandButton(CUSTOM_COMMAND_REPEAT_OFF),
                getNewPlaylistCommandButtons(),

                //getStopCommandButton()
            )
        currentShuffle = customCommands[0]
        currentRepeat = customCommands[2]
        customLayout = ImmutableList.of(customCommands[0],customCommands[2])
        initializeSessionAndPlayer()
        setListener(MediaSessionServiceListener())

        //load the last playlist or new playlist if there is none
        musicList = ArrayList<AudioContent>()
        musicList = MusicDataUtil(this).getLastPlaylist()
        if(musicList.isEmpty()){
            val model = AudioViewModel()
            model.audios.observeForever(observer)
            model.loadNewItems(this,0,150,false)
        }else{
            PlaybackProtocol.setMusicList(musicList)
            setupUpMusicList(musicList, 0)
        }

    }

    @OptIn(UnstableApi::class)
    private fun setupUpMusicList(musicListNew: ArrayList<AudioContent>, position: Int){
        // todo fix empty media error here
        // todo and make empty media views in activity
        if(musicListNew.isNotEmpty()){
            musicList = musicListNew
            for (musicItem in musicList){
                mediaItems.add(
                    MediaItem.Builder()
                    .setMediaId(musicItem.musicId.toString())
                    .setMediaMetadata(musicItem.getMediaMetadata())
                    .setUri(Uri.parse(musicItem.musicUri))
                    .build()
                )
            }

            trackPosition = position
            PlaybackProtocol.updateCurrentMedia(mediaItems[position])
            PlaybackProtocol.updateMediaList(mediaItems)
            //MediaItemTree.mediaFacerInitializeWithContent(musicList)
            currentTrack = mediaItems[position]
            player.addMediaItems(mediaItems)
            player.seekTo(position,0)
            player.prepare()
        }//else the is no music on device so nothing will be set
    }

     @OptIn(UnstableApi::class)
    override fun onUpdateNotification(session: MediaSession) {
        val playerNotificationBuilder = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, CHANNEL_ID)
            .setChannelNameResourceId(R.string.channel_name)
            .setChannelDescriptionResourceId(R.string.description)

            .setNotificationListener(object: PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    super.onNotificationCancelled(notificationId, dismissedByUser)
                    //remove notification and stop service totally
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(MediaLibraryService.STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }else{
                        stopForeground(true)
                        stopSelf()
                    }
                }

                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    super.onNotificationPosted(notificationId, notification, ongoing)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(notificationId,notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                    }else{
                        startForeground(notificationId, notification)
                    }
                }
            })
            .setSmallIconResourceId(R.drawable.ic_logo_notiv)
            .setNextActionIconResourceId(R.drawable.ic_skip_next)
            .setPreviousActionIconResourceId(R.drawable.ic_skip_previous)
            .setPlayActionIconResourceId(R.drawable.ic_play)
            .setPauseActionIconResourceId(R.drawable.ic_pause)
            .setStopActionIconResourceId(R.drawable.ic_cancel)
         //this customs are not needed
           /* .setCustomActionReceiver(object: PlayerNotificationManager.CustomActionReceiver{
                override fun createCustomActions(context: Context, instanceId: Int): MutableMap<String, NotificationCompat.Action> {
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mutableMapOf(Pair(
                            CUSTOM_COMMAND_STOP_PLAYER,
                            NotificationCompat.Action(R.drawable.ic_cancel, "Stop Player",
                                PendingIntent.getBroadcast(context, 300, Intent(
                                    CUSTOM_COMMAND_STOP_PLAYER).setPackage(context.packageName), PendingIntent.FLAG_IMMUTABLE)//.and(PendingIntent.FLAG_UPDATE_CURRENT)
                            )
                        ))
                    } else {
                        mutableMapOf(Pair(
                            CUSTOM_COMMAND_STOP_PLAYER,
                            NotificationCompat.Action(R.drawable.ic_cancel, "Stop Player",
                                PendingIntent.getBroadcast(context, 300, Intent(
                                    CUSTOM_COMMAND_STOP_PLAYER).setPackage(context.packageName),PendingIntent.FLAG_IMMUTABLE )//PendingIntent.FLAG_UPDATE_CURRENT.and(PendingIntent.FLAG_IMMUTABLE)
                            )
                        ))
                    }
                }

                override fun getCustomActions(player: Player): MutableList<String> {
                    return mutableListOf(CUSTOM_COMMAND_STOP_PLAYER)
                }

                override fun onCustomAction(player: Player, action: String, intent: Intent) {
                    when (action) {
                        CUSTOM_COMMAND_STOP_PLAYER -> {
                            stopSelf()
                        }
                    }
                }
            })*/

        playerNotification = playerNotificationBuilder.build()
        playerNotification.setUseStopAction(true)
        playerNotification.setUseFastForwardAction(false)
        playerNotification.setUseRewindAction(false)
        playerNotification.setUseNextActionInCompactView(true)
        playerNotification.setUsePreviousActionInCompactView(true)
        playerNotification.setUseChronometer(true)
        playerNotification.setPlayer(player)
        playerNotification.setMediaSessionToken(session.sessionCompatToken)

    }

    @OptIn(UnstableApi::class)
    private fun initializeSessionAndPlayer() {
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters())
        }

        player =
            ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
                .setTrackSelector(trackSelector)
                .build()
                .also {
                    it.setAudioAttributes(AudioAttributes.DEFAULT, true)
                    it.setHandleAudioBecomingNoisy(true)
                    it.setWakeMode(C.WAKE_MODE_LOCAL)
                    //it.addMediaItems(mediaItems)
                    //it.repeatMode = Player.REPEAT_MODE_ALL
                    //it.shuffleModeEnabled = true
                    it.pauseAtEndOfMediaItems = false
                    it.seekTo(trackPosition, playbackPosition)
                    it.addListener(this@MediaLibrary)
                    //it.prepare()
                }

        val sessionActivityPendingIntent =
            TaskStackBuilder.create(this).run {
                addNextIntent(Intent(this@MediaLibrary, MainActivity::class.java))
                //addNextIntent(Intent(this@MediaLibrary, PlayerActivity::class.java))

                val immutableFlag = if (Build.VERSION.SDK_INT >= 23) FLAG_IMMUTABLE else 0
                getPendingIntent(0, immutableFlag or FLAG_UPDATE_CURRENT)
            }

        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, librarySessionCallback)
                .setSessionActivity(sessionActivityPendingIntent!!)
                .build()
        if (!customLayout.isEmpty()) {
            // Send custom layout to legacy session.
            mediaLibrarySession.setCustomLayout(customLayout)
        }
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
     /*   if (!player.playWhenReady) {
            stopSelf()
        }*/
    }


    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        MusicDataUtil(this).saveLastPlaylist(musicList)
        player.stop()
        player.release()
        mediaLibrarySession.release()
        clearListener()
        isRunning = false
        super.onDestroy()
    }

    private inner class CustomMediaLibrarySessionCallback : MediaLibrarySession.Callback {

        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
            customCommands.forEach { commandButton ->
                // Add custom command to available session commands.
                commandButton.sessionCommand?.let { availableSessionCommands.add(it) }
            }
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(),
                connectionResult.availablePlayerCommands
            )
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            if (!customLayout.isEmpty() && controller.controllerVersion != 0) {
                // Let Media3 controller (for instance the MediaNotificationProvider) know about the custom
                // layout right after it connected.
                ignoreFuture(mediaLibrarySession.setCustomLayout(controller, customLayout))
            }
        }

        // handle my custom playback actions here
        override fun onCustomCommand(session: MediaSession, controller: MediaSession.ControllerInfo, customCommand: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON -> {
                    // Enable shuffling.
                    player.shuffleModeEnabled = true
                    // Change the custom layout to contain the `Disable shuffling` command.
                    currentShuffle = customCommands[1]
                    customLayout = ImmutableList.of(customCommands[1], currentRepeat)
                    // Send the updated custom layout to controllers.
                    session.setCustomLayout(customLayout)
                }

                CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF -> {
                    // Disable shuffling.
                    player.shuffleModeEnabled = false
                    // Change the custom layout to contain the `Enable shuffling` command.
                    currentShuffle = customCommands[0]
                    customLayout = ImmutableList.of(customCommands[0], currentRepeat)
                    // Send the updated custom layout to controllers.
                    session.setCustomLayout(customLayout)
                }

                CUSTOM_COMMAND_REPEAT_ALL -> {
                    player.repeatMode = Player.REPEAT_MODE_ALL
                    currentRepeat = customCommands[3]
                    customLayout = ImmutableList.of(currentShuffle,customCommands[3])
                    session.setCustomLayout(customLayout)
                }

                CUSTOM_COMMAND_REPEAT_ONCE -> {
                    player.repeatMode = Player.REPEAT_MODE_ONE
                    currentRepeat = customCommands[4]
                    customLayout = ImmutableList.of(currentShuffle,customCommands[4])
                    session.setCustomLayout(customLayout)
                }

                CUSTOM_COMMAND_REPEAT_OFF -> {
                    player.repeatMode = Player.REPEAT_MODE_OFF
                    currentRepeat = customCommands[2]
                    customLayout = ImmutableList.of(currentShuffle,customCommands[2])
                    session.setCustomLayout(customLayout)
                }

                CUSTOM_COMMAND_NEW_PLAYLIST -> {

                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.Main){

                            val position =  args.getInt("track_position_to_play")
                            var serializedPlaylist: ArrayList<AudioContent> = ArrayList()
                            val gson = Gson()
                            val json: String? = args.getString("track_list");
                            gson.fromJson<ArrayList<AudioContent>>(json, object : TypeToken<ArrayList<AudioContent>>(){}.type)
                                .also {
                                    serializedPlaylist = it?: ArrayList()
                                    player.stop()// stop the player and put in idle state
                                    player.removeMediaItems(0,mediaItems.size)// remove all mediaItems from player
                                    mediaItems = ArrayList()// empty the mediaItems list
                                    setupUpMusicList(serializedPlaylist, position)// set up the new playlist
                                    player.play()// tell the player to play when item is ready
                                }
                        }
                    }

                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onGetLibraryRoot(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, params: LibraryParams?): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofItem(MediaItemTree.getRootItem(), params))
        }

        override fun onGetItem(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, mediaId: String): ListenableFuture<LibraryResult<MediaItem>> {
            val item =
                MediaItemTree.getItem(mediaId)
                    ?: return Futures.immediateFuture(
                        LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                    )
            return Futures.immediateFuture(LibraryResult.ofItem(item, /* params= */ null))
        }

        override fun onSubscribe(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, parentId: String, params: LibraryParams?): ListenableFuture<LibraryResult<Void>> {
            val children =
                MediaItemTree.getChildren(parentId)
                    ?: return Futures.immediateFuture(
                        LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                    )
            session.notifyChildrenChanged(browser, parentId, children.size, params)
            return Futures.immediateFuture(LibraryResult.ofVoid())
        }

        override fun onGetChildren(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, parentId: String, page: Int, pageSize: Int, params: LibraryParams?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val children =
                MediaItemTree.getChildren(parentId)
                    ?: return Futures.immediateFuture(
                        LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                    )

            return Futures.immediateFuture(LibraryResult.ofItemList(children, params))
        }

        override fun onAddMediaItems(mediaSession: MediaSession, controller: MediaSession.ControllerInfo, mediaItems: List<MediaItem>): ListenableFuture<List<MediaItem>> {
            val updatedMediaItems: List<MediaItem> =
                mediaItems.map { mediaItem ->
                    if (mediaItem.requestMetadata.searchQuery != null)
                        getMediaItemFromSearchQuery(mediaItem.requestMetadata.searchQuery!!)
                    else MediaItemTree.getItem(mediaItem.mediaId) ?: mediaItem
                }
            return Futures.immediateFuture(updatedMediaItems)
        }

        private fun getMediaItemFromSearchQuery(query: String): MediaItem {
            // Only accept query with pattern "play [Title]" or "[Title]"
            // Where [Title]: must be exactly matched
            // If no media with exact name found, play a random media instead
            val mediaTitle =
                if (query.startsWith("play ", ignoreCase = true)) {
                    query.drop(5)
                } else {
                    query
                }

            return MediaItemTree.getItemFromTitle(mediaTitle) ?: MediaItemTree.getRandomItem()
        }
    }


    //add shuffling to media notification
    private fun getShuffleCommandButton(sessionCommand: SessionCommand): CommandButton {
        val isOn = sessionCommand.customAction == CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON
        return CommandButton.Builder()
            .setDisplayName(
                getString(
                    if (isOn) R.string.exo_controls_shuffle_on_description
                    else R.string.exo_controls_shuffle_off_description
                )
            )
            .setSessionCommand(sessionCommand)
            .setIconResId(if (isOn) R.drawable.exo_icon_shuffle_off else R.drawable.exo_icon_shuffle_on)
            .build()
    }

    //add repeat mode to media notification
    @SuppressLint("PrivateResource")
    private fun getRepeatModeCommandButton(sessionCommand: String) : CommandButton {
        return CommandButton.Builder()
            .setDisplayName(
                getString(
                    when (sessionCommand) {
                        CUSTOM_COMMAND_REPEAT_ALL -> R.string.codeboy_controls_repeat_all_description
                        CUSTOM_COMMAND_REPEAT_ONCE -> R.string.codeboy_controls_repeat_once_description
                        else -> R.string.codeboy_controls_repeat_off_description
                    }
                )
            )
            .setSessionCommand(SessionCommand(sessionCommand, Bundle.EMPTY))
            .setIconResId(
                when (sessionCommand) {
                    //CUSTOM_COMMAND_REPEAT_ALL -> androidx.media3.ui.R.drawable.exo_icon_repeat_off
                    CUSTOM_COMMAND_REPEAT_ONCE -> androidx.media3.ui.R.drawable.exo_icon_repeat_all
                    CUSTOM_COMMAND_REPEAT_OFF -> androidx.media3.ui.R.drawable.exo_icon_repeat_one
                    else -> androidx.media3.ui.R.drawable.exo_icon_repeat_off
                }
            )
            .build()
    }

    private fun getNewPlaylistCommandButtons(): CommandButton{
        return CommandButton.Builder()
            .setDisplayName("new playlist")
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_NEW_PLAYLIST, Bundle.EMPTY))
            //.setIconResId(R.drawable.ic_logo_notiv)
            .build()
    }

    //not really needed as command button since media notification already has it
    private fun getStopCommandButton(): CommandButton{
        return CommandButton.Builder()
            .setDisplayName("Stop Player")
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_STOP_PLAYER, Bundle.EMPTY))
            .setIconResId(R.drawable.ic_cancel)
            .build()
    }


    private fun ignoreFuture(customLayout: ListenableFuture<SessionResult>) {
        /* Do nothing. */
    }

    @UnstableApi private inner class MediaSessionServiceListener : Listener {

        /**
         * This method is only required to be implemented on Android 12 or above when an attempt is made
         * by a media controller to resume playback when the {@link MediaSessionService} is in the
         * background.
         */
        override fun onForegroundServiceStartNotAllowedException() {
            val notificationManagerCompat = NotificationManagerCompat.from(this@MediaLibrary)
            ensureNotificationChannel(notificationManagerCompat)
            val pendingIntent =
                TaskStackBuilder.create(this@MediaLibrary).run {
                    addNextIntent(Intent(this@MediaLibrary, MainActivity::class.java))

                    val immutableFlag = if (Build.VERSION.SDK_INT >= 23) FLAG_IMMUTABLE else 0
                    getPendingIntent(0, immutableFlag or FLAG_UPDATE_CURRENT)
                }
            val builder =
                NotificationCompat.Builder(this@MediaLibrary, CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_logo_notiv)
                    .setContentTitle(getString(R.string.notification_content_title))//getString(R.string.notification_content_title
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_content_text))
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
            if (ActivityCompat.checkSelfPermission(
                    this@MediaLibrary,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
        }

    }

    @OptIn(UnstableApi::class)
    private fun  ensureNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
        if (Util.SDK_INT < 26 || notificationManagerCompat.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        notificationManagerCompat.createNotificationChannel(channel)
    }

     override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
         super.onSeekBackIncrementChanged(seekBackIncrementMs)
         playbackPosition = seekBackIncrementMs
     }

     override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
         super.onSeekForwardIncrementChanged(seekForwardIncrementMs)
         playbackPosition = seekForwardIncrementMs
     }

     //tell the playback protocol witch item is playing
     override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
         super.onMediaItemTransition(mediaItem, reason)
         if(mediaItem != null){
             PlaybackProtocol.updateCurrentMedia(mediaItem)
             currentTrack = mediaItem
         }
     }

     override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
         super.onPlaybackParametersChanged(playbackParameters)
     }

     @OptIn(UnstableApi::class)
    override fun onPlaybackStateChanged(playbackState: Int) {
        when(playbackState){
            ExoPlayer.STATE_READY -> {
                //player.playWhenReady = true
            }
            ExoPlayer.STATE_BUFFERING -> {
                //show a toast to tell user it buffering or unstable internet
            }
            ExoPlayer.STATE_ENDED -> {

            }
            ExoPlayer.STATE_IDLE -> {
                //here you can set items and prepare
            }
        }
    }

}