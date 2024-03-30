package com.kelme.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GlobalCountrySearchModel(
    val country_code: String,
    val country_id: String,
    val country_name: String
):Parcelable