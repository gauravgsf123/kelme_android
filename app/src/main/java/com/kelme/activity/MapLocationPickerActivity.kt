package com.kelme.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.kelme.R
import com.kelme.databinding.ActivityMapLocationPickerBinding
import com.kelme.utils.Utils
import java.util.*


class MapLocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapLocationPickerBinding
    private lateinit var map: GoogleMap

    private var latitude: String = ""
    private var longitude: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_map_location_picker)

        setUI()
        initializeMap()
        initializePlace()

    }

    private fun setUI() {
        binding.btnSave.setOnClickListener {
            val intent = Intent()
            intent.putExtra("address",binding.tvLocation.text.toString())
            intent.putExtra("latitude",longitude)
            intent.putExtra("longitude",latitude)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }

        binding.btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            onBackPressed()
        }
    }

    private fun initializePlace(){
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key), Locale.getDefault());
        }
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.DISPLAY_NAME,Place.Field.LOCATION))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                val cameraPosition = CameraPosition.Builder()
                    .target(
                        place.location!!

                    ) // Sets the center of the map to location user
                    .zoom(17f) // Sets the zoom
                    .bearing(90f) // Sets the orientation of the camera to east
                    .tilt(40f) // Sets the tilt of the camera to 30 degrees
                    .build() // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
              //  Log.i("TAG", "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
               // Toast.makeText(this@MapLocationPickerActivity,"Location could not be found",Toast.LENGTH_SHORT).show()
               // Log.i("TAG", "An error occurred: $status")
            }
        })

    }

    private fun initializeMap(){
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        mapFragment?.getMapAsync {
            map = it
            map.mapType = GoogleMap.MAP_TYPE_NORMAL

            map.setOnCameraIdleListener {
                val latLng: LatLng = map.cameraPosition.target
                latitude = latLng.latitude.toString()
                longitude = latLng.longitude.toString()
                binding.tvLocation.text = Utils.getAddressFromLatLng(this@MapLocationPickerActivity, latLng)
            }

        }
    }


    override fun onMapReady(map: GoogleMap) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        map.isMyLocationEnabled = true

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()

        val location: Location? =
            locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false)!!)
        if (location != null) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                    ), 13f
                )
            )
            val cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                    )
                ) // Sets the center of the map to location user
                .zoom(17f) // Sets the zoom
                .bearing(90f) // Sets the orientation of the camera to east
                .tilt(40f) // Sets the tilt of the camera to 30 degrees
                .build() // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }


}




