package com.kelme.model.response

import com.kelme.model.ChatListModel

/**
 * Created by Amit Gupta on 08-07-2021.
 */

data class ChatListResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: List<ChatListModel>
)