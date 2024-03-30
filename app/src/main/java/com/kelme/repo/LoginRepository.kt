package com.kelme.repo

import com.kelme.model.request.ForgetPasswordRequest
import com.kelme.model.request.LoginRequest
import com.kelme.model.request.ResetPasswordRequest
import com.kelme.model.request.VerifyOtpRequest
import com.kelme.network.RetrofitInstance

/**
 * Created by Amit Gupta on 16-05-2021.
 */
class LoginRepository {

    suspend fun signIn(request: LoginRequest) =
        RetrofitInstance.apiService?.signIn(request)

    suspend fun forgetPassword(request: ForgetPasswordRequest) =
        RetrofitInstance.apiService?.forgetPassword(request)

    suspend fun resetPassword(request: ResetPasswordRequest) =
        RetrofitInstance.apiService?.resetPassword(request)

    suspend fun verifyOtp(request: VerifyOtpRequest) =
        RetrofitInstance.apiService?.verifyOtp(request)

    suspend fun logout() =
        RetrofitInstance.apiService?.logout()
}