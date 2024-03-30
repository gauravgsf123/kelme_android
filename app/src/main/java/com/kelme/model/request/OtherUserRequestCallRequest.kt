package com.kelme.model.request

data class OtherUserRequestCallRequest(
    var channel_name : String,
    var receiver_firebase_id : List<String>,
    var sender_user_id : Int,
    var agora_token : String,
    var call_type : String,
    var type : String
)
