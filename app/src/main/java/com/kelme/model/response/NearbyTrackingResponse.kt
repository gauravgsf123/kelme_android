package com.kelme.model.response

import com.kelme.model.NearByTrackingModel

/**
 * Created by Amit Gupta on 16-05-2021.
 */
data class NearbyTrackingResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: List<NearByTrackingModel>
)