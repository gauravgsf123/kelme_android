package com.kelme.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationHelper() {
    var LOCATION_REFRESH_TIME = 0 // 3 seconds. The Minimum Time to get location update
    var LOCATION_REFRESH_DISTANCE = 100 // 0 meters. The Minimum Distance to be changed to get location update
    var locationRequest:LocationRequest = LocationRequest.create()


    @SuppressLint("MissingPermission")
    fun startListeningUserLocation(context: Context, myListener: MyLocationListener) {
        //val mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    // Update UI with location data
                    myListener.onLocationChanged(location)
                }
            }
        }
        locationRequest = LocationRequest.create().apply {
            interval = 60000/*TimeUnit.SECONDS.toMillis(2)*/
            fastestInterval = 60000*20/*TimeUnit.SECONDS.toMillis(2)*/
            //maxWaitTime = 60000/*TimeUnit.MINUTES.toMillis(1)*/
            // priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }
}

interface MyLocationListener {
    fun onLocationChanged(location: Location?)
}