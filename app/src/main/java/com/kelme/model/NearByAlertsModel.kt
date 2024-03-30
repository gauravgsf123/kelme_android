package com.kelme.model

data class NearByAlertsModel(
    val analysis: String,
    val background: String,
    val country_management_id: String,
    val created_at: String,
    val distance: String,
    val intel_gathering: String,
    val latitude: String,
    val location: String,
    val longitude: String,
    val media_file: String,
    val risk_category: String,
    val risk_description: String,
    val risk_type: String,
    val security_advice: String,
    val security_alert_id: String,
    val status: String,
    val title: String,
    val updated_at: String
)