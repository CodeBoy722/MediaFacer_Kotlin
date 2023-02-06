package com.codeboy.mediafacerkotlin.musicSession

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver

class MusicPlaybackButtonReceiver: MediaButtonReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent != null) {
            if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
                val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT,KeyEvent::class.java)
                } else {
                    intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                }
                if (keyEvent != null) {
                    when (keyEvent.action) {
                        KeyEvent.ACTION_UP -> {
                            val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            when (keyEvent.keyCode) {
                                KeyEvent.KEYCODE_VOLUME_UP -> audioManager.adjustStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    AudioManager.ADJUST_RAISE,
                                    AudioManager.FLAG_PLAY_SOUND
                                )

                                KeyEvent.KEYCODE_VOLUME_DOWN -> {

                                }
                            }
                        }

                        KeyEvent.KEYCODE_MEDIA_PLAY -> {

                        }

                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {

                        }

                        KeyEvent.KEYCODE_MEDIA_NEXT -> {

                        }

                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {

                        }

                        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {

                        }

                        KeyEvent.KEYCODE_MEDIA_REWIND -> {

                        }

                    }
                }
            }
        }
    }
}