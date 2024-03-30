package com.kelme.model.request


/**
 * Created by Gaurav Kumar on 08/07/21.
 */
data class ChangePasswordRequest(val old_password: String,
                                 val new_password: String,
                                 val confirm_password: String)
