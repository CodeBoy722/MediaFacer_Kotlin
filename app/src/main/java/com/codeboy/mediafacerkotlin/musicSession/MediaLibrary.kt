package com.codeboy.mediafacerkotlin.musicSession

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.Observer
import androidx.media3.common.*
import androidx.media3.session.MediaSession
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
import androidx.core.net.toUri

@OptIn(UnstableApi::class)
class MediaLibrary : MediaLibraryService(), OnAudioFocusChangeListener, Player.Listener {

    private val librarySessionCallback = CustomMediaLibrarySessionCallback()
    private lateinit var player: ExoPlayer
    private lateinit var mediaLibrarySession: MediaLibrarySession
    private lateinit var playerNotification: PlayerNotificationManager
    private var customCommands: List<CommandButton> = emptyList()
    private var customLayout = ImmutableList.of<CommandButton>()
    private var trackPosition = 0
    private var playbackPosition = 0L
    private var musicList: ArrayList<AudioContent> = ArrayList()
    private var mediaItems: ArrayList<MediaItem> = ArrayList()
    private lateinit var currentTrack: MediaItem
    private var equalizer: Equalizer? = null

    private val observer = Observer<ArrayList<AudioContent>> { list ->
        musicList = list
        PlaybackProtocol.setMusicList(list)
        MusicDataUtil(this).saveLastPlaylist(list)
        setupUpMusicList(list, 0)
    }

    companion object {
        private const val NOTIFICATION_ID = 1010
        private const val CHANNEL_ID = "media_facer_channel"
        private var isRunning = false
        fun isStarted() = isRunning
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        MediaItemTree.mediaFacerInitializeMediaTree()

        initCustomCommands()
        initializeSessionAndPlayer()
        setListener(MediaSessionServiceListener())

        // Load last playlist or fetch new one
        musicList = MusicDataUtil(this).getLastPlaylist()
        if (musicList.isEmpty()) {
            val model = AudioViewModel()
            model.audios.observeForever(observer)
            model.loadNewItems(this, 0, 150, false)
        } else {
            PlaybackProtocol.setMusicList(musicList)
            setupUpMusicList(musicList, 0)
        }
    }

    private fun initCustomCommands() {
        customCommands = listOf(
            getShuffleCommandButton(true),
            getShuffleCommandButton(false),
            getRepeatModeCommandButton(Player.REPEAT_MODE_OFF),
            getRepeatModeCommandButton(Player.REPEAT_MODE_ONE),
            getRepeatModeCommandButton(Player.REPEAT_MODE_ALL),
            getNewPlaylistCommandButton()
        )
        // default layout: shuffle on + repeat off
        customLayout = ImmutableList.of(customCommands[0], customCommands[2])
    }

    private fun setupUpMusicList(list: ArrayList<AudioContent>, position: Int) {
        if (list.isEmpty()) return

        musicList = list
        mediaItems.clear()
        list.forEach { item ->
            mediaItems.add(
                MediaItem.Builder()
                    .setMediaId(item.musicId.toString())
                    .setMediaMetadata(item.getMediaMetadata())
                    .setUri(item.musicUri.toUri())
                    .build()
            )
        }

        trackPosition = position
        currentTrack = mediaItems[position]

        player.setMediaItems(mediaItems, position, 0L)
        player.prepare()
        player.playWhenReady = true
        PlaybackProtocol.updateCurrentMedia(currentTrack)
        PlaybackProtocol.updateMediaList(mediaItems)
    }

