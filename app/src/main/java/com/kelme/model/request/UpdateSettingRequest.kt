package com.kelme.model.request

data class UpdateSettingRequest(
    val enable_tracking: String,
    val push_notification: String
)