package com.kelme.model.response



data class GetTokenAgoraResponse(var status: Boolean,
                                 var message: String,
                                 var code: Int,
                                 var data: Data
){
    data class Data(var token: String)
}
