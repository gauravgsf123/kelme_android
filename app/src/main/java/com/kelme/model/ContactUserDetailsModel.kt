package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactUserDetailsModel(
    val image: String? = " ",
    val name: String?= " ",
    val role: String?= " ",
    val user_id: String?= " ",
    val role_id: String?= " ",
    val username: String?= " ",
    val fireBaseId: String?= " "
): Parcelable
