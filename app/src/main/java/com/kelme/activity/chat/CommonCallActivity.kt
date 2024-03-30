package com.kelme.activity.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.adapter.GroupVideoCallAdapter
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityCommonCallBinding
import com.kelme.event.CallRejectEventAudio
import com.kelme.event.CallRejectEventVideo
import com.kelme.fragment.chat.ChatViewModal
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.VoipNotificationResponse
import com.kelme.model.request.*
import com.kelme.model.response.ChatListModelWithName
import com.kelme.utils.*
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

class CommonCallActivity : BaseActivity(), GroupVideoCallAdapter.OnItemClick, View.OnClickListener {
    private lateinit var binding: ActivityCommonCallBinding
    private var mLocalVideo: VideoCanvas? = null
    private var mRemoteVideo: VideoCanvas? = null
    private lateinit var mRtcEngine: RtcEngine
    private var isMuted = true
    private var isSpeaker = true
    private val mCallEnd = false
    private var mMuted = false
    private lateinit var viewModal: ChatViewModal
    private var agoraToken: String = ""
    private var channelName = ""
    private var chatModel: ChatListModelWithName? = null
    private var callerType = ""
    private var firebaseModel: VoipNotificationResponse? = null
    private var isCallConnect = false
    private var mPlayer2: MediaPlayer? = null
    private var countdown_timer: CountDownTimer? = null
    private var videoIdList = ArrayList<Int>()
    private var myUid = 0
    private var joined = false
    var callerTypeAV = ""
    val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    var startTime: Long = 0
    var timerHandler: Handler = Handler()
    private lateinit var groupCallAdapter: GroupVideoCallAdapter
    var pm: PowerManager? = null
    var wl: PowerManager.WakeLock? = null
    var wl_cpu: PowerManager.WakeLock?=null
    var timerRunnable: Runnable = object : Runnable {
        override fun run() {
            val millis: Long = System.currentTimeMillis() - startTime
            var seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            seconds %= 60
            binding.tvTimerAudio.text = String.format("%02d:%02d", minutes, seconds)
            binding.tvTimerVideo.text = String.format("%02d:%02d", minutes, seconds)
            timerHandler.postDelayed(this, 500)
        }
    }

    private val mRtcEventHandlerVideo: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

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
//            if(err==17){
//               // stopEchoTest
//            }
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

//        override fun onRemoteVideoStateChanged(uid: Int, state: Int) {
//            super.onRemoteVideoStateChanged(uid, state)

//            if((state==1 || state==2) && id > 0) {
//            Log.d(TAG, "onRemoteVideoStateChanged:$uid State$state")
//                groupCallAdapter.updateBackground(uid,state)
//                groupCallAdapter.updateGroupCallItems(videoIdList,mRtcEngine)
//            }
//        }

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
            if (callerTypeAV == "audio") {
                Log.i(
                    TAG,
                    String.format("onJoinChannelSuccess channel %s uid %d", channel, uid)
                )
                // showLongToast(String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
                myUid = uid
                joined = true
                handler?.post {
                }
            } else {
                runOnUiThread {
                    // mRtcEngine.switchCamera()
                }
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.d(TAG, "onUserJoined: " + videoIdList.size)
            if (callerTypeAV == "audio") {
                isCallConnect = true
                Log.i(TAG, "onUserJoined->$uid")
                countdown_timer?.cancel()
                mPlayer2?.stop()

                //showLongToast(String.format("user %d joined!", uid))
                startTime = System.currentTimeMillis()
                timerHandler.postDelayed(timerRunnable, 0)
                videoIdList.add(uid)
                Log.e(TAG, "videoList IF->${videoIdList.size}")
                if (videoIdList.size > 2) {
                    binding.tvCallerName.text = (videoIdList.size + 1).toString() + " Members"
                    binding.tvMembers.text = (videoIdList.size + 1).toString() + " Members"
                }
            } else {
                wakeScreen()
                Log.d("Endssssss", "onFinish:onUserJoined " + videoIdList.size.toString())
                runOnUiThread {
                    Log.e(TAG, "onUserJoined->$uid")
                    Log.e(TAG, "videoList else->${videoIdList.size}")
                    videoIdList.add(uid)
                    setupRemoteVideo(uid)
                    if (videoIdList.size > 2) {
                        binding.tvCallerName.text = (videoIdList.size + 1).toString() + " Members"
                        binding.tvMembers.text = (videoIdList.size + 1).toString() + " Members"
                    }
                }
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            if (callerTypeAV == "audio") {
                Log.i(
                    TAG,
                    String.format("user %d offline! reason:%d", uid, reason)
                )
                timerHandler.removeCallbacks(timerRunnable)
                Log.d("Endssssss", "onFinish:onUserOffline ")
                finish()
                //showLongToast(String.format("user %d offline! reason:%d", uid, reason))
            } else {
                Log.d("Endssssss", "onFinish:onUserOfflineelse")
                runOnUiThread {
                    Log.d(TAG, "onUserOffline:UID  $uid")
                    if (videoIdList.size > 1) {
                        videoIdList.remove(uid)
                        groupCallAdapter.updateGroupCallItems(videoIdList, mRtcEngine)
                    } else {
                        onRemoteUserLeft(uid)
                    }
                }
            }
        }

        override fun onActiveSpeaker(uid: Int) {
            super.onActiveSpeaker(uid)
            Log.i(TAG, String.format("onActiveSpeaker:%d", uid))
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private fun wakeScreen() {

        var pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "TAG"
        )
        wakeLock.acquire()


        /*pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        //val isScreenOn: Boolean = pm.isScreenOn
        val isScreenOn = pm?.isInteractive
        Log.e("screen_on", "" + isScreenOn)
        if (!isScreenOn!!) {
            wl = pm?.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "myApp:MyLock"
            )
            //wl?.acquire(30000)
            //wl_cpu = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Kelme:mycpuMyCpuLock")
            wl?.acquire()
        }*/
    }

    private fun release(){
        val keyguardManager: KeyguardManager =
            applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val keyguardLock: KeyguardManager.KeyguardLock = keyguardManager.newKeyguardLock("TAG")
        keyguardLock.disableKeyguard()
        wl?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window: Window = window
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )


        wakeScreen()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_common_call)

