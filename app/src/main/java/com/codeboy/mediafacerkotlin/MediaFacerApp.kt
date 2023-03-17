package com.codeboy.mediafacerkotlin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.codeboy.mediafacerkotlin.MediaFacerApp.NotificationSource.MUSIC_EVENTS
import com.codeboy.mediafacerkotlin.MediaFacerApp.NotificationSource.channelID

class MediaFacerApp: Application() {

    object NotificationSource{
        const val MUSIC_EVENTS = "media_facer_audio_playback"
        const val channelID = "media_facer_channel"
        const val NOTIFICATION_ID = 1010
    }

    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val eventChannel = NotificationChannel(
                channelID,
                MUSIC_EVENTS,
                NotificationManager.IMPORTANCE_HIGH
            )
            eventChannel.description = applicationContext.getString(R.string.description)
            eventChannel.setShowBadge(false)
            eventChannel.enableLights(false)
            eventChannel.enableVibration(false)
            eventChannel.setSound(null, null)
            //EventChannel.setLightColor(Color.BLUE);
            //EventChannel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            //EventChannel.setLightColor(Color.argb(0,255,20,147));
            notificationManager.createNotificationChannel(eventChannel)
        }

    }


}