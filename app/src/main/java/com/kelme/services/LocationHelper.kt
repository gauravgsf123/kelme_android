package com.kelme.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationHelper {

    companion object {
        private const val LOCATION_INTERVAL: Long = 5 * 60 * 1000 // 20 minutes in milliseconds
        private const val LOCATION_FASTEST_INTERVAL: Long = 5 * 60 * 1000 // same as interval
        private const val LOCATION_DISTANCE: Float = 100f // 100 meters
    }

    private var locationCallback: LocationCallback? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    fun startListeningUserLocation(
        context: Context,
        myListener: MyLocationListener
    ) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val request = LocationRequest.create().apply {
            interval = LOCATION_INTERVAL
            fastestInterval = LOCATION_FASTEST_INTERVAL
            smallestDisplacement = LOCATION_DISTANCE
            priority = PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    myListener.onLocationChanged(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}

interface MyLocationListener {
    fun onLocationChanged(location: Location?)
}