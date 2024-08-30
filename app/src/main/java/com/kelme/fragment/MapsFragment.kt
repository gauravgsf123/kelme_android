package com.kelme.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.net.PlacesClient
import com.kelme.R
import com.kelme.activity.chat.UserDetailsActivity
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.dashboard.DashboardViewModal
import com.kelme.activity.login.LoginActivity
import com.kelme.custom.RegularTextView
import com.kelme.databinding.FragmentMapsBinding
import com.kelme.event.AlertEvent
import com.kelme.event.TrackingEvent
import com.kelme.fragment.security.SecurityAlertsDetailFragment
import com.kelme.interfaces.MyLocationCallback
import com.kelme.model.NearByAlertsModel
import com.kelme.model.NearByTrackingModel
import com.kelme.model.request.NearByRequest
import com.kelme.utils.*
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import de.hdodenhof.circleimageview.CircleImageView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    MyLocationCallback {

   // private lateinit var mapRipple: MapRipple
    private lateinit var marker:Marker
    private lateinit var userMarker:Marker
    //private lateinit var userMarker:MarkerOptions
    private var firsttime="0"
    private val TAG = MapsFragment::class.java.simpleName
    private lateinit var binding: FragmentMapsBinding
    private lateinit var viewModal: DashboardViewModal
    // private lateinit var marker: Marker
    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private var latitude = ""
    private var longitude = ""
    private var street = ""
    private var postalCode = ""
    private var city = ""
    private var type = "1"
    private var listNearByAlert: ArrayList<NearByAlertsModel> = ArrayList()
    private var listNearByTracking: ArrayList<NearByTrackingModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps, container, false)

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            DashboardViewModal::class.java
        )
        setUI()
        setObserver()
        //binding.pulsator.start()
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    private fun addCustomMarker(latlng: LatLng) {
        Log.d(TAG, "addCustomMarker")
        if (this::userMarker.isInitialized)
        userMarker.remove()
        // adding a marker on map with image from drawable
        val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.view_marker, null)
        userMarker = map.addMarker(
            MarkerOptions()
                .position(latlng)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(view)))
        )!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

