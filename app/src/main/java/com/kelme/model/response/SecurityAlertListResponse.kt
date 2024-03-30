package com.kelme.model.response

import com.kelme.model.SecurityAlertModel

/**
 * Created by Amit Gupta on 08-07-2021.
 */
data class SecurityAlertListResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: List<SecurityAlertModel>
)