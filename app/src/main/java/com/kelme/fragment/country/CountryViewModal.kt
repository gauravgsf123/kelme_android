package com.kelme.fragment.country

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelme.R
import com.kelme.model.*
import com.kelme.model.request.*
import com.kelme.model.response.*
import com.kelme.repo.CountryRepository
import com.kelme.utils.Resource
import com.kelme.utils.Utils
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Created by Amit Gupta on 16-05-2021.
 */
class CountryViewModal(private val app: Application) :
    ViewModel() {

    private val repository = CountryRepository()

    val countryOutlookList = MutableLiveData<Resource<List<CountryOutlookModel>>?>()
    val countryList = MutableLiveData<Resource<List<CountryModel>>??>()
    val countrySearchData = MutableLiveData<Resource<CountrySearchModel>?>()
    val globalCountrySearchData = MutableLiveData<Resource<List<GlobalCountryModel>>>()
    val getOtherUserData = MutableLiveData<Resource<OtherUserDetailsModel>>()
    val countryRiskLevelData = MutableLiveData<Resource<CountryRiskLevelData>?>()
    val secrityAlertListData = MutableLiveData<Resource<List<SecrityAlertListData>>?>()
    val calendarData = MutableLiveData<Resource<List<CalendarData>>>()
    val riskLevelData = MutableLiveData<Resource<List<RiskLevelListData>>?>()
    val countryOutlookCategoryData = MutableLiveData<Resource<List<CountryOutlookCategoryData>>?>()
    val securityAlertDetails = MutableLiveData<Resource<SecurityAlertDetailsModel>?>()

    val logout = MutableLiveData<Resource<String>?>()
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


    fun countryOutlookList() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            countryOutlookList.postValue(Resource.Loading())
            val response = repository.countryOutlookList()
            countryOutlookList.postValue(handleCountryOutlookResponse(response))
        } else {
            countryOutlookList.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleCountryOutlookResponse(response: Response<CountryOutlookResponse>?): Resource<List<CountryOutlookModel>>? {
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


    fun countryList(request: CountryListRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            countryList.postValue(Resource.Loading())
            val response = repository.countryList(request)
            countryList.postValue(handleCountryResponse(response))
        } else {
            countryList.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleCountryResponse(response: Response<CountryListResponse>?): Resource<List<CountryModel>>? {
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

    fun allCountryList() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            countryList.postValue(Resource.Loading())
            val response = repository.allCountryList()
            countryList.postValue(handleCountryResponse(response))
        } else {
            countryList.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    fun getCountryByName(request: GetCountryRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            countrySearchData.postValue(Resource.Loading())
            val response = repository.getCountryByName(request)
            countrySearchData.postValue(handleCountrySearchResponse(response))
        } else {
            countrySearchData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    fun globalCountryByName(request: GetGlobalCountryRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            globalCountrySearchData.postValue(Resource.Loading())
            val response = repository.globalCountryByName(request)
            globalCountrySearchData.postValue(handleGlobalCountrySearchResponse(response))
        } else {
            globalCountrySearchData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    fun otherUser(request: GetOtherUserRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            getOtherUserData.postValue(Resource.Loading())
            val response = repository.otherUser(request)
            getOtherUserData.postValue(handleOtherUserDetails(response))
        } else {
            getOtherUserData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleOtherUserDetails(response: Response<GetOtherUserResponse>?): Resource<OtherUserDetailsModel> {
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


    private fun handleGlobalCountrySearchResponse(response: Response<GlobalCountrySearchResponse>?): Resource<List<GlobalCountryModel>> {
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

    private fun handleCountrySearchResponse(response: Response<CountrySearchResponse>?): Resource<CountrySearchModel>? {
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

    fun countryOutlookCategory(request: CountryRiskLevelRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            countryOutlookCategoryData.postValue(Resource.Loading())
            val response = repository.countryOutlookCategory(request)
            countryOutlookCategoryData.postValue(handleCountryOutlookCategoryResponse(response))
        } else {
            countryOutlookCategoryData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleCountryOutlookCategoryResponse(response: Response<CountryOutlookCategoryResponse>?): Resource<List<CountryOutlookCategoryData>>? {
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

    fun countryRiskLevel(request: CountryRiskLevelRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            countryRiskLevelData.postValue(Resource.Loading())
            val response = repository.countryRiskLevel(request)
            countryRiskLevelData.postValue(handleCountryRiskLevelResponse(response))
        } else {
            countryRiskLevelData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleCountryRiskLevelResponse(response: Response<CountryRiskLevelResponse>?): Resource<CountryRiskLevelData>? {
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

    fun securityAlertList(request: CountryRiskLevelRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            secrityAlertListData.postValue(Resource.Loading())
            val response = repository.securityAlertList(request)
            secrityAlertListData.postValue(handleSecurityAlertListResponse(response))
        } else {
            secrityAlertListData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleSecurityAlertListResponse(response: Response<SecrityAlertListResponse>?): Resource<List<SecrityAlertListData>>? {
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

    fun countryCalenderData(request: CountryRiskLevelRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            calendarData.postValue(Resource.Loading())
            val response = repository.countryCalendar(request)
            calendarData.postValue(handleCountryCalenderDataResponse(response))
        } else {
            calendarData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleCountryCalenderDataResponse(response: Response<CalendarResponse>?): Resource<List<CalendarData>> {
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

    fun riskLevelList() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            riskLevelData.postValue(Resource.Loading())
            val response = repository.riskLevelList()
            riskLevelData.postValue(handleRiskLevelListResponse(response))
        } else {
            riskLevelData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleRiskLevelListResponse(response: Response<RiskLevelListResponse>?): Resource<List<RiskLevelListData>>? {
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

    fun securityAlertDetails(request: SecurityAlertDetailsRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            securityAlertDetails.postValue(Resource.Loading())
            val response = repository.securityAlertDetails(request)
            securityAlertDetails.postValue(handleSecurityAlertDetailsResponse(response))
        } else {
            securityAlertDetails.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleSecurityAlertDetailsResponse(response: Response<SecurityAlertDetailsResponse>?): Resource<SecurityAlertDetailsModel>? {
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