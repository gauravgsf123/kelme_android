package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SecurityAlertModel(
    val analysis: String,
    val background: String,
    val category_name: String,
    val country_code: String,
    val country_flag: String,
    val country_id: String,
    val country_management_id: String,
    val country_name: String,
    val created_at: String,
    val intel_gathering: String,
    val latitude: String,
    val location: String,
    val longitude: String,
    val media_file: String,
    val riskType: String,
    val risk_category: String,
    val risk_description: String,
    val risk_type: String,
    val security_advice: String,
    val security_alert_id: String,
    val status: String,
    val title: String,
    val updated_at: String
) : Parcelable