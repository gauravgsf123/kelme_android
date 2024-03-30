package com.kelme.model

data class NotificationModel(
    val body: String,
    val created_at: String,
    val incoming_notification_id: String,
    val message: String,
    val attachment: String,
    val title: String,
    val module_type: String,
    val module_id: String,
    val latitude: String,
    val longitude: String,
    val saftey_check: String,
    val firebase_id: String
)