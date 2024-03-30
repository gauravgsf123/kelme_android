package com.kelme.model.response

data class ChangePasswordResponse(
    val code: Int,
    val message: String,
    val status: Boolean
)

class ChangePasswordData()