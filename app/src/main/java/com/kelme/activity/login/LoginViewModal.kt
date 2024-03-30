package com.kelme.activity.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelme.R
import com.kelme.model.ForgetPasswordModal
import com.kelme.model.UserModel
import com.kelme.model.request.ForgetPasswordRequest
import com.kelme.model.request.LoginRequest
import com.kelme.model.request.ResetPasswordRequest
import com.kelme.model.request.VerifyOtpRequest
import com.kelme.model.response.CommonResponse
import com.kelme.model.response.ForgetPasswordResponse
import com.kelme.model.response.LoginResponse
import com.kelme.repo.LoginRepository
import com.kelme.utils.Resource
import com.kelme.utils.Utils
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Created by Amit Gupta on 16-05-2021.
 */
class LoginViewModal(private val app: Application) :
    ViewModel() {

    private val repository = LoginRepository()

    val user = MutableLiveData<Resource<UserModel>>()
    val forgetPassword = MutableLiveData<Resource<ForgetPasswordModal>>()
    val verifyOtp = MutableLiveData<Resource<String>>()
    val resetPassword = MutableLiveData<Resource<String>>()

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
                    Resource.Error(res.message, res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }

    fun login(request: LoginRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            user.postValue(Resource.Loading())
            val response = repository.signIn(request)
            user.postValue(handleLoginResponse(response))
        } else {
            user.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleLoginResponse(response: Response<LoginResponse>?): Resource<UserModel> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    res.data.let { Resource.Success(res.message, it) }
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun forgetPassword(request: ForgetPasswordRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            forgetPassword.postValue(Resource.Loading())
            val response = repository.forgetPassword(request)
            forgetPassword.postValue(handleForgetResponse(response))
        } else {
            forgetPassword.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleForgetResponse(response: Response<ForgetPasswordResponse>?): Resource<ForgetPasswordModal> {
        if (response?.isSuccessful!!) {
            response.body()?.let { res ->
                return if (res.status) {
                    Resource.Success(res.message, res.data)
                } else {
                    Resource.Error(res.message)
                }
            }
        }
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun verifyOtp(request: VerifyOtpRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            verifyOtp.postValue(Resource.Loading())
            val response = repository.verifyOtp(request)
            verifyOtp.postValue(handleOtpResponse(response))
        } else {
            verifyOtp.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
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
        return Resource.Error(app.resources.getString(R.string.something_went_wrong))
    }


    fun resetPassword(request: ResetPasswordRequest) = viewModelScope.launch {
        if (Utils.hasInternetConnection(app.applicationContext)) {
            resetPassword.postValue(Resource.Loading())
            val response = repository.resetPassword(request)
            resetPassword.postValue(handleResetPasswordResponse(response))
        } else {
            resetPassword.postValue(Resource.Error(app.resources.getString(R.string.no_internet)))
        }
    }

    private fun handleResetPasswordResponse(response: Response<CommonResponse>?): Resource<String> {
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


}