package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GlobalCountryModel(
    val country_id: String,
    val country_code: String,
    val country_name: String
) : Parcelable
