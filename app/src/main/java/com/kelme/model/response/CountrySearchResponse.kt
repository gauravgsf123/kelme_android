package com.kelme.model.response

import com.kelme.model.CountrySearchModel

/**
 * Created by Amit Gupta on 16-05-2021.
 */
data class CountrySearchResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: CountrySearchModel
)