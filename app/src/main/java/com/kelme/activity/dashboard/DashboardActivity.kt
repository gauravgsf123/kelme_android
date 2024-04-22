package com.kelme.activity.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kelme.MyBroadcastReceiver
import com.kelme.R
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.NavMenuAdapter
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityDashboardBinding
import com.kelme.databinding.PopupLogoutBinding
import com.kelme.databinding.PopupSosAlertBinding
import com.kelme.event.*
import com.kelme.fragment.*
import com.kelme.fragment.chat.ChatListFragment
import com.kelme.fragment.country.CountryOutlookFragment
import com.kelme.fragment.profile.ProfileFragment
import com.kelme.fragment.security.SecurityAlertsFragment
import com.kelme.interfaces.MyLocationCallback
import com.kelme.model.ContactModel
import com.kelme.model.request.SosAlertRequest
import com.kelme.model.response.ChatListModelWithName
import com.kelme.services.LocationService
import com.kelme.utils.*
import de.hdodenhof.circleimageview.CircleImageView
import org.greenrobot.eventbus.EventBus

class DashboardActivity : BaseActivity() {

    lateinit var latLng1: LatLng
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModal: DashboardViewModal
    private lateinit var navMenuAdapter: NavMenuAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var latitude = ""
    private var longitude = ""
    private var street = ""
    private lateinit var profileImage: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvCountry: TextView
    private var countDownTimer: CountDownTimer? = null
    private var timeSwapBuff = 10L
    private var isUpOnce = true
    var userGetId =""
    var unSeenMsgCount = 0
    private var dialog: AlertDialog?=null
    private var myLocationCallback: MyLocationCallback? = null
    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    var mService: LocationService? = null
    // Boolean to check if our activity is bound to service or not
    var mIsBound: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView( this, R.layout.activity_dashboard)
        val myList = intent.getSerializableExtra("mylist") as ArrayList<ChatListModelWithName>?
        var startPageNumber =""
        if ( savedInstanceState != null) {
            startPageNumber = savedInstanceState.getString("value").toString()
            Log.e(TAG, "onCreate: page num .....$startPageNumber")
        }
        viewModal = ViewModelProvider(
            this, ViewModalFactory(application)
        ).get(DashboardViewModal::class.java)
        showOrHideCount()
        // val extras: Bundle? = intent.extras
        userGetId = intent.getStringExtra("value").toString()
        val chatId: String = intent.getStringExtra("chatId").toString()
        PrefManager.write(PrefManager.FROMNOTIFICATION, userGetId)
        PrefManager.write(PrefManager.FROMNOTIFICATIONCHATID, chatId)
        // Log.e(TAG, "onCreate: CHATID "+ PrefManager.read(PrefManager.FROMNOTIFICATION,"chatId"))

        val intentFilter = IntentFilter()
        intentFilter.addAction( "com.kelme.MY_SIGNAL")

        val myReceiver = MyBroadcastReceiver()
        registerReceiver(myReceiver, intentFilter)

        val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                // Get extra data included in the Intent
                val value = intent.getStringExtra("value")
                val chatId = intent.getStringExtra("chatId")
                Log.e(TAG, "onReceive: >>>>>>$value-------$chatId")

