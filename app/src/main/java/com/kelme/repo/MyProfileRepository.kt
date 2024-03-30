package com.kelme.repo

import com.kelme.model.request.ChangePasswordRequest
import com.kelme.model.request.DeleteDocumentRequest
import com.kelme.network.RetrofitInstance
import okhttp3.MultipartBody
import okhttp3.RequestBody


/**
 * Created by Gaurav Kumar on 08/07/21.
 */
class MyProfileRepository {

    suspend fun myProfile() =
        RetrofitInstance.apiService?.myProfile()

    suspend fun changePassword(request: ChangePasswordRequest) =
        RetrofitInstance.apiService?.changePassword(request)

    suspend fun updateUserProfile(requestBody: RequestBody) =
        RetrofitInstance.apiService?.updateUserProfile(requestBody)

    suspend fun uploadDocument(filePart: MultipartBody.Part,
                                  title: RequestBody?) =
        RetrofitInstance.apiService?.uploadDocument(filePart,title)

    suspend fun deleteDocument(request: DeleteDocumentRequest) =
        RetrofitInstance.apiService?.deleteDocument(request)

    suspend fun logout() =
        RetrofitInstance.apiService?.logout()
}