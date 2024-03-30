package com.kelme.model.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RiskLevelListResponse(
    val code: Int,
    val `data`: List<RiskLevelListData>,
    val message: String,
    val status: Boolean
):Parcelable

@Parcelize
data class RiskLevelListData(
    val name: String,
    val risk_category_id: String,
    val status: String
):Parcelable