package com.codeboy.mediafacerkotlin.musicSession

import android.app.Notification
import android.app.PendingIntent
import android.content.*
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import androidx.lifecycle.Observer
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import androidx.media3.ui.PlayerNotificationManager.NotificationListener
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.MainActivity
import com.codeboy.mediafacerkotlin.MediaFacerApp.NotificationSource.NOTIFICATION_ID
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.utils.MusicDataUtil
import com.codeboy.mediafacerkotlin.viewModels.AudioViewModel
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.QueueNavigator
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.FileNotFoundException
import java.io.InputStream

class MusicService : MediaBrowserServiceCompat(), OnAudioFocusChangeListener, Player.Listener{

    private val LOG_TAG = "MediaFacer Music"
    private lateinit var mAudioManager: AudioManager
    private lateinit var mMediaSessionCompat: MediaSessionCompat
    private lateinit var player: ExoPlayer
    private var musicList: ArrayList<AudioContent> = ArrayList()
    private var musicMetaDataList: ArrayList<MediaMetadataCompat> = ArrayList()
    private lateinit var currentTrack: MediaMetadataCompat

    //position of current music
    private var trackPosition = 0
    //the seeking position of the current track
    //save and get it from audio data util
    private var playbackPosition = 0L

    private val observer = Observer<ArrayList<AudioContent>> { it ->
        //Live data value has changed
        musicList = it
        //PlaybackProtocol.setCurrentMusic(musicList[0])
        setupUpMusicList(musicList)
    }
    private lateinit var playerNotification: PlayerNotificationManager

