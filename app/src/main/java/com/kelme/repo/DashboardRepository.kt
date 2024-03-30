package com.kelme.repo

import com.kelme.model.request.*
import com.kelme.network.RetrofitInstance

/**
 * Created by Amit Gupta on 16-05-2021.
 */
class DashboardRepository {

    suspend fun unreadNotification() = RetrofitInstance.apiService?.unreadNotification()

    suspend fun logout() =
        RetrofitInstance.apiService?.logout()

    suspend fun contactList(request: ContactListRequest) =
        RetrofitInstance.apiService?.contactList(request)

    suspend fun getSetting() =
        RetrofitInstance.apiService?.getUserSetting()

    suspend fun updateSetting(request: UpdateSettingRequest) =
        RetrofitInstance.apiService?.updateUserSetting(request)

    suspend fun notificationList() =
        RetrofitInstance.apiService?.notificationList()

    suspend fun notificationDelete(request: NotificationDeleteRequest) =
        RetrofitInstance.apiService?.notificationDelete(request)

    suspend fun notificationDetails(request: NotificationDeleteRequest) =
        RetrofitInstance.apiService?.notificationDetails(request)

    suspend fun deleteAllNotification(request: NotificationDeleteRequest) =
        RetrofitInstance.apiService?.deleteAllNotification(request)

    suspend fun getStaticData(request: StaticDataRequest) =
        RetrofitInstance.apiService?.getStaticData(request)

    suspend fun sosAlert(request: SosAlertRequest) =
        RetrofitInstance.apiService?.sosAlert(request)

    suspend fun checkIn(request: CheckInRequest) =
        RetrofitInstance.apiService?.checkIn(request)

    suspend fun nearbyAlerts(request: NearByRequest) =
        RetrofitInstance.apiService?.nearbyAlerts(request)

    suspend fun nearbyTracking(request: NearByRequest) =
        RetrofitInstance.apiService?.nearbyTracking(request)

    suspend fun trackUser(request: CurrentLocationRequest) =
        RetrofitInstance.apiService?.trackUser(request)
}