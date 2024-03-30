package com.kelme.model.response

import com.kelme.model.SettingModel

/**
 * Created by Amit Gupta on 08-07-2021.
 */
data class SettingResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: SettingModel
)