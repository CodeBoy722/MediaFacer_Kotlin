package com.codeboy.mediafacerkotlin.musicSession

import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.Observer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.codeboy.mediafacer.models.AudioContent

class MusicSessionService : MediaSessionService(), AudioManager.OnAudioFocusChangeListener, Player.Listener {

    private lateinit var mediaSession: MediaSession
    private lateinit var mAudioManager: AudioManager
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

    override fun onCreate() {
        super.onCreate()
        //tell all app components that the service is running
        PlaybackProtocol.isMusicServiceRunning = true

        //setup audio manager and request audio focus
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager //register my audio focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioFocusRequest = AudioFocusRequest.Builder(
                AudioManager.AUDIOFOCUS_GAIN.and(
                    AudioManager.STREAM_MUSIC))
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
        setupPlayerSession()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
      return mediaSession
    }

    private fun setupUpMusicList(musicList: ArrayList<AudioContent>){
        /* val imageUri = Uri.parse("https://example.com/image.jpg")
         val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)*/
        for (musicItem in musicList){
            musicMetaDataList.add(musicItem.getMediaMetaDataCompat())
        }
        currentTrack = musicMetaDataList[trackPosition]
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupPlayerSession(){
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
                exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
                exoPlayer.setHandleAudioBecomingNoisy(true)
                exoPlayer.setWakeMode(C.WAKE_MODE_LOCAL)
                exoPlayer.addMediaItems(mediaItems)
                exoPlayer.pauseAtEndOfMediaItems = true
                exoPlayer.seekTo(trackPosition, playbackPosition)
                exoPlayer.addListener(this)
                exoPlayer.prepare()
            }

        mediaSession = MediaSession.Builder(this, player)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onPlaybackStateChanged(playbackState: Int) {
        when(playbackState){
            ExoPlayer.STATE_READY -> {
                player.playWhenReady = true
            }
            ExoPlayer.STATE_IDLE -> {
                //here you can set items and prepare
            }
        }
    }



















}