package com.codeboy.mediafacerkotlin.musicSession

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
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
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.PlayerActivity
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.musicSession.PlaybackProtocol.musicList

class MediaLibrary : MediaLibraryService(), Player.Listener {

    private val librarySessionCallback = CustomMediaLibrarySessionCallback()

    private lateinit var player: ExoPlayer
    private lateinit var mediaLibrarySession: MediaLibrarySession
    private lateinit var customCommands: List<CommandButton>

    private var customLayout = ImmutableList.of<CommandButton>()

    private var trackPosition = 0
    private var playbackPosition = 0L
    private var musicList: ArrayList<AudioContent> = ArrayList()
    //private var musicMetaDataList: ArrayList<MediaMetadataCompat> = ArrayList()
    private var mediaItems = ArrayList<MediaItem>()
    private lateinit var currentTrack: MediaMetadata

    private val observer = Observer<ArrayList<AudioContent>> { it ->
        //Live data value has changed
        musicList = it
        //PlaybackProtocol.setCurrentMusic(musicList[0])
        setupUpMusicList(musicList)
    }


    //playback custom commands
    companion object {
        private const val SEARCH_QUERY_PREFIX_COMPAT = "androidx://media3-session/playFromSearch"
        private const val SEARCH_QUERY_PREFIX = "androidx://media3-session/setMediaUri"
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON = "android.media3.session.demo.SHUFFLE_ON"
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF = "android.media3.session.demo.SHUFFLE_OFF"

        private const val CUSTOM_COMMAND_PLAY = "mediafacer.action.play"
        private const val CUSTOM_COMMAND_PAUSE = "mediafacer.action.pause"
        private const val CUSTOM_COMMAND_NEXT = "mediafacer.action.next"
        private const val CUSTOM_COMMAND_PREVIOUS = "mediafacer.action.previous"
        private const val CUSTOM_COMMAND_STOP = "mediafacer.action.stop"
        private const val NOTIFICATION_ID = 1010
        private const val CHANNEL_ID = "media_facer_channel"
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
       /* customCommands =
            listOf(
                getShuffleCommandButton(
                    SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON, Bundle.EMPTY)
                ),
                getShuffleCommandButton(
                    SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF, Bundle.EMPTY)
                ),
            )*/
        customCommands = modifyPlayerCommandButtons()
        //customLayout = ImmutableList.of(customCommands[0])
        customLayout = ImmutableList.copyOf(customCommands)

        //load the last playlist or new playlist if there is none
        PlaybackProtocol.musicList.observeForever(observer)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady) {
            stopSelf()
        }
    }


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onDestroy() {
        player.release()
        mediaLibrarySession.release()
        clearListener()
        super.onDestroy()
    }

    //not used to init librarySessionCallback
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
            if (CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON == customCommand.customAction) {
                // Enable shuffling.
                player.shuffleModeEnabled = true
                // Change the custom layout to contain the `Disable shuffling` command.
                customLayout = ImmutableList.of(customCommands[1])
                // Send the updated custom layout to controllers.
                session.setCustomLayout(customLayout)
            } else if (CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF == customCommand.customAction) {
                // Disable shuffling.
                player.shuffleModeEnabled = false
                // Change the custom layout to contain the `Enable shuffling` command.
                customLayout = ImmutableList.of(customCommands[0])
                // Send the updated custom layout to controllers.
                session.setCustomLayout(customLayout)
            }else if(CUSTOM_COMMAND_STOP == customCommand.customAction){
                stopSelf()
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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupUpMusicList(musicList: ArrayList<AudioContent>){
        for (musicItem in musicList){
            mediaItems.add(MediaItem.fromUri(musicItem.musicUri))
        }
        currentTrack = mediaItems[trackPosition].mediaMetadata

        initializeSessionAndPlayer()
        setListener(MediaSessionServiceListener())
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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
                    it.addMediaItems(mediaItems)
                    it.pauseAtEndOfMediaItems = false
                    it.seekTo(trackPosition, playbackPosition)
                    it.addListener(this@MediaLibrary)
                    it.prepare()
                }
        MediaItemTree.initialize(assets)

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

    private fun modifyPlayerCommandButtons(): List<CommandButton>{

        val previous = CommandButton.Builder()
            .setDisplayName("Play previous song")
            .setIconResId(R.drawable.ic_skip_previous)
            .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .setEnabled(true)
            .build()

        val playPauseButton = CommandButton.Builder()
            .setDisplayName(if(::player.isInitialized && player.isPlaying) "Pause" else "Play")
            .setIconResId(if(::player.isInitialized && player.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
            .setEnabled(true)
            .build()

        val next = CommandButton.Builder()
            .setDisplayName("Play next song")
            .setIconResId(R.drawable.ic_skip_next)
            .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .setEnabled(true)
            .build()

        val cancel = CommandButton.Builder()
            .setDisplayName("stop playback")
            .setSessionCommand(SessionCommand(CUSTOM_COMMAND_STOP, Bundle.EMPTY))
            .setIconResId(R.drawable.ic_cancel)
            .build()

        return listOf(previous, playPauseButton, next, cancel)

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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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