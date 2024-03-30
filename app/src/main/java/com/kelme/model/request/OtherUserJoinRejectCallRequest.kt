package com.kelme.model.request

/**
 * Created by GAURAV KUMAR on 10,January,2022
 * Quytech,
 */
data class OtherUserJoinRejectCallRequest(val channel_name: String,
                                          val receiver_user_id: List<String>,
                                          val sender_user_id: String,
                                          var agora_token : String,
                                          var call_type : String,
                                          var reject_by : String)
