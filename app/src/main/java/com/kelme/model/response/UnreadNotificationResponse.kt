package com.kelme.model.response

import com.kelme.model.UnreadMsgModel

data class UnreadNotificationResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: UnreadMsgModel
)
