package com.kelme.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubCategory(
    val country_management_id: String,
    val description: String,
    val id: String,
    val risk_category_id: String,
    val risk_category_name: String,
    val risk_type_category_id: String,
    val risk_type_name: String,
    val risk_category_desc: String,
    var isSelected: Boolean = false
): Parcelable