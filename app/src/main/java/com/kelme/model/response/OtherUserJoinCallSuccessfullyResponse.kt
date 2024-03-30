package com.kelme.model.response

/**
 * Created by GAURAV KUMAR on 10,January,2022
 * Quytech,
 */
data class OtherUserJoinCallSuccessfullyResponse(var status : Boolean,
                                                 var message : String,
                                                 var code : Int,
                                                 var data: Data
){
    data class Data(var success : String)
}
