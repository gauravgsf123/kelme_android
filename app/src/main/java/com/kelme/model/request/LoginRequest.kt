package com.kelme.model.request

data class LoginRequest (
    var username: String ,
    var password: String ,
    var device_type_id: String,
    var device_id: String,
    var device_token: String
)