                val data = PrefManager.readUnreadData(PrefManager.MESSAGECOUNT)
                data?.add(chatId!!)
                data?.let { PrefManager.writeUnReadData(PrefManager.MESSAGECOUNT, it) }
                //val unSeenMsgCountString=binding.tvUnreadChatCount.text.toString()
                //unSeenMsgCount= unSeenMsgCountString.toInt()+1
                //PrefManager.write(PrefManager.UNSEENMSGCOUNT,unSeenMsgCount.toString())
                //binding.tvUnreadChatCount.text = PrefManager.read(PrefManager.UNSEENMSGCOUNT,"")
                showOrHideCount()
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
            IntentFilter("NotificationUpdates"))

        if(!checkLocationPermisionGranted()){
            showDisclaimer()
        }else{
            getCurrentLocation()
        }
        setUI()
        setObserver()
        retrieveChatList()
    }

    private fun checkLocationPermisionGranted():Boolean{
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {

                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        } else {
            //checkBackgroundLocation()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            155
        )
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                156
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                155
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
//        LocalBroadcastManager.getInstance(this)
//            .unregisterReceiver(broadcastReceiver)
    }


    private fun gotoNotification() {
        Log.e(TAG, "gotoNotification: OnResume" )
        if (PrefManager.read(PrefManager.FROMNOTIFICATION, "0") == "1"||
            PrefManager.read(PrefManager.FROMNOTIFICATION, "0").equals(1)) {
            PrefManager.write(PrefManager.FROMNOTIFICATION, "0")
            replaceFragment(NotificationFragment(), Bundle.EMPTY)
        }
        else if(PrefManager.read(PrefManager.FROMNOTIFICATION, "0") == "2" ||
            PrefManager.read(PrefManager.FROMNOTIFICATION, "0").equals(2)){
            replaceFragment(ChatListFragment(), Bundle.EMPTY)
        }
    }

    private fun retrieveChatList() {

        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val query = FirebaseDatabase.getInstance().getReference("users")

        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Constants.userList.clear()
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val user = ContactModel()
                        val deviceToken = userSnapshot.child("deviceToken").getValue(String::class.java)
                        val deviceType =
                            userSnapshot.child("deviceType").getValue(String::class.java)
                        val email = userSnapshot.child("email").getValue(String::class.java)
                        val isDeleted = userSnapshot.child("isDeleted").getValue(Boolean::class.java)
                        val isNotificationOn = userSnapshot.child("isNotificationOn").getValue(Boolean::class.java)
                        val isOnline = userSnapshot.child("isOnline").getValue(Boolean::class.java)
                        val lastSeen = userSnapshot.child("lastSeen").getValue(Long::class.java)
                        val name = userSnapshot.child("name").getValue(String::class.java)
                        val profilePicture = userSnapshot.child("profilePicture").getValue(String::class.java)
                        val userId = userSnapshot.child("userId").getValue(String::class.java)
                        val userRole = userSnapshot.child("userRole").getValue(Long::class.java)
                        user.deviceToken = deviceToken
                        user.deviceType = deviceType
                        user.email = email
                        user.isDeleted = isDeleted
                        user.isNotificationOn = isNotificationOn
                        user.isOnline = isOnline
                        user.lastSeen = lastSeen
                        user.name = name
                        user.profilePicture = profilePicture
                        user.userId = userId
                        user.userRole = userRole
                        if(uid != user.userId) {
                            Constants.userList.add(user)
                        }
                    }
                } else{
                    Log.d(TAG, "onDataChange: no data found")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    fun setLocationCallback(locationCallback: MyLocationCallback)
    {
        this.myLocationCallback = locationCallback
        startLocationUpdates()
    }


    override fun onResume() {
        super.onResume()
        // unreadNotificationCount()
        if (!isLocationEnabled(this@DashboardActivity)) {
            enableLocationSettings()
        }
        gotoNotification()
        //bindService()
        Handler().postDelayed({
            //if(!LocationService().isInstanceCreated()){
            Log.i("isMyServiceRunning","${isMyServiceRunning(LocationService::class.java)}")
            if(!isMyServiceRunning(LocationService::class.java)){
                Log.i("isRunning","service not Running")
                stopLocationService()
                ContextCompat.startForegroundService(this, Intent(this, LocationService::class.java))
            }else(
                    Log.i("isRunning","service is Running")
                    )
        }, 2000)

    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            Log.d("serviceConnection", "ServiceConnection: connected to service.")
            // We've bound to MyService, cast the IBinder and get MyBinder instance
            val binder = iBinder as LocationService.MyBinder
            mService = binder.service
            mIsBound = true
            //getRandomNumberFromService() // return a random number from the service
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("serviceConnection", "ServiceConnection: disconnected from service.")
            mIsBound = false
        }
    }

    private fun getRandomNumberFromService() {
        /*mService?.randomNumberLiveData?.observe(this
            , Observer {
                resultTextView?.text = "Random number from service: $it"
            })*/
    }

    private fun bindService() {
        Intent(this, LocationService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        Intent(this, LocationService::class.java).also { intent ->
            unbindService(serviceConnection)
        }
    }

    private fun showDisclaimer(){
        var builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.disclaimer))
        builder.setMessage(resources.getString(R.string.disclaimer_message))
        builder.setPositiveButton(getString(R.string.ok)) { dialog, i ->
            checkLocationPermission()
            getCurrentLocation()

            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.dont_allow), null)
        dialog = builder.create()

        dialog?.setOnShowListener {
            dialog?.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(getColor(R.color.white))
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(R.color.white))
        }

        dialog?.show()
    }


    override fun initializerControl() {
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUI() {

        setNegativeToolbarPadding()

        val menuList: Array<String> = resources.getStringArray(R.array.menu_list)
        navMenuAdapter = NavMenuAdapter(this, menuList)

        val view = binding.navView.getHeaderView(0)
        //val binding2:NavHeaderViewBinding = DataBindingUtil.inflate(this@DashboardActivity, view)
        profileImage = view.findViewById(R.id.profileImage)
        val editProfile = view.findViewById<CircleImageView>(R.id.editProfile)
        editProfile.setOnClickListener {
            replaceFragment(ProfileFragment(), Bundle.EMPTY)
        }
        //if (!PermissionUtil.hasPermissions(this, *PERMISSIONS)) requestPermission(this)
        tvName = view.findViewById(R.id.tvName)
        tvName.text = PrefManager.read(PrefManager.NAME, "")

        tvCountry = view.findViewById(R.id.tvCountry)
        tvCountry.text = PrefManager.read(PrefManager.COUNTRY_NAME, "")

        Utils.loadImage(
            this@DashboardActivity,
            profileImage,
            PrefManager.read(PrefManager.IMAGE, ""))

        binding.menuListView.adapter = navMenuAdapter
        binding.menuListView.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, position, id ->
                //val selectedItem = adapterView.getItemAtPosition(position) as String
                //val itemIdAtPos = adapterView.getItemIdAtPosition(position)
                Utils.hideKeyboard(this, binding.root)
                when (position) {
                    0 -> {
                        replaceFragment(HomeFragment(), Bundle.EMPTY)
                    }
                    1 -> {
                        replaceFragment(CountryOutlookFragment(), Bundle.EMPTY)
                    }
                    2 -> {
                        replaceFragment(SecurityAlertsFragment(), Bundle.EMPTY)
                    }
                    3 -> {
                        replaceFragment(MapsFragment(), Bundle.EMPTY)
                    }
                    4 -> {
                        replaceFragment(ContactFragment(), Bundle.EMPTY)
                    }
                    5 -> {
                        replaceFragment(ChatListFragment(), Bundle.EMPTY)
                    }
                    6 -> {
                        replaceFragment(CheckInFragment(), Bundle.EMPTY)
                    }
                    7 -> {
                        replaceFragment(NotificationFragment(), Bundle.EMPTY)
                    }
                    8 -> {
                        if (binding.drawer.isDrawerOpen(GravityCompat.START))
                        {
                            binding.drawer.closeDrawer(GravityCompat.START)
                        }
                        popupLogout(binding.root)
                    }
                }
            }

        Log.e(TAG,PrefManager.read(PrefManager.FCM_TOKEN, ""))

        binding.tvChatSearch.setOnClickListener {
            replaceFragment(ChatListFragment(), Bundle.EMPTY)
        }

        binding.ivSearch.setOnClickListener {
            replaceFragment(ChatListFragment(), Bundle.EMPTY)
        }

        binding.ivSearchUser.setOnClickListener {
            EventBus.getDefault().post(SearchChatUserEvent(binding.etChatSearch.text.toString()))
        }

        binding.menu.setOnClickListener {
            binding.drawer.openDrawer(GravityCompat.START)
        }

//        binding.backArrow1.setOnClickListener {
//            replaceFragment(HomeFragment(), Bundle.EMPTY)
//        }

        binding.tvTitle.setOnClickListener {
            replaceFragment(HomeFragment(), Bundle.EMPTY)
        }

        binding.tvProfile.setOnClickListener {
            replaceFragment(ProfileFragment(), Bundle.EMPTY)
        }

        binding.tvSetting.setOnClickListener {
            replaceFragment(SettingFragment(), Bundle.EMPTY)
        }

        binding.ivNotification.setOnClickListener {
            replaceFragment(NotificationFragment(), Bundle.EMPTY)
        }

        binding.backArrow.setOnClickListener {
            onBackPressed()
        }

        binding.ivFilter.setOnClickListener {
            EventBus.getDefault().post(FilterEvent())
        }

        binding.btnClear.setOnClickListener {
            EventBus.getDefault().post(ClearEvent())
        }

        binding.imgAlert.setOnClickListener {
            binding.imgAlert.setColorFilter(
                ContextCompat.getColor(this, R.color.yellow),
                PorterDuff.Mode.MULTIPLY
            )
            binding.imgTracking.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                PorterDuff.Mode.MULTIPLY
            )
            EventBus.getDefault().post(AlertEvent())
        }

        binding.imgTracking.setOnClickListener {

//            if(PrefManager.read(PrefManager.USERROLE, "")=="employee"){
//                Toast.makeText(this, "You are employee so you cant trace tracking", Toast.LENGTH_SHORT).show()
//            }else {
            binding.imgAlert.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                PorterDuff.Mode.MULTIPLY
            )
            binding.imgTracking.setColorFilter(
                ContextCompat.getColor(this, R.color.yellow),
                PorterDuff.Mode.MULTIPLY
            )
            EventBus.getDefault().post(TrackingEvent())
            // }
        }

        binding.ivAddChatGroup.setOnClickListener {
            EventBus.getDefault().post(CreateChatGroupEvent())
            //startActivity(Intent(this@DashboardActivity,AudioCallActivity::class.java))
        }

        binding.ivDelete.setOnClickListener {
            EventBus.getDefault().post(DeleteChatEvent())
            //startActivity(Intent(this@DashboardActivity,AudioCallActivity::class.java))
        }

        binding.etChatSearch.setOnClickListener {
            Log.d("ivSearch","ivSearch")
            EventBus.getDefault().post(SearchChatUserEvent(binding.etChatSearch.text.toString()))
        }

        binding.btnSave.setOnClickListener {
            EventBus.getDefault().post(UpdateUserProfileEvent())
            EventBus.getDefault().post(SaveChatGroupEvent())
        }

        binding.imgSos.setOnTouchListener { v, event ->
            getCurrentLocation()
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startTimer()
                    Log.e("Action Down ", ">>>Down")
                    Glide.with(this@DashboardActivity)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .load(R.drawable.sos_alert_2_sec)
                        .into(binding.imgSos)
                }

                MotionEvent.ACTION_UP -> {
                    if (isUpOnce) {
                        stopTimer()
                        Log.e("Action up ", ">>>Up")
                        if (timeSwapBuff == 0L && isUpOnce) {
                            sendSosAlert()
                            vibratePhone()
                        }
                        isUpOnce = false
                    }
                    Glide.with(this@DashboardActivity)
                        .load(R.drawable.dashboard_sos)
                        .into(binding.imgSos)
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        binding.navView.itemIconTintList = null

        replaceFragment(HomeFragment(), Bundle.EMPTY)

        binding.drawer.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                Utils.hideKeyboard(this@DashboardActivity, binding.root)
                updateUserData()
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    private fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            PERMISSIONS,
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
        } else if (requestCode == 155) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Now check background location
                    requestBackgroundLocationPermission()
                }

            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                // Check if we are in a state where the user has denied the permission and
                // selected Don't ask again
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", this.packageName, null),
                        ),
                    )
                }
            }
            return
        } else if (requestCode == 156) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    ContextCompat.startForegroundService(this, Intent(this, LocationService::class.java))
