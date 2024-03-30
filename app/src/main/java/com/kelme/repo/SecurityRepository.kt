package com.kelme.repo

import com.kelme.model.request.SafetyCheckAlertRequest
import com.kelme.model.request.SecurityAlertDetailsRequest
import com.kelme.model.request.SecurityAlertListRequest
import com.kelme.network.RetrofitInstance

/**
 * Created by Amit Gupta on 08-07-2021.
 */
class SecurityRepository {

    suspend fun securityAlertList(request: SecurityAlertListRequest) =
        RetrofitInstance.apiService?.securityAlertList(request)

    suspend fun securityAlertDetails(request: SecurityAlertDetailsRequest) =
        RetrofitInstance.apiService?.securityAlertDetails(request)

    suspend fun riskLevelList() =
        RetrofitInstance.apiService?.riskLevelList()

    suspend fun safetyCheckAlerts(request: SafetyCheckAlertRequest) =
        RetrofitInstance.apiService?.safetyCheckAlert(request)

    suspend fun logout() =
        RetrofitInstance.apiService?.logout()
}