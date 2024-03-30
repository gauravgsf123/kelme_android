package com.kelme.model

/**
 * Created by GAURAV KUMAR on 05,January,2022
 * Quytech,
 */
data class NotificationResponse(
    var notification_type: String,
    var notification_content_id: String,
    var user_id: String,
    var module_type: String,
    var safety_check: String,
    var created_timestamp: Int,
    var message: String,
    var title: String,
    var from_user_id: String,
    var status: Int,
)
