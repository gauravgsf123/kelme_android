package com.kelme.fragment.profile

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelme.R
import com.kelme.model.request.ChangePasswordRequest
import com.kelme.model.request.DeleteDocumentRequest
import com.kelme.model.response.*
import com.kelme.repo.MyProfileRepository
import com.kelme.utils.Resource
import com.kelme.utils.Utils
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

/**
 * Created by Gaurav Kumar on 08/07/21.
 */

class MyProfileViewModel(private val app: Application) : ViewModel() {
    private val repository = MyProfileRepository()
    val myProfileResponse = MutableLiveData<Resource<MyProfileData>>()
    val changePasswordResponse = MutableLiveData<Resource<String>>()
    val updaeUserProfieData = MutableLiveData<Resource<UpdaeUserProfieData>>()
    val uploadDocumentData = MutableLiveData<Resource<UploadDocumentData>>()
    val deleteDocumentResponse = MutableLiveData<Resource<String>>()

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

    fun myProfile() = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            myProfileResponse.postValue(Resource.Loading())
            val response = repository.myProfile()
            myProfileResponse.postValue(handleMyProfileResponse(response))
        } else {
            myProfileResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleMyProfileResponse(response: Response<MyProfileResponse>?): Resource<MyProfileData> {
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

    fun changePassword(
        request: ChangePasswordRequest
    ) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            changePasswordResponse.postValue(Resource.Loading())
            val response = repository.changePassword(request)
            changePasswordResponse.postValue(handleChangePasswordResponse(response))
        } else {
            changePasswordResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleChangePasswordResponse(response: Response<ChangePasswordResponse>?): Resource<String> {
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

    fun deleteDocument(
        request: DeleteDocumentRequest
    ) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            deleteDocumentResponse.postValue(Resource.Loading())
            val response = repository.deleteDocument(request)
            deleteDocumentResponse.postValue(handleDeleteDocumentResponse(response))
        } else {
            deleteDocumentResponse.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleDeleteDocumentResponse(response: Response<DeleteDocumentResponse>?): Resource<String> {
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

    fun updateUserProfile(requestBody: RequestBody) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            updaeUserProfieData.postValue(Resource.Loading())
            val response = repository.updateUserProfile(requestBody)
            updaeUserProfieData.postValue(handleUpdateUserProfileResponse(response))
        } else {
            updaeUserProfieData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleUpdateUserProfileResponse(response: Response<UpdaeUserProfieResponse?>?): Resource<UpdaeUserProfieData> {
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

    fun uploadDocument(
        filePart: MultipartBody.Part,
        title: RequestBody?
    ) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            uploadDocumentData.postValue(Resource.Loading())
            val response = repository.uploadDocument(filePart,title)
            uploadDocumentData.postValue(handleupdateUserProfileResponse(response))
        } else {
            uploadDocumentData.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleupdateUserProfileResponse(response: Response<UploadDocumentResponse?>?): Resource<UploadDocumentData> {
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