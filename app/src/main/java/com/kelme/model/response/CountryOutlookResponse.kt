package com.kelme.model.response

import com.kelme.model.CountryOutlookModel

/**
 * Created by Amit Gupta on 07-07-2021.
 */
data class CountryOutlookResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: List<CountryOutlookModel>
)