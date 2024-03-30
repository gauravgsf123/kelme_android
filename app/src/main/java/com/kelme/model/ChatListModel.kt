package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ChatListModel(
    val chatId: String? = " ",
    val chatLastMessage: String?= " ",
    val chatPic: String?= " ",
    val chatMembers:HashMap<String,Boolean> = HashMap(),
    val chatTitle: String?= " ",
    val chatType: String?= " ",
    val created: Long?=0,
    val createrId: String?= " ",
    val lastUpdate: Long?=0,
    val lastUpdates:HashMap<String,Long> = HashMap(),
) : Parcelable

