package com.kelme.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.kelme.R
import com.kelme.activity.dashboard.DashboardViewModal
import com.kelme.activity.login.LoginActivity
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityShowMapEventLocationBinding
import com.kelme.databinding.PopupSosAlertBinding
import com.kelme.interfaces.MyLocationCallback
import com.kelme.model.request.SosAlertRequest
import com.kelme.utils.*
import java.io.IOException


class ShowMapEventLocationActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityShowMapEventLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var location: String
    private var street = ""
    private var postalCode = ""
    private var city = ""
    lateinit var address: Address
    lateinit var latLng1: LatLng
    private var type = "1"
    private var risk_type = ""
    private var risk_category = ""
    private var countDownTimer: CountDownTimer? = null
    private var timeSwapBuff = 10L
    private var isUpOnce = true
    private lateinit var locationCallback: LocationCallback
    private var latitude = ""
    private var longitude = ""
    private var myLocationCallback: MyLocationCallback? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModal: DashboardViewModal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_map_event_location)

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)

        risk_type = intent.getStringExtra("Risk_type").toString()
        risk_category = intent.getStringExtra("Risk_category").toString()
        location = intent.getStringExtra("Location").toString()
        latitude= PrefManager.read(PrefManager.LATITUDE, "")
        longitude= PrefManager.read(PrefManager.LONGITUDE, "")

        viewModal = ViewModelProvider(
            this,
            ViewModalFactory(application)
        ).get(DashboardViewModal::class.java)

        setUI()
        setObserver()
    }

    private fun setObserver()
    {
        viewModal.sosAlert.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        ProgressDialog.hideProgressBar()
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
                    if (response.message == "240") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }
                }
            }
        }
    }

    override fun initializerControl() {
        TODO("Not yet implemented")
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        val success: Boolean = map.setMapStyle(MapStyleOptions(resources.getString(R.string.style_json)))

        val coder = Geocoder(this)
        val address: List<Address?>

        try {
            address = coder.getFromLocationName(location, 5) as List<Address?>
            if (address != null) {
                val location = address[0]!!
                latLng1 = LatLng(location.latitude, location.longitude)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
        map.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom
        map.setPadding(0, 0, 0, 110)

        createMarker(
            latLng1.latitude, latLng1.longitude, location, "", risk_type.toInt(),
            risk_category.toInt(), 0
        )

        map.setOnCameraIdleListener {

            val address = Utils.getAddressFromLatLngAsAddress(this, latLng1)
            street = address?.getAddressLine(0).toString()
            postalCode = address?.postalCode.toString()
            city = address?.locality.toString()
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 10f))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUI()
    {
        /* binding.clSearchBar.setOnClickListener {
             replaceFragment(ChatListFragment(), Bundle.EMPTY)
         }*/
        binding.backArrow.setOnClickListener {
            finish()
        }

        binding.imgSos.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    startTimer()
                    //  Log.e("Action Down ", ">>>Down")
                    Glide.with(this@ShowMapEventLocationActivity)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .load(R.drawable.sos_alert)
                        .into(binding.imgSos)
                }
                MotionEvent.ACTION_UP -> {
                    if (isUpOnce) {
                        stopTimer()
                        // Log.e("Action up ", ">>>Up")
                        if (timeSwapBuff == 0L && isUpOnce) {
                            sendSosAlert()
                            vibratePhone()
                        }
                        isUpOnce = false
                    }
                    Glide.with(this@ShowMapEventLocationActivity)
                        .load(R.drawable.dashboard_sos)
                        .into(binding.imgSos)
                }
            }
            v?.onTouchEvent(event) ?: true
        }
    }

    private fun popupSosAlert(view: View?) {
        // inflate the layout of the popup window
        val inflater =
            this@ShowMapEventLocationActivity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupBinding: PopupSosAlertBinding =
            DataBindingUtil.inflate(inflater, R.layout.popup_sos_alert, null, false)
        val displayMetrics = DisplayMetrics()
        this@ShowMapEventLocationActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)
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

//        popupBinding.btnYes.setOnClickListener {
//            popupWindow.dismiss()
//            logout()
//        }

    } //eof popup

    fun vibratePhone() {
        val vibrator =
            this@ShowMapEventLocationActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(300)
        }
    }


    private fun startTimer() {
        isUpOnce = true
        countDownTimer = object : CountDownTimer(2700, 1000) {
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

    private fun sendSosAlert()
    {
        if (latitude == "")
        {
//            Toast.makeText(
//                this@ShowMapEventLocationActivity,
//                "Getting your location, Try Again!!",
//                Toast.LENGTH_LONG
//            ).show()
            startLocationUpdates()
        }
        else
        {
            if(street==""){
                street="null"
            }
            val request = SosAlertRequest(
                latitude, street, longitude
            )
            viewModal.sosAlert(request)
        }
    }


fun startLocationUpdates()
{
    if (isLocationEnabled(this@ShowMapEventLocationActivity)) {
        val locationRequest = LocationRequest.create().apply {
            interval = 50000
            fastestInterval = 50000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for (location in p0.locations) {
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
                            this@ShowMapEventLocationActivity,
                            LatLng(location.latitude, location.longitude)
                        )
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
            Looper.getMainLooper()
        )
    } else {
        enableLocationSettings()
    }
}

private fun getCurrentLocation()
{
    if (isLocationEnabled(this@ShowMapEventLocationActivity)) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    PrefManager.write(PrefManager.LATITUDE, location.latitude.toString())
                    PrefManager.write(PrefManager.LONGITUDE, location.longitude.toString())

                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    street = Utils.getAddressFromLatLng(
                        this@ShowMapEventLocationActivity,
                        LatLng(location.latitude, location.longitude)
                    )

                } else
                {
                    Log.e(TAG, ">>>>> Unable to fetch location.")
                  //  Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                    startLocationUpdates()
                }
            }

    } else {
        enableLocationSettings()
    }

}

