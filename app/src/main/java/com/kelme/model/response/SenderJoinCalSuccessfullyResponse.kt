package com.kelme.model.response

data class SenderJoinCalSuccessfullyResponse(var status : Boolean,
                                             var message : String,
                                             var data : Data,
                                             var code : Int){
    data class Data(var success : String)

}
