package com.kelme.model.request

data class ForgetPasswordRequest(
    val username: String,
    val device_type_id: String,
    val device_id: String,
    val device_token: String
)