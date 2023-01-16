package com.codeboy.mediafacerkotlin.musicSession

import android.content.Context
import android.content.Intent
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver

class MusicPlaybackButtonReceiver: MediaButtonReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent != null){
          /*  if (intent.action == PlaybackStateCompat.ACTION_PLAY){

            }*/
        }
    }
}