private fun stopLocationUpdates() {
    if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

override fun onStop() {
    stopLocationUpdates()
    super.onStop()
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
                        this@ShowMapEventLocationActivity,
                        Constants.REQUEST_CODE_LOCATION
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                 //   Toast.makeText(this, "Enable Location from settings", Toast.LENGTH_SHORT)
                 //       .show()
                }
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
//        transaction.setCustomAnimations(
//            R.anim.fragment_enter_slide,
//            R.anim.pop_exit,
//            R.anim.pop_enter,
//            R.anim.fragment_exit_slide
//        )
    transaction.commit()
    // Utils.hideKeyboard(this@DashboardActivity)
}//eof replace fragment


private fun createMarker(
    latitude: Double,
    longitude: Double,
    title: String?,
    snippet: String?,
    risk_type: Int,
    risk_category: Int,
    position: Int
): Marker? {
    var marker: Marker
    if (type == "1") {
        /* var marker = map.addMarker(
             MarkerOptions()
                 .position(LatLng(latitude, longitude))
                 .anchor(0.5f, 0.5f)
                 .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minimalrisk_geopolitics))
                 .title(title)
                 .snippet(snippet)
         )
         marker.tag = position.toString()
         return marker*/

        if (risk_type == 1 && risk_category == 1)    //minimal risk and geopoliticas
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minimalrisk_geopolitics))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 1 && risk_category == 2)    //minimal risk and geopoliticas
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minimalrisk_crime))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 1 && risk_category == 3)    //minimal risk and Terrorism
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minimal_teririsom))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 1 && risk_category == 4)    //minimal risk and Social Unrest
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minimal_socialunrest))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 1 && risk_category == 5)    //minimal risk and Armed Conflict
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_mini_armedconflict))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 1 && risk_category == 6)    //minimal risk and Health
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minihealth))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker

        } else if (risk_type == 2 && risk_category == 1)    //Low risk and Geopolitical
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_geopolitical))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker

        } else if (risk_type == 2 && risk_category == 2)    //Low risk and Crime
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_crime))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 2 && risk_category == 3)    //Low risk and Terrorism
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_terrorism))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker

        } else if (risk_type == 2 && risk_category == 4)    //Low risk and Social Unrest
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable._img_low_socialunrest))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker

        } else if (risk_type == 2 && risk_category == 5)    //Low risk and Armed Conflict
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_armedconflict))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker

        } else if (risk_type == 2 && risk_category == 6)    //Low risk and Health
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_health))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker

        } else if (risk_type == 3 && risk_category == 1)    //Moderate risk and Geopolitical
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_moderete_geopolitical))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 3 && risk_category == 2)    //Moderate risk and Crime
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_moderate_crime))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker

        } else if (risk_type == 3 && risk_category == 3)    //Moderate risk and Terrorism
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_moderate_terrorism))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker

        } else if (risk_type == 3 && risk_category == 4)    //Moderate risk and Social Unrest
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_moderate_socialunrest))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 3 && risk_category == 5)    //Moderate risk and Armed Conflict
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modrate_armedonflict))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 3 && risk_category == 6)    //Moderate risk and Health
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modarate_health))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 4 && risk_category == 1)    //High risk and Geopolitical
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_geopolitical))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 4 && risk_category == 1)    //High risk and Geopolitics
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_geopolitical))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 4 && risk_category == 2)    //High risk and crime
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_crime))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 4 && risk_category == 3)    //High risk and Terriosm
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_terrorism))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 4 && risk_category == 4)    //High risk and Social Unrest
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_socialunrest))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 4 && risk_category == 5)    //High risk and Armed Conflict
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_armedconflict))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 4 && risk_category == 6)    //High risk and Health
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_health))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 5 && risk_category == 1)    //Extreme risk and Geopolitical
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_geopolitical))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 5 && risk_category == 2)    //Extreme risk and Crime
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_crime))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 5 && risk_category == 3)    //Extreme risk and Terrorism
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_terrorism))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 5 && risk_category == 4)    //Extreme risk and Social Unrest
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_socialunrest))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 5 && risk_category == 5)    //Extreme risk and Armed Conflict
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_armedconflict))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else if (risk_type == 5 && risk_category == 6)    //Extreme risk and Health
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_health))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        } else                                    //nothing to add condition 0,0
        {
            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.risk))
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        }
    } else {
        val marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(snippet)
        )
        marker?.tag = position.toString()
        return marker
    }
}
}