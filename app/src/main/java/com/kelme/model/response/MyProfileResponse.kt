package com.kelme.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MyProfileResponse(
    val code: Int,
    val data: MyProfileData,
    val message: String,
    val status: Boolean
):Parcelable

@Parcelize
data class MyProfileData(
    val address: String?,
    val country_id: String?,
    val country_name: String?,
    val document: List<DocumentData>?,
    val email: String?,
    val gender: String?,
    val image: String?,
    val document_id: String?,
    val name: String?,
    val phone_number: String?,
    val role: String?,
    val user_id: String?
):Parcelable

@Parcelize
data class DocumentData(@SerializedName("document_id") var documentId : String,
                        @SerializedName("user_id") var userId : String,
                        @SerializedName("title") var title : String,
                        @SerializedName("document") var document : String,
                        @SerializedName("status") var status : String,
                        @SerializedName("created_at") var createdAt : String,
                        @SerializedName("updated_at") var updatedAt : String):Parcelable