    private val mNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //handles playback when earphones are plugged and unplugged
            //pause exo player when unplugged and play when plugged
        }
    }

     override fun onCreate() {
         super.onCreate()
         //tell all app components that the service is running
         PlaybackProtocol.isMusicServiceRunning = true

         //setup audio manager and request audio focus
         mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager //register my audio focus
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN.and(AudioManager.STREAM_MUSIC))
                 .setOnAudioFocusChangeListener(this)
                 .build()
             // Request audio focus
             when (mAudioManager.requestAudioFocus(audioFocusRequest)) {
                 AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                     // Audio focus successfully granted
                 }
                 else -> {
                     // Audio focus request failed
                 }
             }
         } else {
             mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
         }

         //load the last playlist or new playlist if there is none
         PlaybackProtocol.musicList.observeForever(observer)

         initNoisyReceiver()
         setupMediaSession()
         setupExoPlayer()

     }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent)
        //return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return if (TextUtils.equals(clientPackageName, packageName)) {
            BrowserRoot(getString(R.string.app_name), null)
        } else null
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {

    }

    /*override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
        options: Bundle
    ) {
        super.onLoadChildren(parentId, result, options)
    }*/

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun  onDestroy() {
        playerNotification.setPlayer(null)
        //tell activity components that service is stopping
        PlaybackProtocol.isMusicServiceRunning = false
        //save the last playlist so next time it reads from preferences
        MusicDataUtil(this).saveLastPlaylist(musicList)
        //check if audioViewModel had observers on musicList and remove them
        if (PlaybackProtocol.musicList.hasActiveObservers()){
            PlaybackProtocol.musicList.removeObserver(observer)
        }
        player.removeListener(this)
        player.release()
        super.onDestroy()
    }

    //service setup methods______________________________________________________________________________________________________________________

    //build media items for media playback
    private fun setupUpMusicList(musicList: ArrayList<AudioContent>){
       /* val imageUri = Uri.parse("https://example.com/image.jpg")
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)*/
        for (musicItem in musicList){
            musicMetaDataList.add(musicItem.getMediaMetaDataCompat())
        }
        currentTrack = musicMetaDataList[trackPosition]
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupExoPlayer(){
        val mediaItems = ArrayList<MediaItem>()

        for (item: AudioContent in musicList){
            mediaItems.add(MediaItem.fromUri(item.musicUri))
        }
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters())
        }

        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                exoPlayer.addMediaItems(mediaItems)
                exoPlayer.seekTo(trackPosition, playbackPosition)
                exoPlayer.addListener(this)
                exoPlayer.prepare()
            }
        buildPlayerNotification()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupMediaSession(){
        val mediaButtonReceiverName = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        mMediaSessionCompat = MediaSessionCompat(applicationContext, LOG_TAG, mediaButtonReceiverName, null)
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(this,0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE.and(PendingIntent.FLAG_UPDATE_CURRENT))
        } else {
            PendingIntent.getBroadcast(this, 0, mediaButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent)
        mMediaSessionCompat.setPlaybackState(buildState(PlaybackStateCompat.STATE_NONE.toLong()))

        mMediaSessionCompat.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
            }

            override fun onPause() {
                super.onPause()
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                if(trackPosition == (musicMetaDataList.size - 1)){
                    trackPosition  = 0
                }else{
                    trackPosition  += 1
                }

                currentTrack = musicMetaDataList[trackPosition]
                mMediaSessionCompat.setMetadata(currentTrack)
                player.seekToNextMediaItem()
                PlaybackProtocol.setCurrentMusic(musicList[trackPosition])
                playerNotification.setPlayer(player)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                if(trackPosition == 0){
                    trackPosition  = (musicMetaDataList.size - 1)
                }else{
                    trackPosition  -= 1
                }

                currentTrack = musicMetaDataList[trackPosition]
                mMediaSessionCompat.setMetadata(currentTrack)
                player.seekToPreviousMediaItem()
                PlaybackProtocol.setCurrentMusic(musicList[trackPosition])
                playerNotification.setPlayer(player)
            }

            override fun onFastForward() {
                super.onFastForward()
            }

            override fun onRewind() {
                super.onRewind()
            }

            override fun onStop() {
                super.onStop()
            }

            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
            }

            
        })

        mMediaSessionCompat.setMetadata(currentTrack)
        mMediaSessionCompat.isActive = true
        sessionToken = mMediaSessionCompat.sessionToken
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun buildPlayerNotification(){
        val playerNotificationBuilder = PlayerNotificationManager.Builder(
            this,NOTIFICATION_ID,NOTIFICATION_ID.toString()
        ).setMediaDescriptionAdapter(object: MediaDescriptionAdapter{
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return musicList[trackPosition].title
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val playerIntent = Intent(this@MusicService, MainActivity::class.java)
                return PendingIntent.getActivity(this@MusicService, 0,playerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return mMediaSessionCompat.controller.metadata.description.description
            }

            override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                val imageUri: Uri = Uri.parse(musicList[trackPosition].artUri)
                var inputStream: InputStream? = null
                try {
                    inputStream = contentResolver.openInputStream(imageUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                return BitmapFactory.decodeStream(inputStream)
            }

        }).setNotificationListener(object: NotificationListener{
            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                stopSelf()
                super.onNotificationCancelled(notificationId, dismissedByUser)
            }

            override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID,notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                }else{
                    startForeground(NOTIFICATION_ID, notification)
                }
                super.onNotificationPosted(notificationId, notification, ongoing)
            }
        }).setSmallIconResourceId(R.drawable.logo)
            .setNextActionIconResourceId(R.drawable.ic_next)
            .setPreviousActionIconResourceId(R.drawable.ic_previous)
            .setPlayActionIconResourceId(R.drawable.ic_play)
            .setPauseActionIconResourceId(R.drawable.ic_pause)
            //.setStopActionIconResourceId(androidx.media3.ui.R.drawable.exo_icon_stop)

        playerNotification = playerNotificationBuilder.build()

        playerNotification.setUseStopAction(true)
        playerNotification.setUseFastForwardAction(false)
        playerNotification.setUseRewindAction(false)
        playerNotification.setUseNextActionInCompactView(true)
        playerNotification.setUsePreviousActionInCompactView(true)
        playerNotification.setUseChronometer(true)

        playerNotification.setPlayer(player)
        playerNotification.setMediaSessionToken(mMediaSessionCompat.sessionToken)



        /*val sessionConnector = MediaSessionConnector(mMediaSessionCompat)
        sessionConnector.setQueueNavigator(object: TimelineQueueNavigator() {
            override fun getMediaDescription(
                player: ExoPlayer,
                windowIndex: Int
            ): MediaDescriptionCompat {
                TODO("Not yet implemented")
            }

        })
        sessionConnector.setPlayer(player)*/

    }

    private fun buildState(state: Long): PlaybackStateCompat? {
        return PlaybackStateCompat.Builder().setActions(
            PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    or PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_REWIND or PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_FAST_FORWARD or PlaybackStateCompat.ACTION_PAUSE
        )
            .setState(
                state.toInt(),
                playbackPosition,
                0.5f,
                SystemClock.elapsedRealtime()
            ).build()
    }

    private fun initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(mNoisyReceiver, filter)
    }

    //___________________________________________________________________________________________________________________________________________

    //handling audio focus as music plays or is interfered
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
             /*   if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop()
                }*/
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                //mediaPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                /*if (mediaPlayer != null) {
                    mediaPlayer.setVolume(0.3f, 0.3f)
                }*/
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                /*if (mediaPlayer != null) {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start()
                    }
                    mediaPlayer.setVolume(1.0f, 1.0f)
                }*/
            }
        }
    }

    override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
        super.onSeekBackIncrementChanged(seekBackIncrementMs)
        playbackPosition = seekBackIncrementMs
    }

    override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
        super.onSeekForwardIncrementChanged(seekForwardIncrementMs)
        playbackPosition = seekForwardIncrementMs
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onPlaybackStateChanged(playbackState: Int) {
        when(playbackState){
            ExoPlayer.STATE_READY -> {
                player.playWhenReady = true
            }
            ExoPlayer.STATE_BUFFERING -> {
                //show a toast to tell user it buffering or unstable internet
            }
            ExoPlayer.STATE_ENDED -> {
                //playWhenReady = false
                trackPosition  += 1
                currentTrack = musicMetaDataList[trackPosition]
                mMediaSessionCompat.setMetadata(currentTrack)
                player.seekToNextMediaItem()
                PlaybackProtocol.setCurrentMusic(musicList[trackPosition])
                playerNotification.setPlayer(player)
            }
            ExoPlayer.STATE_IDLE -> {
                //here you can set items and prepare
            }
        }
    }


}