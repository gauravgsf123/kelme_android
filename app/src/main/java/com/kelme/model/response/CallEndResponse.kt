package com.kelme.model.response

data class CallEndResponse(var status : Boolean,
                           var message : String,
                           var data : Data,
                           var code : Int){
    data class Data(var message : String,
                    var channel_name : String)

}

