package com.codeboy.mediafacerkotlin.musicSession

import android.app.PendingIntent
import android.content.*
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
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
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.R
import com.codeboy.mediafacerkotlin.utils.MusicDataUtil
import com.codeboy.mediafacerkotlin.viewModels.AudioViewModel

class MusicService : MediaBrowserServiceCompat(), OnAudioFocusChangeListener, Player.Listener{

    private val LOG_TAG = "MediaFacer Music"
    private lateinit var mAudioManager: AudioManager
    private lateinit var mMediaSessionCompat: MediaSessionCompat
    private lateinit var player: ExoPlayer
    private var musicList: ArrayList<AudioContent> = ArrayList()
    private var musicMetaDataList: ArrayList<MediaMetadataCompat> = ArrayList()

    private val model = AudioViewModel()
    private val observer = Observer<ArrayList<AudioContent>> { it ->
        //Live data value has changed
        musicList = it
        PlaybackProtocol.setCurrentMusic(musicList[0])
        setupUpMusicList(musicList)
    }

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
         musicList = MusicDataUtil(this).getLastPlaylist()
         if(musicList.isEmpty()){
             //load new musicList from MediaFacer
             model.audios.observeForever(observer)
             model.loadNewItems(this,0,150,false)
         }else{
             PlaybackProtocol.setCurrentMusic(musicList[0])
             setupUpMusicList(musicList)
         }

         initNoisyReceiver()
         setupExoPlayer()
         setupMediaSession()
     }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    /*override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }*/

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return if (TextUtils.equals(clientPackageName, packageName)) {
            BrowserRoot(getString(R.string.app_name), null)
        } else null
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {

    }

    override fun onDestroy() {
        //tell activity components that service is stopping
        PlaybackProtocol.isMusicServiceRunning = false
        //save the last playlist so next time it reads from preferences
        MusicDataUtil(this).saveLastPlaylist(musicList)
        //check if audioViewModel had observers on musicList and remove them
        if (model.audios.hasActiveObservers()){
            model.audios.removeObserver(observer)
        }
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
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupExoPlayer(){
        val mediaItems = ArrayList<MediaItem>()

        for (item: AudioContent in musicList){
            mediaItems.add(MediaItem.fromUri(item.musicUri))
        }
        val trackSelector = DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        val currentItem = 0
        val playbackPosition = 0L

        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                exoPlayer.addMediaItems(mediaItems)
                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.addListener(this)
                exoPlayer.prepare()
            }
    }

    private fun setupMediaSession(){
        val mediaButtonReceiverName = ComponentName(applicationContext, MusicPlaybackButtonReceiver::class.java)
        mMediaSessionCompat = MediaSessionCompat(applicationContext, LOG_TAG, mediaButtonReceiverName, null)
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        //mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        mediaButtonIntent.setClass(this, MusicPlaybackButtonReceiver::class.java)

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
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
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

        mMediaSessionCompat.isActive = true
        sessionToken = mMediaSessionCompat.sessionToken

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
                player.currentPosition,
                1f,
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
            }
            ExoPlayer.STATE_IDLE -> {
                //here you can set items and prepare
            }
        }

    }





}