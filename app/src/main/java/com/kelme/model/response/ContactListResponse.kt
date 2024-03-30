package com.kelme.model.response

import com.kelme.model.ContactModel
import com.kelme.model.ContactUserDetailsModel

/**
 * Created by Amit Gupta on 16-05-2021.
 */
data class ContactListResponse(
    var status: Boolean,
    var message: String,
    var code: Int,
    var data: List<ContactUserDetailsModel>
)