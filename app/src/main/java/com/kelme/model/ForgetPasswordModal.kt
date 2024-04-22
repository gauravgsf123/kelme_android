package com.kelme.model

data class ForgetPasswordModal(
    val auth_token: String,
    val otp: String,
    val user_pass: String
)