package com.kelme.model.response

import com.kelme.model.SecurityAlertDetailsModel

/**
 * Created by Amit Gupta on 16-05-2021.
 */
data class SecurityAlertDetailsResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: SecurityAlertDetailsModel
)