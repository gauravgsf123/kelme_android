package com.kelme.model.request

/**
 * Created by GAURAV KUMAR on 10,January,2022
 * Quytech,
 */
data class OtherUserJoinCallSuccessfullyRequest(val sender_user_id: String,val channel_name: String, var receiver_user_id : List<String>)
