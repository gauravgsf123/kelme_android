package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Created by Gaurav Kumar on 05/07/21.
 */
data class ChatConversionModel(
    var message_date: String,
    var message_type: Int,
    var message_model: String
)

