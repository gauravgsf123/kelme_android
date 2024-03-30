package com.kelme.fragment.security

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelme.R
import com.kelme.model.SafetyCheckAlertModel
import com.kelme.model.SecurityAlertDetailsModel
import com.kelme.model.SecurityAlertModel
import com.kelme.model.request.SafetyCheckAlertRequest
import com.kelme.model.request.SecurityAlertDetailsRequest
import com.kelme.model.request.SecurityAlertListRequest
import com.kelme.model.response.*
import com.kelme.repo.SecurityRepository
import com.kelme.utils.Resource
import com.kelme.utils.Utils
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Created by Amit Gupta on 08-07-2021.
 */
class SecurityViewModal(private val app: Application) :
    ViewModel() {

    private val repository = SecurityRepository()
    val securityAlertList = MutableLiveData<Resource<List<SecurityAlertModel>>>()
    val securityAlertDetails = MutableLiveData<Resource<SecurityAlertDetailsModel>>()
    val riskLevelData = MutableLiveData<Resource<List<RiskLevelListData>>>()
    val safetyCheckAlerts = MutableLiveData<Resource<SafetyCheckAlertModel>>()

    val logout = MutableLiveData<Resource<String>>()
    fun logout() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            logout.postValue(Resource.Loading())
            val response = repository.logout()
            logout.postValue(handleLogoutResponse(response))
        } else {
            logout.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleLogoutResponse(response: Response<CommonResponse>?): Resource<String> {
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

    fun securityAlertList(request: SecurityAlertListRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            securityAlertList.postValue(Resource.Loading())
            val response = repository.securityAlertList(request)
            securityAlertList.postValue(handleSecurityAlertListResponse(response))
        } else {
            securityAlertList.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleSecurityAlertListResponse(response: Response<SecurityAlertListResponse>?): Resource<List<SecurityAlertModel>> {
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
        return Resource.Error("")
    }

    fun securityAlertDetails(request: SecurityAlertDetailsRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            securityAlertDetails.postValue(Resource.Loading())
            val response = repository.securityAlertDetails(request)
            securityAlertDetails.postValue(handleSecurityAlertDetailsResponse(response))
        } else {
            securityAlertDetails.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleSecurityAlertDetailsResponse(response: Response<SecurityAlertDetailsResponse>?): Resource<SecurityAlertDetailsModel> {
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



    fun safetyCheckAlert(request: SafetyCheckAlertRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            safetyCheckAlerts.postValue(Resource.Loading())
            val response = repository.safetyCheckAlerts(request)
            safetyCheckAlerts.postValue(handleSafetyCheckAlertResponse(response))
        } else {
            safetyCheckAlerts.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleSafetyCheckAlertResponse(response: Response<SafetyCheckAlertResponse>?): Resource<SafetyCheckAlertModel> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(app.resources.getString(R.string.notification_alert), res.data)
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


    fun riskLevelList() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            riskLevelData.postValue(Resource.Loading())
            val response = repository.riskLevelList()
            riskLevelData.postValue(handleRiskLevelListResponse(response))
        } else {
            riskLevelData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleRiskLevelListResponse(response: Response<RiskLevelListResponse>?): Resource<List<RiskLevelListData>> {
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

}