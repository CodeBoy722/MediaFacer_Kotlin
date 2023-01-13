package com.codeboy.mediafacerkotlin.musicSession

import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Bundle
import android.os.IBinder
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.Player

abstract class MusicService : MediaBrowserServiceCompat(), OnAudioFocusChangeListener, Player.Listener{

     override fun onCreate() {
         super.onCreate()
     }

     override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
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

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
    }



}