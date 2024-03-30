package com.kelme.model.response

data class OtherUserRequestCallResponse(var status : Boolean,
                                        var message : String,
                                        var data : Data,
                                        var code : Int)
{
    data class Data(var message : String,
                    var channelName : String,
                    var agoraToken : String)
}