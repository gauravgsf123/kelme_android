package com.kelme.model.request

data class SenderCanNotJoinCallRequest(var channel_name : String,
                                       var user_id : String,
                                       var call_status : String)