    private fun initializeSessionAndPlayer() {
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters())
        }

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .setTrackSelector(trackSelector)
            .build()
            .also {
                it.setHandleAudioBecomingNoisy(true)
                it.setWakeMode(C.WAKE_MODE_LOCAL)
                it.pauseAtEndOfMediaItems = false
                it.seekTo(trackPosition, playbackPosition)
                it.addListener(this)
            }

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                super.onAudioSessionIdChanged(audioSessionId)
                setupEqualizer(audioSessionId)
            }
        })

        val sessionActivity = TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@MediaLibrary, MainActivity::class.java))
            getPendingIntent(0, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        }

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, librarySessionCallback)
            .setSessionActivity(sessionActivity!!)
            .build()

        if (customLayout.isNotEmpty()) mediaLibrarySession.setCustomLayout(customLayout)

        setupNotification(mediaLibrarySession)
    }

    private fun setupNotification(session: MediaSession) {
        val builder = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, CHANNEL_ID)
            .setChannelNameResourceId(R.string.channel_name)
            .setChannelDescriptionResourceId(R.string.description)
            .setSmallIconResourceId(R.drawable.ic_logo_notiv)
            .setNextActionIconResourceId(R.drawable.ic_skip_next)
            .setPreviousActionIconResourceId(R.drawable.ic_skip_previous)
            .setPlayActionIconResourceId(R.drawable.ic_play)
            .setPauseActionIconResourceId(R.drawable.ic_pause)
            .setStopActionIconResourceId(R.drawable.ic_cancel)
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopForeground(true)
                    stopSelf()
                }

                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                    } else startForeground(notificationId, notification)
                }
            })

        playerNotification = builder.build()
        playerNotification.setUseStopAction(true)
        playerNotification.setUseFastForwardAction(false)
        playerNotification.setUseRewindAction(false)
        playerNotification.setUseNextActionInCompactView(true)
        playerNotification.setUsePreviousActionInCompactView(true)
        playerNotification.setUseChronometer(true)
        playerNotification.setPlayer(player)
        playerNotification.setMediaSessionToken(session.platformToken)
    }

    /** --- EQUALIZER --- */
    private fun setupEqualizer(audioSessionId: Int) {
        equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
        // Optional: adjust bands programmatically here
    }

    fun setEqualizerBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
    }

    fun releaseEqualizer() {
        equalizer?.release()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> if (player.isPlaying) player.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> player.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.volume = 0.3f
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!player.isPlaying) player.play()
                player.volume = 1.0f
            }
        }
    }

    override fun onDestroy() {
        MusicDataUtil(this).saveLastPlaylist(musicList)
        player.stop()
        player.release()
        releaseEqualizer()
        mediaLibrarySession.release()
        isRunning = false
        super.onDestroy()
    }

    // Additional Player.Listener overrides
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        mediaItem?.let {
            PlaybackProtocol.updateCurrentMedia(it)
            currentTrack = it
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        // Can handle buffering, idle, ended here
    }

    // Custom command handling
    private inner class CustomMediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            val commands = super.onConnect(session, controller).availableSessionCommands.buildUpon()
            customCommands.forEach { it.sessionCommand?.let(commands::add) }
            return MediaSession.ConnectionResult.accept(commands.build(), super.onConnect(session, controller).availablePlayerCommands)
        }

        override fun onCustomCommand(session: MediaSession, controller: MediaSession.ControllerInfo, customCommand: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                "SHUFFLE_ON" -> player.shuffleModeEnabled = true
                "SHUFFLE_OFF" -> player.shuffleModeEnabled = false
                "REPEAT_OFF" -> player.repeatMode = Player.REPEAT_MODE_OFF
                "REPEAT_ONE" -> player.repeatMode = Player.REPEAT_MODE_ONE
                "REPEAT_ALL" -> player.repeatMode = Player.REPEAT_MODE_ALL
                "NEW_PLAYLIST" -> {
                    val json = args.getString("track_list") ?: return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    val gson = Gson()
                    val newList = gson.fromJson<ArrayList<AudioContent>>(json, object : TypeToken<ArrayList<AudioContent>>(){}.type) ?: arrayListOf()
                    val position = args.getInt("track_position_to_play", 0)
                    CoroutineScope(Dispatchers.Main).launch {
                        player.stop()
                        mediaItems.clear()
                        setupUpMusicList(newList, position)
                        player.play()
                    }
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onGetLibraryRoot(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, params: LibraryParams?) =
            Futures.immediateFuture(LibraryResult.ofItem(MediaItemTree.getRootItem(), params))
    }

    /** --- Command Buttons --- */
    private fun getShuffleCommandButton(isOn: Boolean): CommandButton {
        val action = if (isOn) "SHUFFLE_ON" else "SHUFFLE_OFF"
        return CommandButton.Builder()
            .setDisplayName(if (isOn) "Shuffle On" else "Shuffle Off")
            .setSessionCommand(SessionCommand(action, Bundle.EMPTY))
            .build()
    }

    private fun getRepeatModeCommandButton(mode: Int): CommandButton {
        val action = when (mode) {
            Player.REPEAT_MODE_ONE -> "REPEAT_ONE"
            Player.REPEAT_MODE_ALL -> "REPEAT_ALL"
            else -> "REPEAT_OFF"
        }
        return CommandButton.Builder()
            .setDisplayName("Repeat Mode")
            .setSessionCommand(SessionCommand(action, Bundle.EMPTY))
            .build()
    }

    private fun getNewPlaylistCommandButton(): CommandButton =
        CommandButton.Builder().setDisplayName("New Playlist")
            .setSessionCommand(SessionCommand("NEW_PLAYLIST", Bundle.EMPTY)).build()

    /** --- Foreground service listener for Android 12+ --- */
    private inner class MediaSessionServiceListener : Listener {
        override fun onForegroundServiceStartNotAllowedException() {
            val notificationManager = NotificationManagerCompat.from(this@MediaLibrary)
            ensureNotificationChannel(notificationManager)
        }
    }

    private fun ensureNotificationChannel(notificationManager: NotificationManagerCompat) {
        if (Util.SDK_INT < 26 || notificationManager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(CHANNEL_ID, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady) {
            stopSelf()
        }
    }
}

