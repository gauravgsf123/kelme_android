package com.kelme.activity.chat

import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.adapter.GroupVideoCallAdapter
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityVideoCallBinding
import com.kelme.event.CallRejectEventVideo
import com.kelme.fragment.chat.ChatViewModal
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

class VideoCallActivity : BaseActivity(),GroupVideoCallAdapter.OnItemClick, View.OnClickListener {
    private lateinit var binding:ActivityVideoCallBinding
    private var mLocalVideo: VideoCanvas? = null
    private var mRemoteVideo: VideoCanvas? = null
    private var mRtcEngine: RtcEngine?=null
    private val mCallEnd = false
    private var mMuted = false
    private lateinit var viewModal: ChatViewModal
    private var agoraToken:String=""
    private var channelName = ""
    private var chatModel: ChatListModelWithName?=null
    private var callerType = ""
    private var firebaseModel: VoipNotificationResponse?=null
    private var isCallConnect = false
    private var mPlayer2: MediaPlayer?=null
    private var countdown_timer: CountDownTimer?=null
    private var videoIdList = ArrayList<Int>()
    val PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO
    )
    var startTime: Long = 0
    var timerHandler: Handler = Handler()
    private lateinit var groupCallAdapter: GroupVideoCallAdapter
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

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            runOnUiThread {
                mRtcEngine?.switchCamera()
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread{
                Log.i(TAG, "onUserJoined->$uid")
                videoIdList.add(uid)
                setupRemoteVideo(uid)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Log.d(TAG, "onUserOffline:UID  $uid")
                if(videoIdList.size>1) {
                    videoIdList.remove(uid)
                    groupCallAdapter.updateGroupCallItems(videoIdList, mRtcEngine!!)
                }
                else {
                    onRemoteUserLeft(uid)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_video_call)
        viewModal = ViewModelProvider(this, ViewModalFactory(application)).get(ChatViewModal::class.java)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding.ivAdd.setOnClickListener(this)
        EventBus.getDefault().register(this)
        if(PermissionUtil.hasPermissions(this, *PERMISSIONS))
            initializeEngine()
        setObserver()

    }

    private fun initializeEngine() {

        try {
            val appId = getString(R.string.agora_app_id)
            mRtcEngine = RtcEngine.create(
                applicationContext,
                appId,
                mRtcEventHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
            onBackPressed()
        }
        init()
    }

    private fun init(){

        callerType = intent.getStringExtra(Constants.CALLER_TYPE)!!
        when (callerType) {
            Constants.CallerType.RECEIVER -> {
                firebaseModel = intent.getParcelableExtra(Constants.FIREBASE_RESPONSE)!!
                channelName = firebaseModel?.channel_name!!
                agoraToken = firebaseModel?.token!!
                callAPI()
                Log.d("channelName",channelName)
            }
            Constants.CallerType.CALLER -> {
                chatModel = intent.getParcelableExtra(Constants.CHATLIST_MODEL)!!
                channelName = createChannelName()
                callAPI()
            }
        }

        setupVideoConfig()
        setupLocalVideo()
        setGroupCallAdapter()
    }

    private fun createChannelName():String{
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        return "${chatModel!!.chatId}_kelme_${dateFormat.format(date)}"
    }

    override fun initializerControl() {

    }

    private fun callAPI(){
        val request = GetTokenAgoraRequest(channelName,
            PrefManager.read(PrefManager.USER_ID,""))
        viewModal.getTokenAgora(request)
    }

    private fun setObserver(){
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
                else -> {
                    // handle null or unexpected case if needed
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
                        var firebaseList = mutableListOf<String>()
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
                            firebaseList,
                            PrefManager.read(PrefManager.USER_ID, "").toInt(),
                            agoraToken,
                            Constants.CallType.VIDEO,
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
                else -> {
                    // handle null or unexpected case if needed
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
                    if (response.message == "240") {
                        viewModal.logout()
                    } else if (response.message == "201") {
                        //  showLongToast(response.message)
                    } else {
                        //  showLongToast(response.message)
                    }
                }
                else -> {
                    // handle null or unexpected case if needed
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
                        val request =
                            GetVoipTokenRequest(
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
                else -> {
                    // handle null or unexpected case if needed
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
                else -> {
                    // handle null or unexpected case if needed
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
                else -> {
                    // handle null or unexpected case if needed
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
                else -> {
                    // handle null or unexpected case if needed
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
                    mRtcEngine?.leaveChannel()
                    endCall()
                    //showLongToast(resources.getString(R.string.user_not_response_your_call))
                }
            }

            override fun onTick(p0: Long) {
                Log.d("countDownTimer",p0.toString())
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
                Log.d("firebaseList",it.key)
                if(it.key!=uid) {
                    firebaseList.add(it.key)// = listOf(it.key) as MutableList<String>
                }
            }
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
            Constants.CallReject.SENDER,"group", firebaseList.size.toString()
        )
        viewModal.callEnd(request)
    }

    private fun setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine!!.enableVideo()

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine!!.setVideoEncoderConfiguration(
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
        mRtcEngine!!.setupLocalVideo(mLocalVideo)
        mRtcEngine?.switchCamera()
    }

    private fun joinChannel(channelName: String, agoraToken: String) {
        Log.d("channelId", channelName)
        Log.d("accessToken", agoraToken)
        val res = mRtcEngine?.joinChannel(agoraToken, channelName, "Extra Optional Data", PrefManager.read(PrefManager.USER_ID,"").toInt())

        if (callerType == Constants.CallerType.CALLER) {
            if (res!! < 0) {
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
            if (res !!< 0) {
                val receiverId=Utils.getReceiverId(firebaseModel!!)
                val request = OtherUserJoinRejectCallRequest(
                    channelName,
                    arrayListOf(receiverId),
                    PrefManager.read(PrefManager.USER_ID, ""),
                    firebaseModel!!.token,
                    Constants.CallType.VIDEO,
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

    private fun setupRemoteVideo(uid: Int) {
        binding.rvGroupVideoCall.visibility=View.VISIBLE
        var parent: ViewGroup = binding.remoteVideoViewContainer
        if (parent.indexOfChild(mLocalVideo!!.view) > -1) {
            parent = binding.localVideoViewContainer
            Log.d(TAG, "setupRemoteVideo: "+"if index of child")
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
        mRtcEngine?.setupRemoteVideo(mRemoteVideo)

        //set for group video call adapter
        groupCallAdapter.updateGroupCallItems(videoIdList, mRtcEngine!!)

        isCallConnect = true
        Log.i(TAG, "onUserJoined->$uid")
        countdown_timer?.cancel()
        mPlayer2?.stop()
        startTime = System.currentTimeMillis()
        timerHandler.postDelayed(timerRunnable, 0)

    }

    private fun setGroupCallAdapter() {
        groupCallAdapter = GroupVideoCallAdapter(baseContext, videoIdList,mRtcEngine!!,this)
        binding.rvGroupVideoCall.adapter = groupCallAdapter
    }

    private fun onRemoteUserLeft(uid: Int) {
        if (mRemoteVideo != null && mRemoteVideo!!.uid == uid) {
            removeFromParent(mRemoteVideo!!)
            // Destroys remote view
            endCall()
            mRemoteVideo = null
            mRtcEngine?.leaveChannel()
            timerHandler.removeCallbacks(timerRunnable)
            finish()
        }
    }

    fun onCallEndVideo(view:View){
        mRtcEngine?.leaveChannel()
        mPlayer2?.stop()
        endCall()
        finish()
    }

    fun onLocalAudioMuteClicked(view: View?) {
        mMuted = !mMuted
        // Stops/Resumes sending the local audio stream.
        mRtcEngine?.muteLocalAudioStream(mMuted)
        val res = if (mMuted) R.drawable.ic_mic_nute else R.drawable.ic_mic
        binding.btnMute.setImageResource(res)
    }

    fun onSwitchCameraClicked(view: View?) {
        // Switches between front and rear cameras.
        mRtcEngine?.switchCamera()
    }

    fun onLocalContainerClick(view: View?) {
        switchView(mLocalVideo!!)
        switchView(mRemoteVideo!!)
    }

    private fun switchView(canvas: VideoCanvas) {
        val parent: ViewGroup? = removeFromParent(canvas)
        if (parent === binding.localVideoViewContainer) {
            if (canvas.view is SurfaceView) {
                (canvas.view as SurfaceView).setZOrderMediaOverlay(false)
            }
            binding.remoteVideoViewContainer.addView(canvas.view)
        } else if (parent === binding.remoteVideoViewContainer) {
            if (canvas.view is SurfaceView) {
                (canvas.view as SurfaceView).setZOrderMediaOverlay(true)
            }
            binding.localVideoViewContainer.addView(canvas.view)
        }
    }

    private fun switchGroupCallView(canvas: VideoCanvas,frameLay: FrameLayout) {
        val parent: ViewGroup? = removeFromParent(canvas)
        if (parent === binding.localVideoViewContainer) {
            if (canvas.view is SurfaceView) {
                (canvas.view as SurfaceView).setZOrderMediaOverlay(false)
            }
            frameLay.addView(canvas.view)
        } else if (parent === frameLay) {
            if (canvas.view is SurfaceView) {
                (canvas.view as SurfaceView).setZOrderMediaOverlay(true)
            }
            binding.localVideoViewContainer.addView(canvas.view)
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CallRejectEventVideo) {
        Log.d("event", "CallRejectEvent")
        finishActivity()

    }

    private fun finishActivity() {
        mPlayer2?.stop()
        mRtcEngine?.leaveChannel()
        finish()
    }

    override fun onClick(mRemoteVideoStr: VideoCanvas, frameLay: FrameLayout) {
        //mRemoteVideo = mRemoteVideoStr
        switchGroupCallView(mLocalVideo!!,frameLay)
        switchGroupCallView(mRemoteVideoStr,frameLay)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivAdd.id -> {
                val intent = Intent(this, AddNewMemberInCallActivity::class.java)
                intent.putExtra("channel", channelName)
                intent.putExtra("agoratoken", agoraToken)
                intent.putExtra("callType",  Constants.CallType.VIDEO)
                Log.d(TAG, "onClick: channel $channelName")
                Log.d(TAG, "onClick: agoratoken $agoraToken")
                startActivity(intent)
            }
        }
    }
}