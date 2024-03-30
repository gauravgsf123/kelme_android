package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupDetailsModel(
    val chatId: String = " ",
    var chatPic: String?= " ",
    val chatMembers:HashMap<String,Boolean> = HashMap<String,Boolean>(),
    val chatMembersDetails:HashMap<String,ChatMembersDetails> = HashMap<String,ChatMembersDetails>(),
    val created: Long?=0,
    val createrId: String?= " ",
    val lastUpdate: Long?=0,
    var name: String? = ""
) : Parcelable
