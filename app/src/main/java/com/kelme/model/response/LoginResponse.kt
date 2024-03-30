package com.kelme.model.response

import com.kelme.model.UserModel

/**
 * Created by Amit Gupta on 16-05-2021.
 */
data class LoginResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: UserModel
)