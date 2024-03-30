package com.kelme.interfaces

import com.google.android.gms.maps.model.LatLng

interface MyLocationCallback {
    fun onLocationSuccess(latlng: LatLng)
}