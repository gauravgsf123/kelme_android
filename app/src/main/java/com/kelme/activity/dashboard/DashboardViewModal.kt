package com.kelme.activity.dashboard

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelme.R
import com.kelme.model.*
import com.kelme.model.request.*
import com.kelme.model.response.*
import com.kelme.repo.DashboardRepository
import com.kelme.utils.Resource
import com.kelme.utils.Utils
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Created by Amit Gupta on 16-05-2021.
 */
class DashboardViewModal(private val app: Application) :
    ViewModel() {

    private val repository = DashboardRepository()

    val logout = MutableLiveData<Resource<String>>()
    val notificationCount = MutableLiveData<Resource<UnreadMsgModel>>()
    val contactList = MutableLiveData<Resource<List<ContactUserDetailsModel>>>()
    val setting = MutableLiveData<Resource<SettingModel>>()
    val updateSetting = MutableLiveData<Resource<SettingModel>>()
    val notificationList = MutableLiveData<Resource<DataNotificationModel>>()
    private val notificationDelete = MutableLiveData<Resource<String>>()
    private val notificationDetails = MutableLiveData<Resource<String>>()
    val notificationDeleteAll = MutableLiveData<Resource<String>>()
    val staticData = MutableLiveData<Resource<StaticDataModel>>()
    val sosAlert = MutableLiveData<Resource<SosAlertModel>>()
    val checkIn = MutableLiveData<Resource<CheckInModel>>()
    val nearbyAlerts = MutableLiveData<Resource<List<NearByAlertsModel>>>()
    val nearbyTracking = MutableLiveData<Resource<List<NearByTrackingModel>>>()
    val verifyCurrentLocation = MutableLiveData<Resource<String>>()

    fun logout() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            logout.postValue(Resource.Loading())
            val response = repository.logout()
            logout.postValue(handleLogoutResponse(response))
        } else {
            logout.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleLogoutResponse(response: Response<CommonResponse>?): Resource<String>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.message)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun unreadNotification() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            notificationCount.postValue(Resource.Loading())
            val response = repository.unreadNotification()
            notificationCount.postValue(handleNotificationCountResponse(response))
        } else {
            notificationCount.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleNotificationCountResponse(response: Response<UnreadNotificationResponse>?): Resource<UnreadMsgModel> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun contactList(request: ContactListRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            contactList.postValue(Resource.Loading())
            val response = repository.contactList(request)
            contactList.postValue(handleContactListResponse(response))
        } else {
            contactList.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleContactListResponse(response: Response<ContactListResponse>?): Resource<List<ContactUserDetailsModel>>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun getSetting() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            setting.postValue(Resource.Loading())
            val response = repository.getSetting()
            setting.postValue(handleSettingResponse(response))
        } else {
            setting.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    fun updateSetting(request: UpdateSettingRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            updateSetting.postValue(Resource.Loading())
            val response = repository.updateSetting(request)
            updateSetting.postValue(handleSettingResponse(response))
        } else {
            updateSetting.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleSettingResponse(response: Response<SettingResponse>?): Resource<SettingModel>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun notificationList() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            notificationList.postValue(Resource.Loading())
            val response = repository.notificationList()
            notificationList.postValue(handleNotificationListResponse(response))
        } else {
            notificationList.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleNotificationListResponse(response: Response<NotificationListResponse>?):
            Resource<DataNotificationModel>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status)
                {
                    Resource.Success(res.message, res.data)
                }
                else
                {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun notificationDelete(request: NotificationDeleteRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext))
        {
            notificationDelete.postValue(Resource.Loading())
            val response = repository.notificationDelete(request)
            notificationDelete.postValue(handleNotificationDeleteResponse(response))
        }
        else {
            notificationDelete.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleNotificationDeleteResponse(response: Response<CommonResponse>?): Resource<String> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.message)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun notificationDetails(request: NotificationDeleteRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            notificationDetails.postValue(Resource.Loading())
            val response = repository.notificationDetails(request)
            notificationDetails.postValue(handleNotificationDetailsResponse(response))
        } else {
            notificationDetails.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleNotificationDetailsResponse(response: Response<CommonResponse>?): Resource<String>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.message)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun notificationDeleteAll(request: NotificationDeleteRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            notificationDeleteAll.postValue(Resource.Loading())
            val response = repository.deleteAllNotification(request)
            notificationDeleteAll.postValue(handleNotificationDeleteAllResponse(response))
        } else {
            notificationDeleteAll.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleNotificationDeleteAllResponse(response: Response<CommonResponse>?): Resource<String>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.message)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun getStaticData(request: StaticDataRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            staticData.postValue(Resource.Loading())
            val response = repository.getStaticData(request)
            staticData.postValue(handleStaticDataResponse(response))
        } else {
            staticData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleStaticDataResponse(response: Response<StaticDataResponse>?): Resource<StaticDataModel>? {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun sosAlert(request: SosAlertRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            sosAlert.postValue(Resource.Loading())
            val response = repository.sosAlert(request)
            sosAlert.postValue(handleSosAlertResponse(response))
        } else {
            sosAlert.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleSosAlertResponse(response: Response<SosAlertResponse>?): Resource<SosAlertModel> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun checkIn(request: CheckInRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            checkIn.postValue(Resource.Loading())
            val response = repository.checkIn(request)
            checkIn.postValue(handleCheckInResponse(response))
        } else {
            checkIn.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleCheckInResponse(response: Response<CheckInResponse>?): Resource<CheckInModel> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun nearbyAlerts(request: NearByRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            nearbyAlerts.postValue(Resource.Loading())
            val response = repository.nearbyAlerts(request)
            nearbyAlerts.postValue(handleNearbyResponse(response))
        } else {
            nearbyAlerts.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleNearbyResponse(response: Response<NearbyAlertsResponse>?): Resource<List<NearByAlertsModel>> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun nearbyTracking(request: NearByRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            nearbyTracking.postValue(Resource.Loading())
            val response = repository.nearbyTracking(request)
            nearbyTracking.postValue(handleNearbyTrackingResponse(response))
        } else {
            nearbyTracking.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleNearbyTrackingResponse(response: Response<NearbyTrackingResponse>?): Resource<List<NearByTrackingModel>> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    if (res.code == 240) {
                        Resource.Error(res.code.toString())
                    } else {
                        Resource.Error(res.message)
                    }
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun trackUser(request: CurrentLocationRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            verifyCurrentLocation.postValue(Resource.Loading())
            val response = repository.trackUser(request)
            verifyCurrentLocation.postValue(handleOtpResponse(response))
        } else {
            verifyCurrentLocation.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleOtpResponse(response: Response<CommonResponse>?): Resource<String> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message,res.message)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error("")
    }
}