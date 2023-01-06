package com.codeboy.mediafacerkotlin.musicSession

import android.content.Intent
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.os.IBinder
import android.service.media.MediaBrowserService

 abstract class MusicService : MediaBrowserService(), OnAudioFocusChangeListener{

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

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }



}