//                    Toast.makeText(
//                        this,
//                        "Granted Background Location Permission",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                // Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
            }
            return
        }
    }

    fun vibratePhone() {
        val vibrator =
            this@DashboardActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(300)
        }
    }

    private fun startTimer() {
        isUpOnce = true
        countDownTimer = object : CountDownTimer(1800, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.e(TAG, ">>>>> $millisUntilFinished")
                timeSwapBuff = millisUntilFinished
                //updateTimerText()
            }

            override fun onFinish() {
                timeSwapBuff = 0L
                binding.imgSos.dispatchTouchEvent(
                    MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP,
                        0.0f,
                        0.0f,
                        0
                    )
                )
            }
        }.start()
    }

    private fun stopTimer() {
        if (countDownTimer != null)
            countDownTimer?.cancel()
    }


    fun setAppTopBackgroundColor(color: Int) {
        binding.clMain.setBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                color
            )
        )
    }

    fun removeAppTopBackgroundColor() {
        binding.clMain.setBackgroundColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.backgroundColor
            )
        )
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun showMenu() {
        binding.menu.visibility = View.VISIBLE
        binding.backArrow.visibility = View.GONE
    }

    fun showBackArrow() {
        binding.backArrow.visibility = View.VISIBLE
        binding.menu.visibility = View.INVISIBLE
    }

    fun changeSearchBarBackground(){
        binding.clSearchBar.setBackgroundResource(R.drawable.chat_search_box)
        binding.ivSearchUser.visibility=View.VISIBLE
        binding.ivSearch.visibility=View.GONE
        binding.tvChatSearch.visibility = View.GONE
        binding.etChatSearch.visibility = View.VISIBLE
        binding.etChatSearch.hint = resources.getString(R.string.search_user_and_group)
        binding.etChatSearch.setTextColor(resources.getColor(R.color.black))
        binding.etChatSearch.setHintTextColor(resources.getColor(R.color.gray))
    }

    fun resetSearchBarBackground(){
        binding.ivSearchUser.visibility=View.GONE
        binding.ivSearch.visibility=View.VISIBLE
        binding.clSearchBar.setBackgroundResource(R.drawable.chat_voice_background)
        binding.tvChatSearch.visibility = View.VISIBLE
        binding.etChatSearch.visibility = View.GONE
        binding.etChatSearch.text?.clear()
        binding.etChatSearch.hint = resources.getString(R.string.chat_voice)
        binding.etChatSearch.setHintTextColor(resources.getColor(R.color.white))
    }

    fun hideUnreadCount(){
        binding.tvUnreadCount.visibility=View.GONE
    }

    fun showUnreadCount(count:String){
        binding.tvUnreadCount.visibility=View.VISIBLE
        binding.tvUnreadCount.text=count
    }

    fun setNegativeToolbarPadding(){
        binding.tvTitle.compoundDrawablePadding=-28
    }

    fun showSearchBar() {
        binding.clSearchBar.visibility = View.VISIBLE
    }

    fun hideSearchBar() {
        binding.clSearchBar.visibility = View.GONE
    }

    fun hideAddChatGroupIcon() {
        binding.ivAddChatGroup.visibility = View.INVISIBLE
    }

    fun showAddChatGroupIcon() {
        binding.ivAddChatGroup.visibility = View.VISIBLE
    }

    fun hideDeleteChatIcon() {
        binding.ivDelete.visibility = View.INVISIBLE
    }

    fun showDeleteChatIcon() {
        binding.ivDelete.visibility = View.VISIBLE
    }

    fun hideNotificationIcon() {
        binding.ivNotification.visibility = View.INVISIBLE
    }

    fun showNotificationIcon() {
        binding.ivNotification.visibility = View.VISIBLE
        binding.btnSave.visibility = View.GONE
        binding.ivFilter.visibility = View.GONE
    }

    fun showFilter() {
        binding.ivFilter.visibility = View.VISIBLE
        binding.ivNotification.visibility = View.INVISIBLE
        binding.btnSave.visibility = View.GONE
    }

    fun hideFilter() {
        binding.ivFilter.visibility = View.GONE
    }

    fun showSaveBtn() {
        binding.btnSave.visibility = View.VISIBLE
        binding.ivNotification.visibility = View.INVISIBLE
    }

    fun showClearBtn() {
        binding.btnClear.visibility = View.VISIBLE
    }

    fun hideClearBtn() {
        binding.btnClear.visibility = View.GONE
    }

    fun showMapControl() {
        binding.clMapControl.visibility = View.VISIBLE
    }

    fun showOrHideCount(){
        val set = PrefManager.readUnreadData(PrefManager.MESSAGECOUNT)
        binding.tvUnreadChatCount.text=set?.size.toString()
        //if(PrefManager.read(PrefManager.UNSEENMSGCOUNT,"0")=="0") {
        if(set?.isNullOrEmpty() == true){
            binding.tvUnreadChatCount.visibility = View.INVISIBLE
        }else{
            binding.tvUnreadChatCount.visibility = View.VISIBLE
        }
    }

    fun showBottom() {
        binding.clBottom.visibility = View.VISIBLE
        binding.imgSos.visibility = View.VISIBLE
    }

    fun hideBottom() {
        binding.clBottom.visibility = View.GONE
        binding.imgSos.visibility = View.GONE
    }

    fun hideMapControl() {
        binding.clMapControl.visibility = View.GONE
    }

    fun resetColorChange() {
        binding.tvProfile.setTextColor(resources.getColor(R.color.white, theme))

        for (drawable in binding.tvProfile.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        resources.getColor(R.color.white, theme),
                        PorterDuff.Mode.SRC_IN)
            }
        }

        binding.tvSetting.setTextColor(resources.getColor(R.color.white, theme))

        for (drawable in binding.tvSetting.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        resources.getColor(R.color.white, theme),
                        PorterDuff.Mode.SRC_IN)
            }
        }
    }

    fun changeSettingColor() {
        binding.tvSetting.setTextColor(resources.getColor(R.color.yellow, theme))

        for (drawable in binding.tvSetting.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        resources.getColor(R.color.yellow, theme),
                        PorterDuff.Mode.SRC_IN)
            }
        }

        binding.tvProfile.setTextColor(resources.getColor(R.color.white, theme))

        for (drawable in binding.tvProfile.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        resources.getColor(R.color.white, theme),
                        PorterDuff.Mode.SRC_IN
                    )
            }
        }
    }

    fun changeProfileColor() {
        binding.tvProfile.setTextColor(resources.getColor(R.color.yellow, theme))

        for (drawable in binding.tvProfile.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        resources.getColor(R.color.yellow, theme),
                        PorterDuff.Mode.SRC_IN
                    )
            }
        }

        binding.tvSetting.setTextColor(resources.getColor(R.color.white, theme))

        for (drawable in binding.tvSetting.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        resources.getColor(R.color.white, theme),
                        PorterDuff.Mode.SRC_IN
                    )
            }
        }
    }

    fun replaceFragment(fragment: Fragment, bundle: Bundle) {
        fragment.arguments = bundle
        // val fragmentCurrent: Fragment? = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val transaction = supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, fragment)
            addToBackStack(fragment::class.java.canonicalName)
        }
        transaction.commit()
        closeDrawer()
        // Utils.hideKeyboard(this@DashboardActivity)
    }//eof replace fragment

    fun addFragment(fragment: Fragment, bundle: Bundle) {
        fragment.arguments = bundle
        // val fragmentCurrent: Fragment? = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val transaction = supportFragmentManager.beginTransaction().apply {
            add(R.id.nav_host_fragment, fragment)
            addToBackStack(fragment::class.java.canonicalName)
        }
        transaction.commit()
        closeDrawer()
        //  Utils.hideKeyboard(this@DashboardActivity)
    }

    private fun closeDrawer() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
        }
    }

    fun clearFragmentStack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            for (i: Int in 0 until supportFragmentManager.backStackEntryCount - 1) {
                supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onBackPressed()
    {
        if (binding.drawer.isDrawerOpen(GravityCompat.START))
        {
            binding.drawer.closeDrawer(GravityCompat.START)
        }
        else {
            if (supportFragmentManager.backStackEntryCount > 1) {
                super.onBackPressed()
            } else {
                finish()
            }
        }
    }

    private fun logout() {
        viewModal.logout()
    }

    private fun sendSosAlert()
    {
        if (latitude == "") {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_NETWORK_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            try {
                latitude = PrefManager.read(PrefManager.SLATITUDE, "")
                longitude = PrefManager.read(PrefManager.SLONGITUDE, "")

                street = Utils.getAddressFromLatLng(
                    this@DashboardActivity,
                    LatLng(latitude.toDouble(), longitude.toDouble())
                )
                if (street == "") {
                    street = "null"
                }
                val request = SosAlertRequest("" + latitude, street, "" + longitude)

                viewModal.sosAlert(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            if(street==""){
                street="null"
            }
            val request = SosAlertRequest(
                latitude, street, longitude
            )
            viewModal.sosAlert(request)
        }
    }

    private fun setObserver() {
        viewModal.logout.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    PrefManager.clearUserPref()
                    startActivity(
                        Intent(
                            this@DashboardActivity,
                            LoginActivity::class.java
                        )
                    )
                    stopLocationService()
                    /*if (mIsBound == true) {
                        unbindService()
                        mIsBound = false
                    }*/
                    finish()
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                this@DashboardActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }
                }
            }
        }

        viewModal.sosAlert.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        startActivity(
                            Intent(
                                this@DashboardActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        ProgressDialog.hideProgressBar()
                        viewModal.unreadNotification()
//                    Toast.makeText(
//                        applicationContext,
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                        popupSosAlert(binding.root)
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                this@DashboardActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }
                }
            }
        }

        viewModal.verifyCurrentLocation.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        startActivity(
                            Intent(
                                this@DashboardActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }
                    ProgressDialog.hideProgressBar()

                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                this@DashboardActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    } else {
//                        Toast.makeText(
//                            this,
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }
        }

        viewModal.notificationCount.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    // ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        startActivity(
                            Intent(
                                this@DashboardActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        val count = response.data?.unread_count
                        if (count != null && count > 0) {
                            binding.tvUnreadCount.visibility = View.VISIBLE
                            binding.tvUnreadCount.text = count.toString()
                        } else {
                            binding.tvUnreadCount.visibility = View.GONE
                        }
                    }
                }
                is Resource.Loading -> {
                    // ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    // ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                this@DashboardActivity,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    } else {
//                        Toast.makeText(
//                            this,
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }
        }
    }

    private fun popupLogout(view: View?) {
        // inflate the layout of the popup window
        val inflater = this@DashboardActivity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupBinding: PopupLogoutBinding =
            DataBindingUtil.inflate(inflater, R.layout.popup_logout, null, false)
        val displayMetrics = DisplayMetrics()
        this@DashboardActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        // create the popup window

        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupBinding.root, width, height, focusable)
        popupWindow.showAtLocation(view, Gravity.END, 0, 0)

        popupBinding.btnNo.setOnClickListener {
            popupWindow.dismiss()
        }

        popupBinding.btnYes.setOnClickListener {
            popupWindow.dismiss()
            PrefManager.write(PrefManager.FCM_USER_ID, " ")
            logout()
        }

    } //eof popup

    private fun popupSosAlert(view: View?) {
        // inflate the layout of the popup window
        val inflater =
            this@DashboardActivity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupBinding: PopupSosAlertBinding =
            DataBindingUtil.inflate(inflater, R.layout.popup_sos_alert, null, false)
        val displayMetrics = DisplayMetrics()
        this@DashboardActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        // create the popup window
        // int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        // int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupBinding.root, width, height, focusable)
        popupWindow.showAtLocation(view, Gravity.END, 0, 0)

        popupBinding.imgCancel.setOnClickListener {
            popupWindow.dismiss()
        }

    } //eof popup

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }

        if (requestCode == Constants.REQUEST_CODE_LOCATION && requestCode == Activity.RESULT_OK) {
            getCurrentLocation()
        }
    }

    fun startLocationUpdates()
    {
        if (isLocationEnabled(this@DashboardActivity)) {
            val locationRequest = LocationRequest.create().apply {
                interval = 50000
                fastestInterval = 50000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            if (ContextCompat.checkSelfPermission( this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION),Constants.PERMISSIONS_REQUEST_CODE_DEMO)

                return
            }

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        if (location != null) {
                            PrefManager.write(PrefManager.LATITUDE, location.latitude.toString())
                            PrefManager.write(PrefManager.LONGITUDE, location.longitude.toString())

                            myLocationCallback?.onLocationSuccess(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                )
                            )

                            latitude = location.latitude.toString()
                            longitude = location.longitude.toString()
                            street = Utils.getAddressFromLatLng(
                                this@DashboardActivity,
                                LatLng(location.latitude, location.longitude)
                            )
                        }
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            enableLocationSettings()
        }
    }

    private fun getCurrentLocation() {
        if (isLocationEnabled(this@DashboardActivity)) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            if (ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ), Constants.PERMISSIONS_REQUEST_CODE_DEMO
                )
                return
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        PrefManager.write(PrefManager.LATITUDE, location.latitude.toString())
                        PrefManager.write(PrefManager.LONGITUDE, location.longitude.toString())

                        latitude = location.latitude.toString()
                        longitude = location.longitude.toString()
                        street = Utils.getAddressFromLatLngDashBoard(
                            this@DashboardActivity,
                            LatLng(location.latitude, location.longitude))
                    }
                    else {
                        Log.e(TAG, ">>>>> Unable to fetch location.")
                        //     Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                        if(latitude == "") {
                            startLocationUpdates()
                        }
                        else { }
                    }
                }
        } else {
            enableLocationSettings()
        }
    }

    private fun stopLocationUpdates()
    {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onStop() {
        //stopLocationUpdates()
        super.onStop()
//        if(isFinishing)
    }

    private fun stopLocationService(){
        stopService(Intent(this,LocationService::class.java))
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun enableLocationSettings() {
        val locationRequest = LocationRequest.create()
            .setInterval(500)
            .setFastestInterval(500)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(
                this
            ) { response: LocationSettingsResponse? ->
                getCurrentLocation()
            }
            .addOnFailureListener(
                this
            ) { ex: java.lang.Exception? ->
                if (ex is ResolvableApiException) {
                    // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                        ex.startResolutionForResult(
                            this@DashboardActivity,
                            Constants.REQUEST_CODE_LOCATION)

                    } catch (sendEx: IntentSender.SendIntentException) {
                        //  Toast.makeText(this, "Enable Location from settings", Toast.LENGTH_SHORT)
                        //     .show()
                    }
                }
            }
    }

    private fun updateUserData() {
        tvName.text = PrefManager.read(PrefManager.NAME, "")
        tvCountry.text = PrefManager.read(PrefManager.COUNTRY_NAME, "")
        Utils.loadImage(
            this@DashboardActivity,
            profileImage,
            PrefManager.read(PrefManager.IMAGE, "")
        )
    }
}