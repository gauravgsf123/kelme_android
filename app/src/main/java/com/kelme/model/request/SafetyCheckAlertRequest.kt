package com.kelme.model.request

data class SafetyCheckAlertRequest(
    val security_alert_id: String,
    val saftey_check_type: String,
    val latitude: String,
    val longitude: String,
)