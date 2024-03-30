package com.kelme.activity.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityAudioCallBinding
import com.kelme.event.CallRejectEventAudio
import com.kelme.fragment.chat.ChatViewModal
import com.kelme.model.VoipNotificationResponse
import com.kelme.model.request.*
import com.kelme.model.response.ChatListModelWithName
import com.kelme.utils.*
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

class AudioCallActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAudioCallBinding
    var startTime: Long = 0
    private lateinit var engine: RtcEngine
    private var myUid = 0
    private var joined = false
    private var isMuted = true
    private var isSpeaker = true
    private lateinit var viewModal: ChatViewModal
    private var agoraToken: String = ""
    private var channelName = ""
    private var chatModel: ChatListModelWithName? = null
    private var firebaseModel: VoipNotificationResponse? = null
    private var callerType = ""
    private var isCallConnect = false
    private var mPlayer2: MediaPlayer?=null
    var countdown_timer: CountDownTimer?=null
    val PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
    )

    var timerHandler: Handler = Handler()
    var timerRunnable: Runnable = object : Runnable {
        override fun run() {
            val millis: Long = System.currentTimeMillis() - startTime
            var seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            seconds %= 60
            binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            timerHandler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio_call)

        viewModal =
            ViewModelProvider(this, ViewModalFactory(application)).get(ChatViewModal::class.java)
        if (!PermissionUtil.hasPermissions(this, *PERMISSIONS)) requestPermission(this)

        EventBus.getDefault().register(this)
        init()
    }

    private fun init() {

        try {
            val appId = getString(R.string.agora_app_id)
            engine = RtcEngine.create(
                applicationContext,
                appId,
                iRtcEngineEventHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
            onBackPressed()
        }

        binding.backArrow.setOnClickListener(this)
        binding.ivCall.setOnClickListener(this)
        binding.ivMic.setOnClickListener(this)
        binding.ivSpeaker.setOnClickListener(this)
        binding.ivAdd.setOnClickListener(this)

        callerType = intent.getStringExtra(Constants.CALLER_TYPE)!!
        Log.d("call_type",callerType)
        setObserver()
        when (callerType) {
            Constants.CallerType.RECEIVER -> {
                firebaseModel = intent.getParcelableExtra(Constants.FIREBASE_RESPONSE)!!
                channelName = intent.getStringExtra(Constants.CALL_CHANNEL_NAME)!!//firebaseModel?.channel_name!!
                Log.d("channelName:---------------XXXXX",intent.getStringExtra(Constants.CALL_CHANNEL_NAME)!!)
                //agoraToken = firebaseModel?.token!!
                callAPI()

                //setObserver()
            }
            Constants.CallerType.CALLER -> {
                chatModel = intent.getParcelableExtra(Constants.CHATLIST_MODEL)!!
                binding.tvCallerName.text = intent.getStringExtra(Constants.USER_NAME)//chatModel!!.name
                channelName = createChannelName()
                callAPI()
            }
        }
    }

    private fun createChannelName(): String {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        return "${chatModel?.chatId}_kelme_${dateFormat.format(date)}"
    }

    private fun callAPI() {
        val request = GetTokenAgoraRequest(channelName, PrefManager.read(PrefManager.USER_ID, ""))
        viewModal.getTokenAgora(request)
    }

    private fun setObserver() {
        viewModal.getTokenAgoraResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        Log.e("agoraToken", response.data?.token!!)
                        agoraToken = response.data.token
                        joinChannel(channelName, response.data.token)
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else if (response.message == "201") {
                      //  showLongToast(response.message)
                    } else {
                        // showLongToast(response.message)
                    }
                }
            }
        }

        viewModal.getVoipTokenResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        val firebaseList = mutableListOf<String>()
                        //agoraToken = "0063c43f90e857a4c4e80771185ad6bcac2IAAD9Pl9uhiSnP1frWuE5rMIGP/NNKEAO5SDW6//H/mNLNVwmCgAAAAAEAAJjRWPiAlTYQEAAQCICVNh"
                        response.data?.let { showLog(it) }
                        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
                        val filteredKeysMap = chatModel?.chatMembers?.filterKeys { it != uid }
                        chatModel?.chatMembers?.forEach {
                            Log.d("firebaseList", it.key)
                            firebaseList.add(it.key)// = listOf(it.key) as MutableList<String>
                        }
                        firebaseList.remove(uid)

                        val request = OtherUserRequestCallRequest(
                            channelName,
                            firebaseList,//listOf(filteredKeysMap?.keys?.first()) as List<String>,
                            PrefManager.read(PrefManager.USER_ID, "").toInt(),
                            agoraToken,
                            Constants.CallType.AUDIO,
                            Constants.ChatType.SINGLE
                        )
                        viewModal.otherUserRequestCall(request)
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else if (response.message == "201") {
                        // showLongToast(response.message)
                    } else {
                        // showLongToast(response.message)
                    }
                }
            }
        }

        viewModal.callEndResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        //agoraToken = response.data?.token.toString()
                        response.data?.let { showLog(response.data) }
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else if (response.message == "201") {
                        // showLongToast(response.message)
                    } else {
                        //  showLongToast(response.message)
                    }
                }
            }
        }

        viewModal.senderJoinCalSuccessfullyResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        //agoraToken = response.data?.token.toString()

                        countDownTimer()
                        val request = GetVoipTokenRequest(
                            agoraToken,
                            PrefManager.read(PrefManager.USER_ID, "")
                        )
                        viewModal.getVoipToken(request)

                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else if (response.message == "201") {
                        //showLongToast(response.message)
                    } else {
                        //showLongToast(response.message)
                    }
                }
            }
        }

        viewModal.otherUserRequestCallResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        //agoraToken = response.data?.token.toString()
                        response.data?.let {
                            showLog(response.data.message)
                            //Toast.makeText(this, "" + response.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else if (response.message == "201") {
                        // showLongToast(response.message)
                    } else {
                        // showLongToast(response.message)
                    }
                }
            }
        }

        viewModal.otherUserJoinCallSuccessfullyResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        //agoraToken = response.data?.token.toString()
                        //response.data?.let { showLog(response.data.message) }
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    //  showLongToast(response.message)
                }
            }
        }

        viewModal.otherUserjoinRejectCallResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    // showLongToast(response.message)
                }
            }
        }
    }


    private fun countDownTimer() {
        mPlayer2 = MediaPlayer.create(this, R.raw.ringing)
        mPlayer2?.start()
        mPlayer2?.isLooping = true

        countdown_timer = object : CountDownTimer(30000, 1000) {
            override fun onFinish() {
                if(!isCallConnect){
                    countdown_timer?.cancel()
                    mPlayer2?.stop()
                    engine.leaveChannel()
                    Log.d("Endssssss", "onFinish:CountDownTimer ")
                    endCall()
                    // showLongToast(resources.getString(R.string.user_not_response_your_call))
                }
            }

            override fun onTick(p0: Long) {
                Log.d("countDownTimer",p0.toString())
            }
        }
        countdown_timer?.start()

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            binding.ivCall.id -> {
                countdown_timer?.cancel()
                Log.d("Endssssss", "onFinish:onclick ")
                endCall()
                mPlayer2?.stop()
            }
            binding.ivMic.id -> {
                val res = if (isMuted) R.drawable.ic_mic else R.drawable.ic_mic_nute
                binding.ivMic.setImageResource(res)
                engine.muteLocalAudioStream(!isMuted)
                isMuted = !isMuted
            }
            binding.ivSpeaker.id -> {
                val res = if (isSpeaker) R.drawable.ic_speaker_off else R.drawable.ic_speaker
                binding.ivSpeaker.setImageResource(res)
                engine.setEnableSpeakerphone(!isSpeaker)
                isSpeaker = !isSpeaker
            }
            binding.ivAdd.id -> {
                val intent = Intent(this, AddNewMemberInCallActivity::class.java)
                intent.putExtra("channel", channelName)
                intent.putExtra("agoratoken", agoraToken)
                intent.putExtra("callType",  Constants.CallType.AUDIO)
                Log.d(TAG, "onClick: channel $channelName")
                Log.d(TAG, "onClick: agoratoken $agoraToken")
                startActivity(intent)
            }
        }
    }


    private fun endCall() {
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val firebaseList = mutableListOf<String>()
        if (chatModel != null) {
            chatModel?.chatMembers?.forEach {
                Log.d("firebaseList",it.key)
                if(it.key!=uid) {
                    firebaseList.add(it.key)// = listOf(it.key) as MutableList<String>
                }
            }
            //firebaseList = listOf(it.key)
        } else {
            if(uid!=firebaseModel?.receiver_id){
                firebaseModel?.receiver_id?.let { firebaseList.add(it)
                    Log.d(TAG, "endCall: list$it")}//firebaseModel?.receiver_id
            }else{
                firebaseModel?.sender_id?.let { firebaseList.add(it)
                    Log.d(TAG, "endCall: list$it")}//firebaseModel?.receiver_id
            }
        }

        val request = CallEndRequest(
            channelName,
            firebaseList,//listOf(firebaseId) as List<String>,
            PrefManager.read(PrefManager.USER_ID, ""),
            agoraToken,
            Constants.CallType.AUDIO,
//            Constants.ChatType.SINGLE,
            Constants.CallReject.SENDER,
            "group",firebaseList.size.toString()   ///will need  to  check
        )
        viewModal.callEnd(request)

        engine.leaveChannel()
    }

    override fun initializerControl() {

    }

    override fun onStop() {
        super.onStop()
//        EventBus.getDefault().unregister(this)
//        //if(mPlayer2!=null && mPlayer2.isPlaying)
//        mPlayer2?.stop()
//        engine.leaveChannel()
    }

    private fun joinChannel(channelId: String, accessToken: String) {
        Log.d("channelId", channelId)
        Log.d("accessToken", accessToken)
        engine.enableAudioVolumeIndication(1000, 3,true)
        engine.setEnableSpeakerphone(!isSpeaker)
        engine.muteLocalAudioStream(!isMuted)
        var res: Int=0
        if (callerType == Constants.CallerType.CALLER) {
            res = engine.joinChannel(
                accessToken,
                channelId,
                "",
                PrefManager.read(PrefManager.USER_ID, "").toInt()
            )
            Log.d("joinChannel", res.toString())
        } else {
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 0)
            res = engine.joinChannel(
                accessToken,
                channelId,
                "", PrefManager.read(PrefManager.USER_ID, "").toInt()
            )
            Log.d("joinChannel", res.toString())
        }
        if (callerType == Constants.CallerType.CALLER) {
            if (res < 0) {
                val request = SenderCanNotJoinCallRequest(
                    channelName,
                    PrefManager.read(PrefManager.USER_ID, ""),
                    ""
                )
                viewModal.senderCanNotJoinCall(request)
                return
            } else {
                val request = SenderJoinCallSuccessfullyRequest(
                    channelName,
                    PrefManager.read(PrefManager.USER_ID, "")
                )
                viewModal.senderJoinCalSuccessfully(request)
            }
        }else{
            if (res < 0) {
                val receiverId=Utils.getReceiverId(firebaseModel!!)
                val request = OtherUserJoinRejectCallRequest(
                    channelName,
                    arrayListOf(receiverId),
                    PrefManager.read(PrefManager.USER_ID, ""),
                    firebaseModel!!.token,
                    Constants.CallType.AUDIO,
                    Constants.CallReject.SENDER
                )
                viewModal.otherUserjoinRejectCall(request)
                return
            } else {
                val receiverId=Utils.getReceiverId(firebaseModel!!)
                val request = OtherUserJoinCallSuccessfullyRequest(
                    PrefManager.read(PrefManager.USER_ID, ""),
                    channelName,
                    arrayListOf(receiverId)
                )
                viewModal.otherUserJoinCallSuccessfully(request)

            }
        }
    }

    private val iRtcEngineEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        /**Reports a warning during SDK runtime.
         * Warning code: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_warn_code.html */
        override fun onWarning(warn: Int) {
            Log.w(
                TAG,
                String.format(
                    "onWarning code %d message %s",
                    warn,
                    RtcEngine.getErrorDescription(warn)
                )
            )
        }

        /**Reports an error during SDK runtime.
         * Error code: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html */
        override fun onError(err: Int) {
            Log.e(
                TAG,
                String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err))
            )
            showAlert(
                String.format(
                    "onError code %d message %s",
                    err,
                    RtcEngine.getErrorDescription(err)
                )
            )
            finish()
        }

        /**Occurs when a user leaves the channel.
         * @param stats With this callback, the application retrieves the channel information,
         * such as the call duration and statistics.
         */
        override fun onLeaveChannel(stats: RtcStats) {
            super.onLeaveChannel(stats)
            Log.i(
                TAG,
                String.format("local user %d leaveChannel!", myUid)
            )
            //showLongToast(String.format("local user %d leaveChannel!", myUid))
            finish()
        }


        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.i(
                TAG,
                String.format("onJoinChannelSuccess channel %s uid %d", channel, uid)
            )
           // showLongToast(String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
            myUid = uid
            joined = true
            handler?.post {
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            isCallConnect = true
            Log.i(TAG, "onUserJoined->$uid")
            countdown_timer?.cancel()
            Log.d("Endssssss", "onFinish:onUserJoined ")
            mPlayer2?.stop()
            //showLongToast(String.format("user %d joined!", uid))
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 0)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(
                TAG,
                String.format("user %d offline! reason:%d", uid, reason)
            )
            timerHandler.removeCallbacks(timerRunnable)
            Log.d("Endssssss", "onFinish:onUserOffline ")
            finish()
            //showLongToast(String.format("user %d offline! reason:%d", uid, reason))
        }

        override fun onActiveSpeaker(uid: Int) {
            super.onActiveSpeaker(uid)
            Log.i(
                TAG,
                String.format("onActiveSpeaker:%d", uid)
            )
        }
    }

    override fun onBackPressed() {
    }

    fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            Constants.PERMISSIONS,
            Constants.PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var res = true
        if (requestCode == Constants.PERMISSIONS_REQUEST_CODE && grantResults.isNotEmpty()) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    res = false
                }
            }
            if (res) {
                //launch()
            } else {
//                Toast.makeText(
//                    applicationContext,
//                    "All permissions are required",
//                    Toast.LENGTH_LONG
//                ).show()
                finish()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CallRejectEventAudio) {
        Log.d("event", "CallRejectEvent")
        Log.d("Endssssss", "onFinish:CallRejectEventAudio ")
        finishActivity()
    }

    private fun finishActivity() {
        Log.d("Endssssss", "onFinish:finishActivity ")
        mPlayer2?.stop()
        engine.leaveChannel()
        finish()
        //startActivity(Intent(context, SearchUserActivity::class.java))
    }
}