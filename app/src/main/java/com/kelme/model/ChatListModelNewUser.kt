package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatListModelNewUser (
    val chatId: String? = "",
    val chatLastMessage: String?= "",
    val chatPic: String?= "",
    val chatMembers:HashMap<String,Boolean> = HashMap<String,Boolean>(),
    val chatTitle: String?= "",
    val chatType: String?= "",
    val created: Long?=0,
    val createrId: String?= "",
    val lastUpdate: Long?=0,
    val lastUpdates:HashMap<String,Long> = HashMap<String,Long>(),
    val ChatMembersDetails:ArrayList<ChatMembersDetails> = ArrayList<ChatMembersDetails>(),
) : Parcelable