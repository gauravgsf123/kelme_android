package com.kelme.model

data class CheckInModel(
    val city: String,
    val gps_date: Int,
    val latitude: String,
    val longitude: String,
    val postcode: String,
    val street: String,
    val user_id: String
)