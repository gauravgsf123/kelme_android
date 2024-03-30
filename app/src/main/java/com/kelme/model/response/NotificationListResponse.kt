package com.kelme.model.response

import com.kelme.model.NotificationModel

/**
 * Created by Amit Gupta on 08-07-2021.
 */
data class NotificationListResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: DataNotificationModel
)

data class DataNotificationModel(
    val notification_data: List<NotificationModel>,
    val unread_count:String
)
