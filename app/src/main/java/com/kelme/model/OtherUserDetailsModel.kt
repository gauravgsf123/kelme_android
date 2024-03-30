package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OtherUserDetailsModel(
    var name: String?="",
    val email: String?="",
    val country_id: String?="",
    val phone_number: String?="",
    var image: String?="",
    val address: String?="",
    val country_name: String?="",
    val gender: String?="",
    var user_id: String?=""
): Parcelable
