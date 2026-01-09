package com.kelme.activity.chat

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.kelme.R
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityCallerBinding
import com.kelme.event.CallEndedEvent
import com.kelme.fragment.chat.ChatViewModal
import com.kelme.model.VoipNotificationResponse
import com.kelme.model.request.CallEndRequest
import com.kelme.model.request.OtherUserJoinRejectCallRequest
import com.kelme.services.HeadsUpNotificationService
import com.kelme.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CallerActivity : BaseActivity(),View.OnClickListener {
    private lateinit var binding: ActivityCallerBinding
    private lateinit var voipresponse : VoipNotificationResponse
    private lateinit var viewModal: ChatViewModal
    val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    //private var ringtone = RingtoneManager.getRingtone(applicationContext,notification)
    private lateinit var ringtone: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        /*val pm: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
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
        }*/
        binding = DataBindingUtil.setContentView(this, R.layout.activity_caller)
        viewModal = ViewModelProvider(this, ViewModalFactory(application)).get(ChatViewModal::class.java)
        callGetIntent()
        initUI()
        EventBus.getDefault().register(this)

        /*ringtone = MediaPlayer.create(this, notification)
        //var ringtone = RingtoneManager.getRingtone(applicationContext,notification)
        if(!ringtone.isPlaying)
            ringtone.start()*/

        binding.fbReciveCall.setOnClickListener(this)
        binding.fbEndCall.setOnClickListener(this)
        binding.fbMessage.setOnClickListener(this)

        setObserver()
    }

    private fun setObserver() {

        viewModal.callEndResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    var receiverId = Utils.getReceiverId(voipresponse)
                    val request1 = OtherUserJoinRejectCallRequest(
                        voipresponse.channel_name,
                        arrayListOf(receiverId),
                        PrefManager.read(PrefManager.USER_ID, ""),
                        voipresponse.token,
                        voipresponse.call_type,
                        Constants.CallReject.RECEIVER
                    )
                    viewModal.otherUserjoinRejectCall(request1)
//
                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
//
                }
                else -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        }

        viewModal.otherUserjoinRejectCallResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    stopService(Intent(this,HeadsUpNotificationService::class.java))
                    finish()
//
                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
//
                }
                else -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        }
    }

    private fun initUI() {
    }

    private fun callGetIntent() {
        Log.d("NOTIFICATION_INTENT_TYPE", intent.getStringExtra(Constants.NOTIFICATION_INTENT_TYPE).toString())
        voipresponse = intent.getParcelableExtra<VoipNotificationResponse>(Constants.FIREBASE_RESPONSE)!!
        /*Log.d("channelName:--------------->>>>>",getIntent().getStringExtra(Constants.NOTIFICATION_INTENT_TYPE)!!)
        Log.d("channelName:--------------->>>>>",getIntent().getStringExtra(Constants.CALL_TYPE)!!)
        Log.d("channelName:--------------->>>>>",intent.getParcelableExtra<VoipNotificationResponse>(Constants.FIREBASE_RESPONSE)?.channel_name.toString())*/
        when {
            intent.getStringExtra(Constants.NOTIFICATION_INTENT_TYPE)==Constants.NotificationIntentType.NOTIFICATION -> {
                binding.userTv.text = voipresponse.sender_name
                binding.callingTv.text = voipresponse.call_type+" calling..."
                Glide.with(this).load(voipresponse.receiver_image).into(binding.userAv)
            }
            intent.getStringExtra(Constants.NOTIFICATION_INTENT_TYPE)==Constants.NotificationIntentType.RECEIVE -> {
                receiveCall()
                stopService(Intent(this,HeadsUpNotificationService::class.java))
            }
            intent.getStringExtra(Constants.NOTIFICATION_INTENT_TYPE)==Constants.NotificationIntentType.END -> {
                endCall()
            }
        }
    }

    override fun initializerControl() {
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            binding.fbReciveCall.id->{
                receiveCall()
            }
            binding.fbMessage.id->{
                endCall()
            }
            binding.fbEndCall.id->{
                endCall()
            }
        }
    }

    private fun endCall(){
        Ringtone.stopRingtone()
        val receiverId= Utils.getReceiverId(voipresponse)
        val request = CallEndRequest(
            voipresponse.channel_name,
            arrayListOf(receiverId),
            PrefManager.read(PrefManager.USER_ID, ""),
            voipresponse.token,
            voipresponse.call_type,
         //   Constants.CallType.AUDIO,
//            Constants.ChatType.SINGLE,
            Constants.CallReject.RECEIVER,"group", arrayListOf(receiverId).size.toString()
        )
        viewModal.callEnd(request)

    }

    private fun receiveCall(){
        Ringtone.stopRingtone()
        if(intent.getStringExtra(Constants.CALL_TYPE)==Constants.CallType.AUDIO) {
            val intent = Intent(this, CommonCallActivity::class.java)
            intent.putExtra(Constants.CALL_TYPE_AV, Constants.CallType.AUDIO)
            intent.putExtra(Constants.CALLER_TYPE, Constants.CallerType.RECEIVER)
            intent.putExtra(Constants.CALL_CHANNEL_NAME, getIntent().getStringExtra(Constants.CALL_CHANNEL_NAME))
            intent.putExtra(Constants.FIREBASE_RESPONSE,getIntent().getParcelableExtra<VoipNotificationResponse>(Constants.FIREBASE_RESPONSE))
            startActivity(intent)
            finish()
        }else if(intent.getStringExtra(Constants.CALL_TYPE)==Constants.CallType.VIDEO) {
            val intent = Intent(this, CommonCallActivity::class.java)
            intent.putExtra(Constants.CALL_TYPE_AV, Constants.CallType.VIDEO)
            intent.putExtra(Constants.CALLER_TYPE, Constants.CallerType.RECEIVER)
            intent.putExtra(Constants.CALL_CHANNEL_NAME, getIntent().getStringExtra(Constants.CALL_CHANNEL_NAME))
            intent.putExtra(Constants.FIREBASE_RESPONSE,getIntent().getParcelableExtra<VoipNotificationResponse>(Constants.FIREBASE_RESPONSE))
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
//
    }

    override fun onStop() {
        super.onStop()
        if(isFinishing){
            EventBus.getDefault().unregister(this)
        }
//        EventBus.getDefault().unregister(this)
        /*if(ringtone.isPlaying)
          ringtone.stop()*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CallEndedEvent) {
        Log.d("event","CallRejectEvent")
        finishActivity()
    }

    private fun finishActivity() {
        finish()
        //startActivity(Intent(context, SearchUserActivity::class.java))
    }
}