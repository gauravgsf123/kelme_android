package com.kelme.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatModel (
        var actionBy:String?="",
        var message: String? = "",
        var messageId: String?= "",
        var sender: String?= "",
        var status: Long?=0,
        var timestamp: Long?=0,
        var type: String?= " ",
) : Parcelable