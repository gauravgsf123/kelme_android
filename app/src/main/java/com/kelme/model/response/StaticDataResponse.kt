package com.kelme.model.response

import com.kelme.model.StaticDataModel

/**
 * Created by Amit Gupta on 16-05-2021.
 */
data class StaticDataResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: StaticDataModel
)