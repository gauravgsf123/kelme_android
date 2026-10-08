package com.kelme.utils

data class AppUpdateResponse(
    val status: Boolean = false,
    val message: String = "",
    val data: AppUpdateData? = null,
    val code: Int = 0
)

data class AppUpdateData(
    val app_version: String? = null,
    val lattest_version: String? = null
)
