package com.kelme.model.response

import com.kelme.model.CheckInModel

/**
 * Created by Amit Gupta on 08-07-2021.
 */
data class CheckInResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: CheckInModel
)