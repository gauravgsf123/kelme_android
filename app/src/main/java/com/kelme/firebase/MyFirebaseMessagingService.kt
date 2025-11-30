
package com.kelme.firebase

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.JsonReader
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.kelme.R
import com.kelme.activity.chat.CallerActivity
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.event.CallEndedEvent
import com.kelme.event.CallRejectEventAudio
import com.kelme.event.CallRejectEventVideo
import com.kelme.model.NotificationResponse
import com.kelme.model.VoipNotificationResponse
import com.kelme.services.HeadsUpNotificationService
import com.kelme.services.SirenNotificationService
import com.kelme.utils.Constants
import com.kelme.utils.PrefManager
import com.kelme.utils.Ringtone
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.StringReader

class MyFirebaseMessagingService : FirebaseMessagingService()
{
    val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    var notificationManager: NotificationManager?=null
    override fun onNewToken(s: String) {
        PrefManager.write(PrefManager.FCM_TOKEN, s)
        Log.e(TAG, "onNewToken $s")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        //Ringtone.playSiren(this)
        Log.e(TAG, "onMessageReceived 01 ${remoteMessage.data}")
        val json = (remoteMessage.data as Map<String, String>?)?.let { JSONObject(it).toString() }
        val data = Gson().fromJson(
            json,
            VoipNotificationResponse::class.java
        )
        Log.e(TAG, "onMessageReceived 02 $data")

            //if(data.receiver_id!=null){
                try {


                    if (data.call_type != null) {
                        //Ringtone.playSiren(this)
                        callNotificationType(data)
                    }else if(remoteMessage.data["chatId"]!=null){
                        try {
                            val chatid = remoteMessage.data["chatId"].toString()
                            val body = remoteMessage.data["body"].toString()
                            val title = remoteMessage.data["title"].toString()
                            val intent = Intent(this, DashboardActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra("value", "2") // 2 for chat notification to move chat fragment
                            intent.action = "data"
                            intent.putExtra("chatId", chatid)
                            sendNotification(intent, title, body, "chat", "", chatid)
                        }catch (e:Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        val data = Gson().fromJson(json, NotificationResponse::class.java)
                        when (data.notification_type) {
                            "15" -> {
                                PrefManager.write(PrefManager.FROMNOTIFICATION, "1")
                                val intent = Intent(this, DashboardActivity::class.java)
                                intent.flags = FLAG_ACTIVITY_NEW_TASK
                                intent.action = "data"
                                intent.putExtra("value", "1")
                                sendNotification(
                                    intent,
                                    data.title,
                                    data.message, "15", data.from_user_id,"")
                            }"6" -> {
                            PrefManager.write(PrefManager.FROMNOTIFICATION, "1")
                            val intent = Intent(this, DashboardActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.action = "data"
                            intent.putExtra("value", "1")
                            sendNotification(
                                intent, data.title,
                                data.message, "6", data.from_user_id,""
                            )
                        }else -> {
                            PrefManager.write(PrefManager.FROMNOTIFICATION, "1")
                            val intent = Intent(this, DashboardActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.action = "data"
                            intent.putExtra("value", "1")
                            sendNotification(
                                intent, data.title,
                                data.message, "", "",""
                            )
                        }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }


    }

    private fun sendMessageToActivity(value: String, chatId: String) {
        val intent = Intent("NotificationUpdates")
        // You can also include some extra data.
        intent.putExtra("value", value)
        intent.putExtra("chatId", chatId)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun playSiren() {
        val mMediaPlayer = MediaPlayer.create(this, R.raw.siren);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // mMediaPlayer.isLooping = true
        mMediaPlayer.start()
    }

    private fun performClickAction(contex:Context, action:String, data: VoipNotificationResponse?=null){
        when(action){
            Constants.CALL_RECEIVE_ACTION->{
                var openIntent: Intent? = null
                try {
                    openIntent = Intent(this,CallerActivity::class.java)
                    openIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    startActivity(openIntent)
                }catch (e:Exception){e.printStackTrace()}
            }
            Constants.CALL_CANCEL_ACTION->{
                stopService(Intent(this, HeadsUpNotificationService::class.java))
                val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                sendBroadcast(it)
            }
        }
    }

    private fun callNotificationType(data: VoipNotificationResponse)
    {
        when (data.notification_type){
            /*Constants.NotificationType.EMERGENCY_BUTTON_ACTIVATION, Constants.NotificationType.CHECK_IN,
            Constants.NotificationType.PUBLICATION_OF_A_SECURITY_ALERT, Constants.NotificationType.PUBLICATION_OF_A_VARIOUS_TYPE_OF_REPORTS,
            Constants.NotificationType.PUBLICATION_OF_A_VARIOUS_TYPE_OF_NOTIFICATION, Constants.NotificationType.GEOFENCE_ALERTS,
            Constants.NotificationType.SAFETY_CHECK -> {
                wakeScreen()
            }

            Constants.NotificationType.CHAT->{
                sendNotification(
                    Intent(this, DashboardActivity::class.java),
                    "",
                    "", "", "",""
                )
            }*/
            Constants.NotificationType.CALLING->{
                wakeScreen()
                if(data.type == "Call request"){
                    Ringtone.playRingtone(this)
                    val openIntent = Intent(this,HeadsUpNotificationService::class.java)
                    openIntent.putExtra(Constants.CALL_TYPE, data.call_type)
                    openIntent.putExtra(Constants.CALL_CHANNEL_NAME,data.channel_name)
                    openIntent.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.NOTIFICATION)
                    openIntent.putExtra(Constants.FIREBASE_RESPONSE,data)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(openIntent)
                    }else startService(openIntent)
                }else{
                    Log.d("event","Call Reject")
                    stopService(Intent(this,HeadsUpNotificationService::class.java))
                    Ringtone.stopRingtone()
                    if(data.call_reject==Constants.CallReject.SENDER){
                        Log.d("event","Call Reject SENDER")
                        EventBus.getDefault().post(CallEndedEvent())
                    }else if(data.call_reject==Constants.CallReject.RECEIVER){
                        Log.d("event","Call Reject RECEIVER")
                        if(data.call_type==Constants.CallType.AUDIO){
                            Log.d("event","Call Reject RECEIVER AUDIO")
                            EventBus.getDefault().post(CallRejectEventAudio())
                        } else if(data.call_type==Constants.CallType.VIDEO){
                            Log.d("event","Call Reject RECEIVER VIDEO")
                            EventBus.getDefault().post(CallRejectEventVideo())
                        }
                    }
                }
            }
        }
    }

    private fun wakeScreen() {
        val pm: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        //val isScreenOn: Boolean = pm.isScreenOn
        val isScreenOn = if (Build.VERSION.SDK_INT >= 20) pm.isInteractive else pm.isScreenOn
        Log.e("screen_on", "" + isScreenOn)
        if (!isScreenOn) {
            val wl: PowerManager.WakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "myApp:MyLock"
            )
            wl.acquire(30000)
            val wl_cpu: PowerManager.WakeLock =
                pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Kelme:mycpuMyCpuLock")
            wl_cpu.acquire(30000)
        }
    }

    private fun notificationShow(data:VoipNotificationResponse){
        val notificationIntent = Intent(this, DashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)
        var notification: Notification? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val NOTIFICATION_CHANNEL_ID = "com.currency.usdtoinr"
            val channelName = "My Background Service"
            var chan: NotificationChannel? = null
            chan = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager =
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            notification = notificationBuilder
                .setSmallIcon(R.drawable.app_icon) // Small Icon
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.app_icon
                    )
                )
                .setContentTitle(data.title)
                .setContentText(data.message)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setFullScreenIntent(pendingIntent,true)
                .build()
            startForeground(2, notification)
            Log.d("MyFirebaseMessaging", "After startforeground executed")
        } else  //API 26 and lower
        {
            notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.app_icon) // Small Icon
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.app_icon
                    )
                )
                .setContentTitle(data.title)
                .setContentText(data.message)
                .setContentIntent(pendingIntent)
                .setTicker("")
                .build()
            startForeground(2, notification)
        }
    }

    private fun CallingNotificationShow(voipNotificationResponse: VoipNotificationResponse){
        val notificationIntent = Intent(this, CallerActivity::class.java)
        val channelName = voipNotificationResponse.channel_name
        notificationIntent.flags = FLAG_ACTIVITY_NEW_TASK
        notificationIntent.putExtra(Constants.CALL_TYPE, voipNotificationResponse.call_type)
        notificationIntent.putExtra(Constants.CALL_CHANNEL_NAME,channelName)
        notificationIntent.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.NOTIFICATION)
        notificationIntent.putExtra(Constants.FIREBASE_RESPONSE,voipNotificationResponse)
        //Log.d("channelName:---------------++++",data.channel_name)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        var notification: Notification? = null
        Log.d("channelName:---------------++++",voipNotificationResponse.channel_name)
        Ringtone.playRingtone(applicationContext)
        val customNotificationView = RemoteViews(packageName, R.layout.custom_notification_small)
        customNotificationView.setTextViewText(R.id.tv_calling, resources.getString(R.string.calling))
        customNotificationView.setTextViewText(R.id.tv_name, voipNotificationResponse.sender_name)

        val intentReceive = Intent(this, CallerActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        }
        intentReceive.flags = FLAG_ACTIVITY_NEW_TASK
        notificationIntent.putExtra(Constants.CALL_TYPE, voipNotificationResponse.call_type)
        notificationIntent.putExtra(Constants.CALL_CHANNEL_NAME,channelName)
        notificationIntent.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.NOTIFICATION)
        notificationIntent.putExtra(Constants.FIREBASE_RESPONSE,voipNotificationResponse)
        val pendingIntentReceive: PendingIntent = PendingIntent.getActivity(this, 0, intentReceive,
            PendingIntent.FLAG_IMMUTABLE)

        val intentEnd = Intent(this, CallerActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        }
        intentEnd.flags = FLAG_ACTIVITY_NEW_TASK
        notificationIntent.putExtra(Constants.CALL_TYPE, voipNotificationResponse.call_type)
        notificationIntent.putExtra(Constants.CALL_CHANNEL_NAME,channelName)
        notificationIntent.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.NOTIFICATION)
        notificationIntent.putExtra(Constants.FIREBASE_RESPONSE,voipNotificationResponse)
        val pendingIntentEnd: PendingIntent = PendingIntent.getActivity(this, 0, intentEnd,
            PendingIntent.FLAG_IMMUTABLE)

        customNotificationView.setOnClickPendingIntent(R.id.fb_recive_call, pendingIntentReceive)
        customNotificationView.setOnClickPendingIntent(R.id.fb_recive_call, pendingIntentEnd)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val NOTIFICATION_CHANNEL_ID = "com.currency.usdtoinr"
            val channelName = "My Background Service"
            var chan: NotificationChannel? = null
            chan = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            notification = notificationBuilder
                .setSmallIcon(R.drawable.app_icon) // Small Icon
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.app_icon
                    )
                )
                .setContentTitle(getString(R.string.calling))
                .setContentText(voipNotificationResponse.sender_name)
                .setCustomContentView(customNotificationView)
                .setCustomBigContentView(customNotificationView)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                //.setFullScreenIntent(pendingIntent,true)
                //.setContentIntent(pendingIntent)
                .build()
            startForeground(2, notification)

            Log.d("MyFirebaseMessaging", "After startforeground executed")
        } else  //API 26 and lower
        {
            notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.app_icon) // Small Icon
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.app_icon)
                )
                .setContentTitle(getString(R.string.calling))
                .setContentText(voipNotificationResponse.sender_name)
                .setContentIntent(pendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setTicker("")
                .build()
            startForeground(2, notification)

        }
    }

    private fun showNotification(data: VoipNotificationResponse) {
        val notifyIntent = Intent(this, CallerActivity::class.java)
        //  notifyIntent.putExtra("flag","History");
        // Set the Activity to start in a new, empty task
        notifyIntent.flags = (FLAG_ACTIVITY_NEW_TASK
                or FLAG_ACTIVITY_CLEAR_TASK)
        notifyIntent.putExtra(Constants.CALL_TYPE, data.call_type)
        notifyIntent.putExtra(
            Constants.NOTIFICATION_INTENT_TYPE,
            Constants.NotificationIntentType.NOTIFICATION
        )
        notifyIntent.putExtra(Constants.FIREBASE_RESPONSE, data)
        //notifyIntent.putExtra(Constants.CHANNEL_NAME,ringtone)
        // Create the PendingIntent
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        //ringtone = MediaPlayer.create(this, notification)
        //var ringtone = RingtoneManager.getRingtone(applicationContext,notification)
        /*if(!ringtone.isPlaying)
            ringtone.start()*/
        Ringtone.playRingtone(applicationContext)

        val ringtone: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "com.kelme"
        if (Build
                .VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "My Background Service",
                NotificationManager.IMPORTANCE_NONE
            )
            notificationChannel.description = "Voip Channel"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableLights(true)
            notificationChannel.setShowBadge(true);
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager?.createNotificationChannel(notificationChannel)

        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        /*RemoteViews contentView = new RemoteViews(getPackageName() , R.layout. custom_notification_layout ) ;
        notificationBuilder.setAutoCancel(true)
                //.setContent(contentView)
                .setContentIntent(notifyPendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                // .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title).setColor(getResources().getColor(R.color.red)).setColorized(true)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setContentInfo("Info")
                .setColorized(true);*/
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            notificationBuilder.setSmallIcon(R.mipmap.ic_kelme_logo)
            notificationBuilder.setContentTitle(resources.getString(R.string.calling)).setStyle(
                NotificationCompat.BigTextStyle().bigText(data.sender_name))
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLights(Color.WHITE, 500, 500)
                .setContentText(data.sender_name)
        } else {
            val customNotificationView = RemoteViews(packageName, R.layout.custom_notification_small)

            //  customNotificationView.setImageViewBitmap(R.id.imgNupeye, R.mipmap.ic_launcher);
            customNotificationView.setTextViewText(R.id.tv_calling, resources.getString(R.string.calling))
            customNotificationView.setTextViewText(R.id.tv_name, data.sender_name)

            notificationBuilder.setContent(customNotificationView);
            notificationBuilder.setSmallIcon(R.mipmap.ic_kelme_logo)
            notificationBuilder.setTimeoutAfter(30000)
            notificationBuilder.setColorized(true)
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL)
            notificationBuilder.setAutoCancel(true)
            notificationBuilder.priority = NotificationCompat.PRIORITY_MAX

            val intentReceive = Intent(this, CallerActivity::class.java).apply {
                flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            }
            intentReceive.flags = FLAG_ACTIVITY_NEW_TASK
            intentReceive.putExtra(Constants.CALL_TYPE, data.call_type)
            intentReceive.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.RECEIVE)
            intentReceive.putExtra(Constants.FIREBASE_RESPONSE,data)
            val pendingIntentReceive: PendingIntent = PendingIntent.getActivity(this, 0, intentReceive,
                PendingIntent.FLAG_IMMUTABLE)

            val intentEnd = Intent(this, CallerActivity::class.java).apply {
                flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            }
            intentEnd.flags = FLAG_ACTIVITY_NEW_TASK
            intentEnd.putExtra(Constants.CALL_TYPE, data.call_type)
            intentEnd.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.END)
            intentEnd.putExtra(Constants.FIREBASE_RESPONSE,data)
            val pendingIntentEnd: PendingIntent = PendingIntent.getActivity(this, 0, intentEnd,
                PendingIntent.FLAG_IMMUTABLE)

            customNotificationView.setOnClickPendingIntent(R.id.fb_recive_call, pendingIntentReceive)
            customNotificationView.setOnClickPendingIntent(R.id.fb_end_call, pendingIntentEnd)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.mipmap.ic_kelme_logo)
            notificationBuilder.color = resources.getColor(R.color.primary_blue)
        } else {
            // notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            notificationBuilder.setSmallIcon(R.mipmap.ic_kelme_logo)
            notificationBuilder.color = resources.getColor(R.color.primary_blue)
        }
        //notificationBuilder.setContentIntent(notifyPendingIntent)
        notificationBuilder.setFullScreenIntent(notifyPendingIntent, true)
        notificationManager?.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    companion object {
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
        private const val CHANNEL_ID = "144"
        private const val NOTIFICATION_ID = 101
    }

    fun showCallerDialog(data: VoipNotificationResponse) {

        if(data.type == "Call request"){
            val intent = Intent(this, CallerActivity::class.java)
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Constants.CALL_TYPE, data.call_type)
            intent.putExtra(Constants.FIREBASE_RESPONSE,data)
            startActivity(intent)
        }else{
            Log.d("event","Call Reject")
            if(data.call_reject==Constants.CallReject.SENDER){
                EventBus.getDefault().post(CallEndedEvent())
            }else if(data.call_reject==Constants.CallReject.RECEIVER){
                if(data.call_type == Constants.CallType.AUDIO)
                EventBus.getDefault().post(CallRejectEventAudio())
                if(data.call_type == Constants.CallType.VIDEO)
                    EventBus.getDefault().post(CallRejectEventVideo())
            }
        }
    }

    fun sendNotification(
        intent: Intent?,
        title: String?,
        messageBody: String?,
        notificationType: String,
        currentUser: String,
        chatid: String
    ) {
        if ((notificationType == "15" || notificationType == "6") && PrefManager.read(PrefManager.USER_ID, "it") != currentUser) {
            Ringtone.playSiren(this)
                /*try {
                    val r: android.media.Ringtone? = RingtoneManager.getRingtone(
                        applicationContext,
                        Uri.parse("android.resource://" + this.packageName + "/" + R.raw.siren)
                    )
                    r?.play()

                    val soundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.siren)
                    val mediaPlayer = MediaPlayer.create(applicationContext, soundUri)
                    mediaPlayer.start()  // Play sound

                    // Optionally, you can release the MediaPlayer once it's finished
                    mediaPlayer.setOnCompletionListener {
                        mediaPlayer.release()
                    }

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }*/
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                try {
                    val notification =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r: android.media.Ringtone? =
                        RingtoneManager.getRingtone(applicationContext, notification)
                    r?.play()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        val requestID = System.currentTimeMillis().toInt()
        val notificationBuilder: NotificationCompat.Builder
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(intent!!)
        val pendingIntent = stackBuilder.getPendingIntent(
            requestID,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val soundUri: Uri = Uri.parse("android.resource://${packageName}/raw/siren")
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            Log.e(TAG, "sendNotification: Here 01 $title $messageBody" )
            channel.description = messageBody
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.enableLights(true)
            channel.enableVibration(true)
            // do something for phones running an SDK before oreo
            notificationBuilder =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.kelme_app_logo) // Small Icon
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.kelme_app_logo
                        )
                    )
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setSound(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.siren))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(
                System.currentTimeMillis().toInt(),
                notificationBuilder.build()
            )
        } else {
            // do something for phones running an SDK before oreo
            Log.e(TAG, "sendNotification: Here 02 $title $messageBody" )
            notificationBuilder =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.kelme_app_logo) // Small Icon
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.drawable.kelme_app_logo
                        )
                    )
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                    .setAutoCancel(true)
                    .setSound(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.siren))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(),
                notificationBuilder.build())
        }

        if(notificationType=="15" || notificationType=="6") {
            if (PrefManager.read(PrefManager.USER_ID, "it") != currentUser) {
                wakeScreen()
                Ringtone.playSiren(applicationContext)
                val intent1 = Intent()
                intent1.action = "com.kelme.MY_SIGNAL"
                sendBroadcast(intent1)
                /*val openIntent = Intent(this,SirenNotificationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(openIntent)
                }else startService(openIntent)*/

                /*val openIntent = Intent(this,HeadsUpNotificationService::class.java)
                openIntent.putExtra(Constants.CALL_TYPE, "")
                openIntent.putExtra(Constants.CALL_CHANNEL_NAME,"data.channel_name")
                openIntent.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.NOTIFICATION)
                openIntent.putExtra(Constants.FIREBASE_RESPONSE,"data")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(openIntent)
                }else startService(openIntent)*/
            }
        }else if (notificationType=="chat"){
            sendMessageToActivity("2",chatid)
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun createCustomNotification(data: VoipNotificationResponse, senderName: String) {

        val notificationID: Int = 100
        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, CallerActivity::class.java).apply {
            flags = FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TASK
        }
        //intent.data = Uri.parse("https://www.tutorialsbuzz.com/")
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Constants.CALL_TYPE, data.call_type)
        intent.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.NOTIFICATION)
        intent.putExtra(Constants.FIREBASE_RESPONSE,data)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val channelId = "Kelme"
        val channelName = "Kelme"
        Ringtone.playRingtone(applicationContext)
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            //.setSound(alarmSound)
            //.setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data.sender_name))
            .setTimeoutAfter(30000)
            .setOngoing(true)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setVibrate(longArrayOf(500, 500, 500))
            .setDefaults(Notification.DEFAULT_VIBRATE)

        val smallContent = RemoteViews(packageName, R.layout.custom_notification_small)
        val bigContent = RemoteViews(packageName, R.layout.custom_notification_small)

        val callerIntent = Intent(applicationContext, CallerActivity::class.java)
        val callerReceive = PendingIntent.getService(applicationContext, 0, callerIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val intentReceive = Intent(this, CallerActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        }
        intentReceive.flags = FLAG_ACTIVITY_NEW_TASK
        intentReceive.putExtra(Constants.CALL_TYPE, data.call_type)
        intent.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.RECEIVE)
        intentReceive.putExtra(Constants.FIREBASE_RESPONSE,data)
        val pendingIntentReceive: PendingIntent = PendingIntent.getActivity(this, 0, intentReceive,
            PendingIntent.FLAG_IMMUTABLE)

        val intentEnd = Intent(this, CallerActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        }
        intentEnd.flags = FLAG_ACTIVITY_NEW_TASK
        intentEnd.putExtra(Constants.CALL_TYPE, data.call_type)
        intent.putExtra(Constants.NOTIFICATION_INTENT_TYPE, Constants.NotificationIntentType.END)
        intentEnd.putExtra(Constants.FIREBASE_RESPONSE,data)
        val pendingIntentEnd: PendingIntent = PendingIntent.getActivity(this, 0, intentEnd,
            PendingIntent.FLAG_IMMUTABLE)

        smallContent.setOnClickPendingIntent(R.id.fb_recive_call, pendingIntentReceive)
        smallContent.setOnClickPendingIntent(R.id.fb_end_call, pendingIntentEnd)

        bigContent.setOnClickPendingIntent(R.id.fb_recive_call, pendingIntentReceive)
        bigContent.setOnClickPendingIntent(R.id.fb_end_call, pendingIntentEnd)

        /*val accept = findViewById<Button>(R.id.accept)
        accept.setOnClickListener {
            Toast.makeText(this,"accept", Toast.LENGTH_LONG).show()
        }
        val reject = findViewById<Button>(R.id.reject)
        reject.setOnClickListener {
            Toast.makeText(this,"reject", Toast.LENGTH_LONG).show()
        }*/

        bigContent.setTextViewText(R.id.tv_name, senderName)
        smallContent.setTextViewText(R.id.tv_name, senderName)

        bigContent.setImageViewResource(R.id.image, R.drawable.user)
        smallContent.setImageViewResource(R.id.image, R.drawable.user)

        builder.setContent(bigContent)
        builder.setCustomBigContentView(bigContent)

        with(NotificationManagerCompat.from(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = data.channel_name;
                channel.setShowBadge(true);
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC;
                createNotificationChannel(channel)
                notify(notificationID, builder.build())
            }else{
                //createNotificationChannel(channel)
                notify(notificationID, builder.build())
            }
        }
    }

   /* fun sendChatNotification(
        intent: Intent?,
        senderName: String?,
        messageBody: String?,
        senderId: String?
    ) {
        val notificationBuilder: NotificationCompat.Builder
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(intent!!)
        val pendingIntent = stackBuilder.getPendingIntent(100, PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = messageBody
            // do something for phones running an SDK before oreo
            notificationBuilder =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher) // Small Icon
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.mipmap.ic_launcher
                        )
                    )
                    .setContentTitle(senderName)
                    .setContentText(messageBody)
                    //.setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setStyle(
                        NotificationCompat.InboxStyle()
                            .addLine(messageBody)
                            .setBigContentTitle(senderName)
                    )
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(
                senderId!!.toInt(),
                notificationBuilder.build()
            )
        } else {
            // do something for phones running an SDK before oreo
            notificationBuilder =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher) // Small Icon
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources,
                            R.mipmap.ic_launcher
                        )
                    )
                    .setContentTitle(senderName)
                    .setContentText(messageBody)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    //.setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                    .setStyle(
                        NotificationCompat.InboxStyle()
                            .addLine(messageBody)
                            .setBigContentTitle(senderName)
                    )
                    .setAutoCancel(true)
                    // .setGroup(senderId)
                    // .setGroupSummary(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(
                senderId!!.toInt(),
                notificationBuilder.build()
            )
        }
    }*/
}
