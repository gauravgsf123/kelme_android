package com.kelme.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kelme.EmergencyAlertActivity
import com.kelme.R
import com.kelme.activity.chat.CallerActivity
import com.kelme.model.VoipNotificationResponse
import com.kelme.utils.Constants


class SirenNotificationService: Service() {
    private val CHANNEL_ID = "SirenChannel"
    private val CHANNEL_NAME = "Siren Channel"
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SirenService","onStartCommand")
        var notification: Notification? = null
        var i = Intent(this, EmergencyAlertActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //startActivity(i)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 901, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val NOTIFICATION_CHANNEL_ID = CHANNEL_ID
            val channelName = CHANNEL_NAME
            var chan: NotificationChannel? = null
            chan = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            chan.importance = NotificationManager.IMPORTANCE_HIGH
            val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            notification = notificationBuilder
                .setSmallIcon(R.drawable.kelme_app_logo)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(pendingIntent,true)
                .build()
            startForeground(902, notification)

        } else  //API 26 and lower
        {
            i = Intent(this, EmergencyAlertActivity::class.java)
            val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo_size) // notification icon
                .setContentText("") // message for notification
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)



            val pi = PendingIntent.getActivity(this, 903, i, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            mBuilder.setContentIntent(pi)


        }


        return super.onStartCommand(intent, flags, startId)
    }
}