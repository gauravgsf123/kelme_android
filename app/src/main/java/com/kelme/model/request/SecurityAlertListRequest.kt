package com.kelme.model.request

data class SecurityAlertListRequest(
    val country_id: String,
    val time_from: String,
    val time_to: String,
    val search_alert_category: String,
    val search_input: String
)