//        val url = URL( PrefManager.read(PrefManager.IMAGE, ""))
//        val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
//        map.addMarker(
//            MarkerOptions()
//                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
//        )

        mapFragment?.getMapAsync {
            map = it
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isZoomGesturesEnabled = true

            map.setPadding(0, 0, 0, 130) //change padding

            map.setOnCameraIdleListener {
                val latLng: LatLng = map.cameraPosition.target
                latitude = latLng.latitude.toString()
                longitude = latLng.longitude.toString()
                //Log.e("TAG", "initializeMap: $latitude $longitude")
                val address = Utils.getAddressFromLatLngAsAddress(requireActivity(), latLng)
                street = address?.getAddressLine(0).toString()
                postalCode = address?.postalCode.toString()
                           city = address?.locality.toString()

                map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            }

            if (type == "1") {
                map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                    override fun getInfoWindow(p0: Marker): View? {
                        val view = layoutInflater.inflate(R.layout.map_description_dialog, null)
                        val tvDescription: RegularTextView =
                            view.findViewById(R.id.tv_description)
                        val ivDetails: ImageView =
                            view.findViewById(R.id.iv_details)
                        tvDescription.text = p0.snippet
                        view.setOnClickListener {

                        }
                        return view
                    }

                    override fun getInfoContents(p0: Marker): View? {
                        return null
                    }

                })

                map.setOnInfoWindowClickListener { marker ->
                    if(type=="1") {
                        val bundle = Bundle()
                        bundle.putString(
                            Constants.SECURITY_ALERT_MODEL,
                            listNearByAlert[marker.tag.toString().toInt()].security_alert_id
                        )
                        (activity as DashboardActivity).replaceFragment(
                            SecurityAlertsDetailFragment(),
                            bundle
                        )
                    }else{
                        val intent = Intent(context,UserDetailsActivity::class.java)
                        intent.putExtra("userId",listNearByTracking[marker.tag.toString().toInt()].firebase_id)
                        activity?.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            EventBus.getDefault().register(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        (activity as DashboardActivity?)?.run {
            setTitle("Maps")
            hideNotificationIcon()
            showBackArrow()
            showMapControl()
            hideUnreadCount()
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as DashboardActivity?)?.run {
            hideMapControl()
        }
        EventBus.getDefault().unregister(this)
    }

    private fun setUI() {
        // nothing
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: TrackingEvent) {
        nearbyTracking()
        firsttime="1"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: AlertEvent)
    {
        nearbyAlert()
        firsttime="1"
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val success: Boolean = map.setMapStyle(
            MapStyleOptions(
                resources
                    .getString(R.string.style_json)
            )
        )
        //map.isMyLocationEnabled = true

        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()

        val location: Location? =
            locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false)!!)
        if (location != null)
        {
            latitude = location.latitude.toString()
            longitude = location.longitude.toString()

            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), 35f
                )
            )
            val cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        location.latitude,
                        location.longitude
                    )
                ) // Sets the center of the map to location user
                .zoom(35f) // Sets the zoom
                .bearing(360f) // Sets the orientation of the camera to North
                .tilt(40f) // Sets the tilt of the camera to 30 degrees
                .build() // Creates a CameraPosition from the builder
            //map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            map.setOnMarkerClickListener(this)
            //map.uiSettings.setAllGesturesEnabled(false);


            nearbyAlert()
        } else {
            if (activity is DashboardActivity)
            {
                (activity as DashboardActivity).setLocationCallback(this)
            }
        }

        (activity as DashboardActivity).setLocationCallback(this)
    }

    private fun createMarker(
        latitude: Double,
        longitude: Double,
        title: String?,
        snippet: String?,
        risk_type: Int,
        risk_category: Int,
        image:String,
        position: Int
    ):Marker? {

        if(type == "1")
        {
            if(risk_type == 1 && risk_category == 1)    //minimal risk and geopoliticas
            {
              // startPulsatorAnimation(map,latitude,longitude)
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
            }

              else if(risk_type == 1 && risk_category == 2)    //minimal risk and geopoliticas
            {
             //   startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 1 && risk_category == 3)    //minimal risk and Terrorism
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 1 && risk_category == 4)    //minimal risk and Social Unrest
            {
             //   startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 1 && risk_category == 5)    //minimal risk and Armed Conflict
            {
              //  startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 1 && risk_category == 6)    //minimal risk and Health
            {
             //   startPulsatorAnimation(map,latitude,longitude)
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

            }else if (risk_type == 1 && risk_category == 7)    //minimal risk and Travel
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minitravel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }
            else if (risk_type == 1 && risk_category == 8)    //minimal risk and Travel Disruption
            {
             //   startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minitravel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }
            else if (risk_type == 1 && risk_category == 9)    //minimal risk and Natural Hazard
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_mininaturalhazard))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 1 && risk_category == 10)    //minimal risk and Weather
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_miniweather))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 1 && risk_category == 11)    //minimal risk and police Operation
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minipoliceoperation))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 1 && risk_category == 12)    //minimal risk and Traffic Disruption
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minitrafficdisruption))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 1 && risk_category == 13)    //minimal risk and Militancy
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minimilitancy))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 1 && risk_category == 14)    //minimal risk and Strike
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_ministrike))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }
            else if (risk_type == 2 && risk_category == 1)    //Low risk and Geopolitical
            {
              //  startPulsatorAnimation(map,latitude,longitude)
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
              //  startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 2 && risk_category == 3)    //Low risk and Terrorism
            {
              //  startPulsatorAnimation(map,latitude,longitude)
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
              //  startPulsatorAnimation(map,latitude,longitude)
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
              //  startPulsatorAnimation(map,latitude,longitude)
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
                //startPulsatorAnimation(map,latitude,longitude)
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

            }else if (risk_type == 2 && risk_category == 7)    //Low risk and Travel
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_travel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }
            else if (risk_type == 2 && risk_category == 8)    //Low risk and Travel Disruption
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_travel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }
            else if (risk_type == 2 && risk_category == 9)    //Low risk and Natural Hazard
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_naturalhazard))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 2 && risk_category == 10)    //Low risk and Weather
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_weather))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 2 && risk_category == 11)    //Low risk and police Operation
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_policeoperation))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 2 && risk_category == 12)    //Low risk and Traffic Disruption
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_trafficdisruption))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 2 && risk_category == 13)    //Low risk and Militancy
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_militancy))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            }else if (risk_type == 2 && risk_category == 14)    //Low risk and Strike
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_low_strike))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker

            } else if (risk_type == 3 && risk_category == 1)    //Moderate risk and Geopolitical
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 3 && risk_category == 2)    //Moderate risk and Crime
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
               // startPulsatorAnimation(map,latitude,longitude)
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
               // startPulsatorAnimation(map,latitude,longitude)
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
               // startPulsatorAnimation(map,latitude,longitude)
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
              //  startPulsatorAnimation(map,latitude,longitude)
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
            }else if (risk_type == 3 && risk_category == 7)    //Moderate risk and Travel
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modarate_travel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }
            else if (risk_type == 3 && risk_category == 8)    //Moderate risk and Travel Disruption
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modarate_travel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }
            else if (risk_type == 3 && risk_category == 9)    //Moderate risk and Natural Hazard
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modarate_naturalhazard))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 3 && risk_category == 10)    //Moderate risk and Weather
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modarate_weather))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 3 && risk_category == 11)    //Moderate risk and police Operation
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modarate_policeoperation))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 3 && risk_category == 12)    //Moderate risk and Traffic Disruption
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modarate_traficdisruption))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 3 && risk_category == 13)    //Moderate risk and Militancy
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modarate_militancy))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 3 && risk_category == 14)    //Moderate risk and Strike
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_modarate_srike))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }
            else if (risk_type == 4 && risk_category == 1)    //High risk and Geopolitics
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 4 && risk_category == 2)    //High risk and crime
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 4 && risk_category == 3)    //High risk and Terriosm
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
            }

            else if (risk_type == 4 && risk_category == 4)    //High risk and Social Unrest
            {
              //  startPulsatorAnimation(map,latitude,longitude)
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
            }

            else if (risk_type == 4 && risk_category == 5)    //High risk and Armed Conflict
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 4 && risk_category == 6)    //High risk and Health
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
            }else if (risk_type == 4 && risk_category == 7)    //High risk and Travel
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_travel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }
            else if (risk_type == 4 && risk_category == 8)    //High risk and Travel Disruption
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_travel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }
            else if (risk_type == 4 && risk_category == 9)    //High risk and Natural Hazard
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_naturalhazard))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 4 && risk_category == 10)    //High risk and Weather
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_weather))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 4 && risk_category == 11)    //High risk and police Operation
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_policeoperation))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 4 && risk_category == 12)    //High risk and Traffic Disruption
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_trafficdisruption))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 4 && risk_category == 13)    //High risk and Militancy
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_militancy))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 4 && risk_category == 14)    //High risk and Strike
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_high_strike))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }
            else if (risk_type == 5 && risk_category == 1)    //Extreme risk and Geopolitical
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 5 && risk_category == 2)    //Extreme risk and Crime
            {
              //  startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 5 && risk_category == 3)    //Extreme risk and Terrorism
            {
              //  startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 5 && risk_category == 4)    //Extreme risk and Social Unrest
            {
              //  startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 5 && risk_category == 5)    //Extreme risk and Armed Conflict
            {
              //  startPulsatorAnimation1(map,latitude,longitude)
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
            }
            else if (risk_type == 5 && risk_category == 6)    //Extreme risk and Health
            {
               // startPulsatorAnimation(map,latitude,longitude)
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
            }
            else if (risk_type == 5 && risk_category == 7)    //Extreme risk and Travel
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_travel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }
            else if (risk_type == 5 && risk_category == 8)    //Extreme risk and Travel Disruption
            {
               // startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_travel))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }
            else if (risk_type == 5 && risk_category == 9)    //Extreme risk and Natural Hazard
            {
             //   startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_naturalhazard))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 5 && risk_category == 10)    //Extreme risk and Weather
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_weather))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 5 && risk_category == 11)    //Extreme risk and police Operation
            {
             //   startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_policeoperation))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 5 && risk_category == 12)    //Extreme risk and Traffic Disruption
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_trafficdisruption))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 5 && risk_category == 13)    //Extreme risk and Militancy
            {
              //  startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_militancy))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }else if (risk_type == 5 && risk_category == 14)    //Extreme risk and Strike
            {
             //   startPulsatorAnimation(map,latitude,longitude)
                marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_extreame_strike))
                        .title(title)
                        .snippet(snippet)
                )!!
                marker.tag = position.toString()
                return marker
            }
            else                                    //nothing to add condition 0,0
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
        }
        else
        {


            Glide.with(this)
                .asBitmap()
                .load(image)
                .into(object : CustomTarget<Bitmap?>(100,100) {
                    override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {


                    }
                })


            marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_minimalrisk_crime))
                    .anchor(0.5f, 0.5f)
                    .title(title)
                    .snippet(snippet)
            )!!
            marker.tag = position.toString()
            return marker
        }
    }

    private fun getMarkerBitmapFromView(view: View): Bitmap {
        val item: CircleImageView = view.findViewById(R.id.map_photo)
        item.setImageDrawable(resources.getDrawable(R.drawable.user))
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(
            view.measuredWidth, view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable = view.background
        drawable?.draw(canvas)
        view.draw(canvas)
        return returnedBitmap
    }


    override fun onMarkerClick(p0: Marker): Boolean {
        val position = (p0!!.tag).toString()
        if (type == "1") {

            //var view = requireActivity().findViewById(p0.id)//p0.id
            /*showDiscriptionBox(requireView(),p0.snippet,position)
            val bundle = Bundle()
            bundle.putString(
                Constants.SECURITY_ALERT_MODEL,
                listNearByAlert[position.toInt()].security_alert_id
            )
            (activity as DashboardActivity).replaceFragment(
                SecurityAlertsDetailFragment(),
                bundle
            )*/

        } else {
            //nothing to do
        }

        return false
    }

    private fun setObserver() {
        viewModal.logout.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    PrefManager.clearUserPref()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    startActivity(
                        Intent(
                            activity,
                            LoginActivity::class.java
                        )
                    )
                    activity?.finish()
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    if (response.message == "Your session has been expired, Please login again.") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                activity,
                                LoginActivity::class.java
                            )
                        )
                        activity?.finish()
                    }
                }
            }
        })

        viewModal.nearbyAlerts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                        type = "1"
                        map.clear()
                        listNearByAlert.clear()
                        listNearByAlert = response.data as ArrayList<NearByAlertsModel>
                        listNearByAlert.forEachIndexed { index, it ->
                            if (!it.latitude.isEmpty()) {
                                var marker = createMarker(
                                    it.latitude.toDouble(),
                                    it.longitude.toDouble(),
                                    "",
                                    it.title,
                                    it.risk_type.toInt(),
                                    it.risk_category.toInt(),
                                    "",
                                    index
                                )
                            }
                        }
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }
        }

        viewModal.nearbyTracking.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                        map.clear()
                        type = "2"
                        listNearByTracking.clear()
                        listNearByTracking = response.data as ArrayList<NearByTrackingModel>
                        if(listNearByTracking.size==0){

                        }

                        listNearByTracking.forEachIndexed { index, it ->
                            createMarker(
                                it.latitude.toDouble(),
                                it.longitude.toDouble(), "", it.name, 0, 0,it.image ,index
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun nearbyAlert()
    {
        if(latitude == "")
        {
//            Toast.makeText(requireContext(), "Could not get your location", Toast.LENGTH_SHORT)
//                .show()
        }
        else
        {
            val request = NearByRequest(latitude, longitude,PrefManager.read(PrefManager.COUNTRY_ID,""))
            viewModal.nearbyAlerts(request)
            firsttime="1"
        }
    }

    private fun nearbyTracking() {
        if (latitude == "")
        {
//            Toast.makeText(requireContext(), "Could not get your location", Toast.LENGTH_SHORT)
//                .show()
        }
        else
        {
            val request = NearByRequest(
                latitude,
                longitude,
                PrefManager.read(PrefManager.COUNTRY_ID,"")
            )
            viewModal.nearbyTracking(request)
        }
    }

    override fun onLocationSuccess(latlng: LatLng)
    {
        /*map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latlng, 35f
            )
        )*/
        val cameraPosition = CameraPosition.Builder()
            .target(
                latlng
            ) // Sets the center of the map to location user
            .zoom(35f) // Sets the zoom
            .bearing(360f) // Sets the orientation of the camera to North
            .tilt(40f) // Sets the tilt of the camera to 30 degrees
            .build() // Creates a CameraPosition from the builder

        //map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        map.setOnMarkerClickListener(this)


        addCustomMarker(latlng)

        if(firsttime.equals("0"))
        {
            nearbyAlert()
        }
        else
        { }
    }

    fun showDiscriptionBox(view: View, description: String, position: String){
        val balloon = Balloon.Builder(requireContext())
            .setLayout(R.layout.map_description_dialog)
            .setArrowOrientation(ArrowOrientation.TOP)
            .setArrowSize(1)
            .setIsVisibleArrow(false)
            .setMarginRight(18)
            .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
            .setBalloonAnimation(BalloonAnimation.FADE)
            .build()
        val tvDescription: RegularTextView =
            balloon.getContentView().findViewById(R.id.tv_description)
        val ivDetails: ImageView =
            balloon.getContentView().findViewById(R.id.iv_details)
        tvDescription.text = description

        balloon.showAlignTop(view)

        tvDescription.setOnClickListener {
            //balloon.dismiss()
        }

        ivDetails.setOnClickListener {
            if(type=="1") {
                val bundle = Bundle()
                bundle.putString(
                    Constants.SECURITY_ALERT_MODEL,
                    listNearByAlert[position.toInt()].security_alert_id
                )
                (activity as DashboardActivity).replaceFragment(
                    SecurityAlertsDetailFragment(),
                    bundle
                )
            }else{
                val intent = Intent(context,UserDetailsActivity::class.java)
                intent.putExtra("userId",listNearByTracking[position.toInt()].firebase_id)
                activity?.startActivity(intent)
            }
            balloon.dismiss()
        }
    }
}