        viewModal =
            ViewModelProvider(this, ViewModalFactory(application)).get(ChatViewModal::class.java)

        EventBus.getDefault().register(this)
        Log.d("ivPhone","ivPhone 01")
        callerTypeAV = intent.getStringExtra(Constants.CALL_TYPE_AV)!!
        if (callerTypeAV == "audio") {
            binding.cvAudio.visibility = View.VISIBLE
            binding.cvVideo.visibility = View.GONE
        } else {
            binding.cvAudio.visibility = View.GONE
            binding.cvVideo.visibility = View.VISIBLE
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (PermissionUtil.hasPermissions(this, *PERMISSIONS))
            initializeEngine()
        setObserver()

    }

    override fun initializerControl() {

    }

    private fun initializeEngine() {
        try {
            val appId = getString(R.string.agora_app_id)
            mRtcEngine = RtcEngine.create(
                applicationContext,
                appId,
                mRtcEventHandlerVideo
            )
        } catch (e: Exception) {
            e.printStackTrace()
            onBackPressed()
        }

        init()
    }

    /*override fun onBackPressed() {
        super.onBackPressed()
        if(mPlayer2!=null && mPlayer2!!.isPlaying)
            mPlayer2?.stop()
            mRtcEngine.leaveChannel()
        endCall()
    }*/

    private fun init() {
        onButtonClickListner()

        callerType = intent.getStringExtra(Constants.CALLER_TYPE)!!
        when (callerType) {
            Constants.CallerType.RECEIVER -> {
                firebaseModel = intent.getParcelableExtra(Constants.FIREBASE_RESPONSE)!!
                binding.tvMembers.text = firebaseModel!!.sender_name
                binding.tvCallerName.text = firebaseModel!!.sender_name
                if (callerTypeAV == "audio") {
                    channelName =
                        intent.getStringExtra(Constants.CALL_CHANNEL_NAME)!!//firebaseModel?.channel_name!!
                    Log.d(
                        "channelName:---------------XXXXX",
                        intent.getStringExtra(Constants.CALL_CHANNEL_NAME)!!
                    )
                    //agoraToken = firebaseModel?.token!!
                    callAPI()
                } else {
                    channelName = firebaseModel?.channel_name!!
                    agoraToken = firebaseModel?.token!!
                    callAPI()
                    Log.d("channelName", channelName)
                }
            }
            Constants.CallerType.CALLER -> {
                chatModel = intent.getParcelableExtra(Constants.CHATLIST_MODEL)!!
                channelName = createChannelName()
                binding.tvCallerName.text =
                    intent.getStringExtra(Constants.USER_NAME)//chatModel!!.name
                binding.tvMembers.text = intent.getStringExtra(Constants.USER_NAME)
                callAPI()
            }
        }

        if (callerTypeAV == "video") {
            setupVideoConfig()
            setupLocalVideo()
            setGroupCallAdapter()
        }
    }

