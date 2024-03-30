package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountryOutlookModel(
    val country_outlook_id: String,
    val country_outlook_name: String,
    val position: String,
    val status: String
) : Parcelable