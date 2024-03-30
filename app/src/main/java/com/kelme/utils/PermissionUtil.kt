package com.kelme.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Created by GAURAV KUMAR on 08,November,2021
 * Quytech,
 */
object PermissionUtil {

    fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            Constants.PERMISSIONS,
            Constants.PERMISSIONS_REQUEST_CODE
        )
    }

    fun hasPermissions(context: Context,
        vararg permissions: String
    ): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }


}