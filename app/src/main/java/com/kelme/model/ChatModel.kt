package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatModel(
    val message: String? = " ",
    val messageId: String?= " ",
    val sender: String?= " ",
    val status: Long?=0,
    val timestamp: Long?=0,
    val type:  String?= " ",
) : Parcelable