package com.kelme.model

data class SosAlertModel(
    val latitude: String,
    val location: String,
    val longitude: String,
    val status: Int,
    val user_id: String
)