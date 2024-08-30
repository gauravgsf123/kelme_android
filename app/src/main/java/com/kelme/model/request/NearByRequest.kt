package com.kelme.model.request

data class NearByRequest(
    val lat: String,
    val long: String,
    val country_id:String
)