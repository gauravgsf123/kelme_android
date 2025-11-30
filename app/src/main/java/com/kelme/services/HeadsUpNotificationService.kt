package com.kelme.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.kelme.R
import com.kelme.activity.chat.CallerActivity
import com.kelme.model.VoipNotificationResponse
import com.kelme.utils.Constants
import java.util.*

class HeadsUpNotificationService : Service() {
    private val CHANNEL_ID = "VoipChannel"
    private val CHANNEL_NAME = "Voip Channel"
    private var data :VoipNotificationResponse?=null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("service","onStartCommand")
        if (intent?.extras != null) {
            var channel =  intent.getStringExtra(Constants.CALL_CHANNEL_NAME)
            data = intent.getParcelableExtra(Constants.FIREBASE_RESPONSE)!!
            Log.d("channel", data?.channel_name.toString())
        }

        var notificationIntent = Intent(this, CallerActivity::class.java)
        notificationIntent.putExtra(Constants.CALL_TYPE, intent?.getStringExtra(Constants.CALL_TYPE))
        notificationIntent.putExtra(Constants.NOTIFICATION_INTENT_TYPE, intent?.getStringExtra(Constants.NOTIFICATION_INTENT_TYPE))
        notificationIntent.putExtra(Constants.CALL_CHANNEL_NAME, intent?.getStringExtra(Constants.CALL_CHANNEL_NAME))
        notificationIntent.putExtra(Constants.FIREBASE_RESPONSE,intent?.getParcelableExtra<VoipNotificationResponse>(Constants.FIREBASE_RESPONSE))
        var pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        var notification: Notification? = null

//        val voipresponse = intent?.getParcelableExtra<VoipNotificationResponse>(Constants.FIREBASE_RESPONSE)!!
        val callerType = if(data?.call_type==Constants.CallType.AUDIO){
            "Audio"
        }else "Video"
        val customNotificationView = RemoteViews(packageName, R.layout.custom_notification_small)
        customNotificationView.setTextViewText(R.id.tv_calling, callerType+" "+resources.getString(R.string.calling))
        customNotificationView.setTextViewText(R.id.tv_name, intent?.getParcelableExtra<VoipNotificationResponse>(Constants.FIREBASE_RESPONSE)?.sender_name)

        val intentReceive = Intent(this, CallerActivity::class.java)
        intentReceive.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intentReceive.putExtra(Constants.CALL_TYPE, intent?.getStringExtra(Constants.CALL_TYPE))
        intentReceive.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.RECEIVE)
        intentReceive.putExtra(Constants.CALL_CHANNEL_NAME, intent?.getStringExtra(Constants.CALL_CHANNEL_NAME))
        intentReceive.putExtra(Constants.FIREBASE_RESPONSE,intent?.getParcelableExtra<VoipNotificationResponse>(Constants.FIREBASE_RESPONSE))
        val pendingIntentReceive: PendingIntent = PendingIntent.getActivity(this, 102, intentReceive, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val intentEnd = Intent(this, CallerActivity::class.java)
        intentEnd.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intentEnd.putExtra(Constants.CALL_TYPE, intent?.getStringExtra(Constants.CALL_TYPE))
        intentEnd.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.END)
        intentEnd.putExtra(Constants.CALL_CHANNEL_NAME, intent?.getStringExtra(Constants.CALL_CHANNEL_NAME))
        intentEnd.putExtra(Constants.FIREBASE_RESPONSE,intent?.getParcelableExtra<VoipNotificationResponse>(Constants.FIREBASE_RESPONSE))
        val pendingIntentEnd: PendingIntent = PendingIntent.getActivity(this, 101, intentEnd, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        customNotificationView.setOnClickPendingIntent(R.id.fb_recive_call, pendingIntentReceive)
        customNotificationView.setOnClickPendingIntent(R.id.fb_end_call, pendingIntentEnd)

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
                .setSmallIcon(R.drawable.logo_size)
                .setContentTitle(resources.getString(R.string.calling))
                .setCustomContentView(customNotificationView)
                .setCustomBigContentView(customNotificationView)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(pendingIntent,true)
                .build()
            startForeground(201, notification)

            Log.d("HeadsUpNotificationService", "After startforeground executed")
        } else  //API 26 and lower
        {
            notificationIntent = Intent(this, CallerActivity::class.java)

            val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo_size) // notification icon
                .setContentTitle(resources.getString(R.string.calling)) // title for notification
                .setContentText("") // message for notification
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)



            val pi = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            mBuilder.setContentIntent(pi)


        }

        //return START_STICKY
        return super.onStartCommand(intent, flags, startId)
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Call Notifications"
            Objects.requireNonNull(baseContext.getSystemService(NotificationManager::class.java))
                .createNotificationChannel(channel)
        }
    }
}