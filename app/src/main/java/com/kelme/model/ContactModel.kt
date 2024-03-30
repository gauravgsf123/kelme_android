package com.kelme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactModel(
    var deviceToken: String? = " ",
    var deviceType: String?= "",
    var email: String?= " ",
    var isDeleted: Boolean?= true,
    var isNotificationOn: Boolean?= true,
    var isOnline: Boolean?= true,
    var lastSeen: Long?= 0,
    var name: String?= " ",
    var profilePicture: String?= " ",
    var userId: String? = " ",
    var userRole: Long?= 0
) : Parcelable
{
    var isSelected: Boolean = false
}

