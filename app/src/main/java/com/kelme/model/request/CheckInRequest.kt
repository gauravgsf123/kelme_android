package com.kelme.model.request

data class CheckInRequest(
    val city: String,
    val latitude: String,
    val longitude: String,
    val postcode: String,
    val street: String
)