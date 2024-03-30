package com.kelme.model.request

data class CallEndRequest(
    var channel_name: String,
    var receiver_firebase_id: List<String>,
    var sender_user_id: String,
    var agora_token : String,
    var call_type : String,
    var reject_by : String,
    var group_type : String,
    var count : String
    )
