package com.kelme.model.response

import com.google.gson.annotations.SerializedName


/**
 * Created by Gaurav Kumar on 12/07/21.
 */
data class UpdaeUserProfieResponse(@SerializedName("status") var status : Boolean,
                                   @SerializedName("message") var message : String,
                                   @SerializedName("data") var data : UpdaeUserProfieData,
                                   @SerializedName("code") var code : Int
)

data class UpdaeUserProfieData (
    @SerializedName("name") var name : String,
    @SerializedName("email") var email : String,
    @SerializedName("country_id") var countryId : String,
    @SerializedName("phone_number") var phoneNumber : String,
    @SerializedName("image") var image : String,
    @SerializedName("address") var address : String,
    @SerializedName("role") var role : String,
    @SerializedName("country_name") var countryName : String,
    @SerializedName("gender") var gender : String,
    @SerializedName("document") var document : List<DocumentData>,
    @SerializedName("user_id") var userId : String

)

/*
data class DocumentData(@SerializedName("document_id") var documentId : String,
                        @SerializedName("user_id") var userId : String,
                        @SerializedName("title") var title : String,
                        @SerializedName("document") var document : String,
                        @SerializedName("status") var status : String,
                        @SerializedName("created_at") var createdAt : String,
                        @SerializedName("updated_at") var updatedAt : String
)*/
