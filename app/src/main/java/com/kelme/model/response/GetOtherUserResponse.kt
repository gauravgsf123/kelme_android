package com.kelme.model.response

import com.kelme.model.GlobalCountryModel
import com.kelme.model.OtherUserDetailsModel

class GetOtherUserResponse (
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: OtherUserDetailsModel
)