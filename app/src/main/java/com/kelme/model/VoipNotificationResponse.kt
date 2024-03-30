package com.kelme.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by GAURAV KUMAR on 03,October,2021
 * Quytech,
 */
@Parcelize
data class VoipNotificationResponse(
    var channel_name: String = "",
    var notification_type: String = "",
    var format_type: String = "",
    var sender_id: String = "",
    var receiver_id: String = "",
    var sender_name: String = "",
    var receiver_image: String = "",
    var created_timestamp: Int,
    var type: String = "",
    var call_reject:String="",
    var call_type: String = "",
    var token: String = "",
    var title: String = "",
    var message: String = ""
) : Parcelable