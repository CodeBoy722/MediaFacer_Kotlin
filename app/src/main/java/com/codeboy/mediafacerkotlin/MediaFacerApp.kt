package com.codeboy.mediafacerkotlin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.codeboy.mediafacerkotlin.MediaFacerApp.NotificationSource.MUSIC_EVENTS
import com.codeboy.mediafacerkotlin.MediaFacerApp.NotificationSource.NOTIFICATION_ID

class MediaFacerApp: Application() {

    object NotificationSource{
        const val MUSIC_EVENTS = "MediaFacer"
        const val NOTIFICATION_ID = 1010
    }



    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val EventChannel = NotificationChannel(
                NOTIFICATION_ID.toString(),
                MUSIC_EVENTS,
                NotificationManager.IMPORTANCE_HIGH
            )
            EventChannel.description = applicationContext.getString(R.string.description)
            EventChannel.setShowBadge(false)
            EventChannel.enableLights(false)
            EventChannel.enableVibration(false)
            EventChannel.setSound(null, null)
            //EventChannel.setLightColor(Color.BLUE);
            //EventChannel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            //EventChannel.setLightColor(Color.argb(0,255,20,147));
            notificationManager.createNotificationChannel(EventChannel)
        }

    }


}