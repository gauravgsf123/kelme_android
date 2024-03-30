package com.kelme.model.request

data class ResetPasswordRequest(
    val password: String,
    val confirm_password: String,
)