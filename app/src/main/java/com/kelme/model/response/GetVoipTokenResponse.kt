package com.kelme.model.response

data class GetVoipTokenResponse(var status: Boolean,
                                var message: String,
                                var code: Int,
                                var data: Boolean)