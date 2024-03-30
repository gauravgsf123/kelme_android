package com.kelme.model


data class SafetyCheckAlertModel(
    val notification_content_id: Int,
    val from_user_id: String,
    val module_type: Int,
    val module_id: String,
    val saftey_check: String
)