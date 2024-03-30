package com.kelme.model.response

import com.kelme.model.NearByAlertsModel

/**
 * Created by Amit Gupta on 16-05-2021.
 */
data class NearbyAlertsResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: List<NearByAlertsModel>
)