package com.kelme.model.request

data class VerifyOtpRequest(
    val otp: String,
    val otp_type: String,
    val provider: String
)