package com.kelme.model.response

import com.kelme.model.SafetyCheckAlertModel

/**
 * Created by Amit Gupta on 08-07-2021.
 */
data class SafetyCheckAlertResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: SafetyCheckAlertModel
)