    private fun onButtonClickListner() {
        binding.ivAddVideo.setOnClickListener(this)
        binding.ivAddAudio.setOnClickListener(this)
        binding.backArrow.setOnClickListener(this)
        binding.ivCall.setOnClickListener(this)
        binding.ivMic.setOnClickListener(this)
        binding.ivSpeaker.setOnClickListener(this)
        binding.ivSwapAudio.setOnClickListener(this)
        binding.ivSwapVideo.setOnClickListener(this)
    }

    private fun createChannelName(): String {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        return "${chatModel!!.chatId}_kelme_${dateFormat.format(date)}"
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
                        agoraToken = response.data?.token.toString()
                        response.data?.let { showLog(agoraToken) }
                        joinChannel(channelName, agoraToken)
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

        viewModal.getVoipTokenResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        val firebaseList = mutableListOf<String>()
                        response.data?.let { showLog(it) }
                        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
                        val filteredKeysMap = chatModel?.chatMembers?.filterKeys { it != uid }
                        chatModel?.chatMembers?.forEach {
                            Log.d("firebaseList", it.key)
                            firebaseList.add(it.key)// = listOf(it.key) as MutableList<String>
                        }
                        firebaseList.remove(uid)
                        if (callerTypeAV == "video") {
                            val request = OtherUserRequestCallRequest(
                                channelName,
                                firebaseList,
                                PrefManager.read(PrefManager.USER_ID, "").toInt(),
                                agoraToken,
                                Constants.CallType.VIDEO,
                                Constants.ChatType.SINGLE
                            )
                            viewModal.otherUserRequestCall(request)
                        } else {
                            val request = OtherUserRequestCallRequest(
                                channelName,
                                firebaseList,
                                PrefManager.read(PrefManager.USER_ID, "").toInt(),
                                agoraToken,
                                Constants.CallType.AUDIO,
                                Constants.ChatType.SINGLE
                            )
                            viewModal.otherUserRequestCall(request)
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
                    // ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else if (response.message == "201") {
                        //  showLongToast(response.message)
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
                        wakeScreen()
                        ProgressDialog.hideProgressBar()
                        //agoraToken = response.data?.token.toString()
                        countDownTimer()
                        val request = GetVoipTokenRequest(
                            agoraToken,
                            PrefManager.read(PrefManager.USER_ID, "")
                        )
                        viewModal.getVoipToken(request)
                        // showLog(chatModel.UserName.toString())
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

        viewModal.otherUserRequestCallResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        //agoraToken = response.data?.token.toString()
                        response.data?.let { showLog(response.data.message) }
                        //  Toast.makeText(this, "" + response.message, Toast.LENGTH_SHORT).show()
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
                        //      showLongToast(response.message)
                    } else {
                        //     showLongToast(response.message)
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
                    //showLongToast(response.message)
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
                if (!isCallConnect) {
                    countdown_timer?.cancel()
                    mPlayer2?.stop()
                    mRtcEngine.leaveChannel()
                    endCall()
                    //showLongToast(resources.getString(R.string.user_not_response_your_call))
                }
            }

            override fun onTick(p0: Long) {
                Log.d("countDownTimer", p0.toString())
            }
        }
        countdown_timer?.start()
    }

    private fun endCall() {
        countdown_timer?.cancel()
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val firebaseList = mutableListOf<String>()
        if (chatModel != null) {
            chatModel!!.chatMembers.forEach {
                Log.d("firebaseList", it.key)
                if (it.key != uid) {
                    firebaseList.add(it.key)// = listOf(it.key) as MutableList<String>
                }
            }
        } else {
            if (uid != firebaseModel?.receiver_id) {
                firebaseModel?.receiver_id?.let {
                    firebaseList.add(it)
                    Log.d(TAG, "endCall: list$it")
                }//firebaseModel?.receiver_id
            } else {
                firebaseModel?.sender_id?.let {
                    firebaseList.add(it)
                    Log.d(TAG, "endCall: list$it")
                }//firebaseModel?.receiver_id
            }
        }
        if (callerTypeAV == "audio") {
            val request = CallEndRequest(
                channelName,
                firebaseList,//listOf(firebaseId) as List<String>,
                PrefManager.read(PrefManager.USER_ID, ""),
                agoraToken,
                Constants.CallType.AUDIO,
//            Constants.ChatType.SINGLE,
                Constants.CallReject.SENDER, "Audio", (videoIdList.size + 1).toString()
            )
            viewModal.callEnd(request)
            mRtcEngine.leaveChannel()
        } else {
            val request = CallEndRequest(
                channelName,
                firebaseList,//listOf(firebaseId) as List<String>,
                PrefManager.read(PrefManager.USER_ID, ""),
                agoraToken,
                Constants.CallType.AUDIO,
//            Constants.ChatType.SINGLE,
                Constants.CallReject.SENDER, "Video", (videoIdList.size + 1).toString()
            )
            viewModal.callEnd(request)
        }
        finish()
    }

    private fun setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo()

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun setupLocalVideo() {
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        val view = RtcEngine.CreateRendererView(baseContext)
        view.setZOrderMediaOverlay(true)
        binding.remoteVideoViewContainer.addView(view)
        binding.localVideoViewContainer.visibility = View.GONE
        mLocalVideo = VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0)
        mRtcEngine.setupLocalVideo(mLocalVideo)
        // mRtcEngine.switchCamera()
    }

    private fun joinChannel(channelName: String, agoraToken: String) {
        Log.d("channelId", channelName)
        Log.d("accessToken", agoraToken)
        var res = 0
        if (callerTypeAV == "audio") {
            mRtcEngine.enableAudioVolumeIndication(1000, 3, true)
            mRtcEngine.setEnableSpeakerphone(!isSpeaker)
            mRtcEngine.muteLocalAudioStream(!isMuted)
            if (callerType == Constants.CallerType.CALLER) {
                res = mRtcEngine.joinChannel(
                    agoraToken,
                    channelName,
                    "",
                    PrefManager.read(PrefManager.USER_ID, "").toInt()
                )
                Log.d("joinChannel", res.toString())
            } else {
                startTime = System.currentTimeMillis()
                timerHandler.postDelayed(timerRunnable, 0)
                res = mRtcEngine.joinChannel(
                    agoraToken,
                    channelName,
                    "", PrefManager.read(PrefManager.USER_ID, "").toInt()
                )
                Log.d("joinChannel", res.toString())
            }
        } else {
            res = mRtcEngine.joinChannel(
                agoraToken,
                channelName,
                "Extra Optional Data",
                PrefManager.read(PrefManager.USER_ID, "").toInt()
            )
        }
        Log.d("mRtcEngine", res.toString())
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
        } else {
            /*if (res < 0) {
                val receiverId=Utils.getReceiverId(firebaseModel!!)
                if(callerTypeAV=="audio") {
                    val request = OtherUserJoinRejectCallRequest(
                        channelName,
                        arrayListOf(receiverId),
                        PrefManager.read(PrefManager.USER_ID, ""),
                        firebaseModel!!.token,
                        Constants.CallType.AUDIO,
                        Constants.CallReject.SENDER
                    )
                    viewModal.otherUserjoinRejectCall(request)
                }else{
                    val request = OtherUserJoinRejectCallRequest(
                        channelName,
                        arrayListOf(receiverId),
                        PrefManager.read(PrefManager.USER_ID, ""),
                        firebaseModel!!.token,
                        Constants.CallType.VIDEO,
                        Constants.CallReject.SENDER
                    )
                    viewModal.otherUserjoinRejectCall(request)
                }
                return
            } else {*/
            val receiverId = Utils.getReceiverId(firebaseModel!!)
            val request = OtherUserJoinCallSuccessfullyRequest(
                PrefManager.read(PrefManager.USER_ID, ""),
                channelName,
                arrayListOf(receiverId)
            )
            viewModal.otherUserJoinCallSuccessfully(request)
            // }
        }
    }

    private fun setupRemoteVideo(uid: Int) {

        var parent: ViewGroup
        binding.rvGroupVideoCall.visibility = View.VISIBLE
        parent = binding.remoteVideoViewContainer
        if (parent.indexOfChild(mLocalVideo!!.view) > -1) {
            parent = binding.localVideoViewContainer
            Log.d(TAG, "setupRemoteVideo: " + "if index of child")
        }
        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
//        if (mRemoteVideo != null) {
//            return
//        }
        //  binding.localVideoViewContainer.visibility = View.VISIBLE
        /*switchView(mLocalVideo!!)
        switchView(mRemoteVideo!!)*/
        /*
          Creates the video renderer view.
          CreateRendererView returns the SurfaceView type. The operation and layout of the view
          are managed by the app, and the Agora SDK renders the view provided by the app.
          The video display view must be created using this method instead of directly
          calling SurfaceView.
         */
        val view = RtcEngine.CreateRendererView(baseContext)
        view.setZOrderMediaOverlay(parent === binding.localVideoViewContainer)
        parent.addView(view)
        mRemoteVideo = VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid)
        // Initializes the video view of a remote user.
        mRtcEngine.setupRemoteVideo(mRemoteVideo)

        //set for group video call adapter
        groupCallAdapter.updateGroupCallItems(videoIdList, mRtcEngine)

        isCallConnect = true
        Log.i(TAG, "onUserJoined->$uid")
        countdown_timer?.cancel()
        mPlayer2?.stop()
        startTime = System.currentTimeMillis()
        timerHandler.postDelayed(timerRunnable, 0)
    }

    private fun setGroupCallAdapter() {
        groupCallAdapter = GroupVideoCallAdapter(baseContext, videoIdList, mRtcEngine, this)
        binding.rvGroupVideoCall.adapter = groupCallAdapter

        groupCallAdapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
//               onLocalContainerClick(position,view)
//                adminMembersList.forEach {
//                    if (it.isSelected) {
//                        it.isSelected = false
//                    }
//                }
//                adminMembersList[position].isSelected = true
//                adminAdapter.updateItems(adminMembersList)
            }
        })
    }

    private fun onRemoteUserLeft(uid: Int) {
        if (mRemoteVideo != null && mRemoteVideo!!.uid == uid) {
            removeFromParent(mRemoteVideo!!)
            // Destroys remote view
            endCall()
            mRemoteVideo = null
            mRtcEngine.leaveChannel()
            timerHandler.removeCallbacks(timerRunnable)
            finish()
        }
    }

    fun onCallEnd(view: View) {
        mRtcEngine.leaveChannel()
        mPlayer2?.stop()
        Log.e(TAG, "onCallEnd: videolistsize " + videoIdList.size)
        if (videoIdList.size < 2 || videoIdList.size == null) {
            endCall()
            finish()
        } else {
            return
        }
    }

    fun onLocalAudioMuteClicked(view: View?) {
        mMuted = !mMuted
        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(mMuted)
        val res = if (mMuted) R.drawable.ic_mic_nute else R.drawable.ic_mic
        binding.btnMute.setImageResource(res)
    }

    fun onSwitchCameraClicked(view: View?) {
        // Switches between front and rear cameras.
        mRtcEngine.switchCamera()
    }

    fun onLocalContainerClick(position: Int, view: View?) {
        switchView(mLocalVideo!!, position, view)
        switchView(mRemoteVideo!!, position, view)
    }

    private fun switchView(canvas: VideoCanvas, position: Int, view: View?) {
        val parent: ViewGroup? = removeFromParent(canvas)
        if (parent === binding.rvGroupVideoCall[position]) {
            if (canvas.view is SurfaceView) {
                (canvas.view as SurfaceView).setZOrderMediaOverlay(false)
            }
            binding.remoteVideoViewContainer.addView(canvas.view)
        } else if (parent === binding.remoteVideoViewContainer) {
            if (canvas.view is SurfaceView) {
                (canvas.view as SurfaceView).setZOrderMediaOverlay(true)
            }
            // groupCallAdapter.get[position].addView(canvas.view)
        }
    }

    private fun switchGroupCallView(canvas: VideoCanvas, frameLay: FrameLayout) {
        val parent: ViewGroup? = removeFromParent(canvas)
        if (parent === frameLay) {
            if (canvas.view is SurfaceView) {
                (canvas.view as SurfaceView).setZOrderMediaOverlay(false)
            }
            binding.remoteVideoViewContainer.addView(canvas.view)
            Log.d("FGFFG", "LOCAL")
        } else if (parent === binding.remoteVideoViewContainer) {
            if (canvas.view is SurfaceView) {
                (canvas.view as SurfaceView).setZOrderMediaOverlay(true)
            }
            frameLay.addView(canvas.view)
            Log.d("FGFFG", "REMOTE")
        }
    }

    private fun showButtons(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        //mMuteBtn.setVisibility(visibility)
        //mSwitchCameraBtn.setVisibility(visibility)
    }

    private fun removeFromParent(canvas: VideoCanvas): ViewGroup? {
        if (canvas != null) {
            val parent = canvas.view.parent
            if (parent != null) {
                val group = parent as ViewGroup
                group.removeView(canvas.view)
                return group
            }
        }
        return null
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        //------------------------------------------------------
//        EventBus.getDefault().unregister(this)
//        //if(mPlayer2!=null && mPlayer2.isPlaying)
//        mPlayer2?.stop()
//        mRtcEngine.leaveChannel()
        //if(!isCallConnect){

        //showLongToast(resources.getString(R.string.user_not_response_your_call))
        // }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CallRejectEventVideo) {
        Log.d("event", "CallRejectEvent")
        finishActivity()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CallRejectEventAudio) {
        Log.d("event", "CallRejectEvent")
        finishActivity()
    }

    private fun finishActivity() {
        mPlayer2?.stop()
        mRtcEngine.leaveChannel()
        release()
        finish()
    }

    override fun onClick(mRemoteVideoStr: VideoCanvas, frameLay: FrameLayout) {
        //mRemoteVideo = mRemoteVideoStr
        switchGroupCallView(mLocalVideo!!, frameLay)
        switchGroupCallView(mRemoteVideoStr, frameLay)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivCall.id -> {
                countdown_timer?.cancel()
                mPlayer2?.stop()
                endCall()
            }
            binding.ivMic.id -> {
                val res = if (isMuted) R.drawable.ic_mic else R.drawable.ic_mic_nute
                binding.ivMic.setImageResource(res)
                mRtcEngine.muteLocalAudioStream(!isMuted)
                isMuted = !isMuted
            }
            binding.ivSpeaker.id -> {
                val res = if (isSpeaker) R.drawable.ic_speaker_off else R.drawable.ic_speaker
                binding.ivSpeaker.setImageResource(res)
                mRtcEngine.setEnableSpeakerphone(!isSpeaker)
                isSpeaker = !isSpeaker
            }

            binding.ivAddVideo.id -> {
                val intent = Intent(this, AddNewMemberInCallActivity::class.java)
                intent.putExtra("channel", channelName)
                intent.putExtra("agoratoken", agoraToken)
                intent.putExtra("callType", Constants.CallType.VIDEO)
                startActivity(intent)
            }
            binding.ivAddAudio.id -> {
                val intent = Intent(this, AddNewMemberInCallActivity::class.java)
                intent.putExtra("channel", channelName)
                intent.putExtra("agoratoken", agoraToken)
                intent.putExtra("callType", Constants.CallType.AUDIO)
                startActivity(intent)
            }
            binding.ivSwapAudio.id -> {
                binding.cvAudio.visibility = View.VISIBLE
                binding.cvVideo.visibility = View.GONE
                binding.rvGroupVideoCall.visibility = View.GONE
                mRtcEngine.disableVideo()
            }
            binding.ivSwapVideo.id -> {
                // mRtcEngine.enableVideo()
                binding.cvAudio.visibility = View.GONE
                binding.cvVideo.visibility = View.VISIBLE
                setupVideoConfig()
                setupLocalVideo()
                setGroupCallAdapter()
                binding.rvGroupVideoCall.visibility = View.VISIBLE
                Log.d(TAG, "onClick: " + videoIdList.size)
                groupCallAdapter.updateGroupCallItems(videoIdList, mRtcEngine)
            }
//            binding.ivSwapVideo.id ->{
//                // mRtcEngine.enableVideo()
//                binding.cvAudio.visibility=View.GONE
//                binding.cvVideo.visibility=View.VISIBLE
//                setupVideoConfig()
//                setupLocalVideo()
//                setGroupCallAdapter()
//                binding.rvGroupVideoCall.visibility=View.VISIBLE
//                Log.d(TAG, "onClick: "+videoIdList.size)
//                groupCallAdapter.updateGroupCallItems(videoIdList,mRtcEngine)
//            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }
}