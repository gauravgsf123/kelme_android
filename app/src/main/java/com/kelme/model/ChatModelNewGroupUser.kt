package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatModelNewGroupUser(
    val chatId: String? = "",
    val chatLastMessage: String? = "",
    val chatPic: String? = "",
    val chatMembers: HashMap<String?, Boolean> = HashMap<String?, Boolean>(),
    val chatTitle: String? = "",
    val chatType: String? = "",
    val created: Long? = 0,
    val createrId: String? = "",
    val lastUpdate: Long? = 0,
    val lastUpdates: HashMap<String?, Long> = HashMap<String?, Long>(),
    val lastMessages: HashMap<String, String> = HashMap<String, String>(),
    val ChatMembersDetails: HashMap<String?, ChatMembersDetails> = HashMap<String?, ChatMembersDetails>()
) : Parcelable