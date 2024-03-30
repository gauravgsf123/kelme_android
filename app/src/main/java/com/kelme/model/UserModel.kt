package com.kelme.model

data class UserModel(
    val address: Any,
    val auth_token: String,
    val country_id: String,
    val country_name: String,
    val document: List<Any>,
    val email: String,
    val gender: String,
    val image: String,
    val name: String,
    val phone_number: String,
    val role: String,
    val user_id: String,
    val company_id: String
)