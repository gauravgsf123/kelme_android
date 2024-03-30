package com.kelme.model.response

import com.kelme.model.GlobalCountryModel

data class GlobalCountrySearchResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: List<GlobalCountryModel>
)
