package com.kelme.model.response

import com.kelme.model.ForgetPasswordModal

/**
 * Created by Amit Gupta on 16-05-2021.
 */
data class ForgetPasswordResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: ForgetPasswordModal
)