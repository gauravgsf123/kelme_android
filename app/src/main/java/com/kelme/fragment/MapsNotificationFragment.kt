package com.kelme.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.kelme.R
import com.kelme.activity.chat.UserDetailsActivity
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.custom.RegularTextView
import com.kelme.databinding.FragmentMapsNotificationBinding
import com.kelme.fragment.security.SecurityAlertsDetailFragment
import com.kelme.interfaces.MyLocationCallback
import com.kelme.utils.Constants
import com.kelme.utils.PrefManager

class MapsNotificationFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    MyLocationCallback {

    private lateinit var binding: FragmentMapsNotificationBinding
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps_notification, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        mapFragment?.getMapAsync {
            map = it
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isZoomGesturesEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false

            map.setPadding(0, 0, 0, 130) //change padding

            map.setOnCameraIdleListener {

                val longm = ""+PrefManager.read(PrefManager.NLONGITUDE,"")
                val doublelong: Double = longm.toDouble()
                val latm =""+PrefManager.read(PrefManager.NLATITUDE,"")
                val doublelat: Double = latm.toDouble()
                val module =""+PrefManager.read(PrefManager.NMODULE,"")
                val safety =""+PrefManager.read(PrefManager.NSAFETY,"")
                var bitmapdraw: BitmapDrawable = resources.getDrawable(R.drawable.map_icon_checkin) as BitmapDrawable

                when (module) {
                    "3" -> {
                        bitmapdraw =resources.getDrawable(R.drawable.map_icon_checkin) as BitmapDrawable
                    }
                    "2" -> {
                        bitmapdraw = resources.getDrawable(R.drawable.map_icon_sos) as BitmapDrawable

                    }
                    "5" -> {
                        bitmapdraw = if(safety=="1") {
                            resources.getDrawable(R.drawable.map_icon_safety) as BitmapDrawable

                        }else{
                            resources.getDrawable(R.drawable.map_icon_danger) as BitmapDrawable
                        }
                    }
                    "7" -> {
                        bitmapdraw = if(safety=="1") {
                            resources.getDrawable(R.drawable.map_icon_safety) as BitmapDrawable

                        }else{
                            resources.getDrawable(R.drawable.map_icon_danger) as BitmapDrawable
                        }
                    }
                }
                val b: Bitmap = bitmapdraw.bitmap
                val smallMarker: Bitmap = Bitmap.createScaledBitmap(b, 90, 120, false)
                val sydney = LatLng(doublelat,doublelong)
                map.addMarker(
                    MarkerOptions().position(sydney)
                        .title(PrefManager.read(PrefManager.NTITLE, ""))
                        .snippet(PrefManager.read(PrefManager.NTITLE, ""))
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                )
                map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

                map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

                    override fun getInfoContents(marker: Marker): View? {
                        return null
                    }

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
                })

                map.setOnInfoWindowClickListener {
                    val intent = Intent(context, UserDetailsActivity::class.java)
                        intent.putExtra("userId",PrefManager.read(PrefManager.NFIREBASEID,""))
                        activity?.startActivity(intent)
                }
            }
        }
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
            MapStyleOptions(resources.getString(R.string.style_json))
        )
//        map.isMyLocationEnabled = true

        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()

        val location: Location? =
            locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false)!!)
        if (location != null)
        {
           val latitude = location.latitude.toString()
           val longitude = location.longitude.toString()

            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), 15f
                )
            )
            val cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        location.latitude,
                        location.longitude
                    )
                ) // Sets the center of the map to location user
                .zoom(15f) // Sets the zoom
                .bearing(360f) // Sets the orientation of the camera to North
                .tilt(40f) // Sets the tilt of the camera to 30 degrees
                .build() // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            map.setOnMarkerClickListener(this)

        } else {
            if (activity is DashboardActivity)
            {
                (activity as DashboardActivity).setLocationCallback(this)
            }
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        //hello
        return false
    }

    override fun onLocationSuccess(latlng: LatLng) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latlng, 15f
            )
        )
        val cameraPosition = CameraPosition.Builder()
            .target(
                latlng
            ) // Sets the center of the map to location user
            .zoom(15f) // Sets the zoom
            .bearing(360f) // Sets the orientation of the camera to North
            .tilt(40f) // Sets the tilt of the camera to 30 degrees
            .build() // Creates a CameraPosition from the builder

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        map.setOnMarkerClickListener(this)
    }
}