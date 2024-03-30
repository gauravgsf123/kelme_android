package com.kelme.model.response

import android.os.Parcelable
import com.kelme.model.ChatMembersDetails
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatListModelWithName(
    var chatId: String = "",
    var chatLastMessage: String?= "",
    var chatPic: String?= "",
    var chatMembers:HashMap<String,Boolean> = HashMap(),
    var chatTitle: String?= "",
    var chatType: String?= "",
    var created: Long?=0,
    var createrId: String?= "",
    var lastUpdate: Long?=0,
    var lastUpdates:HashMap<String,Long> = HashMap(),
    var chatMemberDetails:HashMap<String, ChatMembersDetails> = HashMap(),
    var lastUpdateTime: Long?=0,
    var name: String? = "",
    var isAdmin: String? = "",
    var unSeenMsg:Int? = 0,
    var isSelected:Boolean = false
) : Parcelable