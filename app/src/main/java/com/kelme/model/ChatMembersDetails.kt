package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMembersDetails(
    var memberDelete: Long? = 0,
    var memberId: String?= " ",
    var memberJoin: Long?= 0,
    var memberLeave: Long?= 0,
    var allChatDelete: Long?= 0
) : Parcelable