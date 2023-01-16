package com.codeboy.mediafacerkotlin.musicSession

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.Observer
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.codeboy.mediafacer.models.AudioContent
import com.codeboy.mediafacerkotlin.utils.MusicDataUtil
import com.codeboy.mediafacerkotlin.viewModels.AudioViewModel

abstract class MusicService : MediaBrowserServiceCompat(), OnAudioFocusChangeListener, Player.Listener{

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
     }

     override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        TODO("Not yet implemented")
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
        for (musicItem in musicList){
            musicMetaDataList.add(musicItem.getMediaMetaDataCompat())
        }
    }

    private fun setupExoPlayer(){

    }

    private fun setupMediaSession(){

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
        super.onPlaybackStateChanged(playbackState)
    }





}