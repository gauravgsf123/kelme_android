package com.kelme.model.response

import com.google.gson.annotations.SerializedName


/**
 * Created by Gaurav Kumar on 13/07/21.
 */
data class UploadDocumentResponse(
    @SerializedName("status") var status: Boolean,
    @SerializedName("message") var message: String,
    @SerializedName("data") var data: UploadDocumentData,
    @SerializedName("code") var code: Int
)

data class UploadDocumentData(
    @SerializedName("title") var title: String,
    @SerializedName("document") var document: String,
    @SerializedName("user_id") var userId: String,
    @SerializedName("created_at") var createdAt: Int,
    @SerializedName("document_id") var document_id: String
)