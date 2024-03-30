package com.kelme.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


/**
 * Created by Gaurav Kumar on 09/07/21.
 */
@Parcelize
data class SecrityAlertListResponse(@SerializedName("status") var status : Boolean,
                                    @SerializedName("message") var message : String,
                                    @SerializedName("data") var data : List<SecrityAlertListData>,
                                    @SerializedName("code") var code : Int):Parcelable

@Parcelize
data class SecrityAlertListData (
    @SerializedName("security_alert_id") var securityAlertId : String,
    @SerializedName("country_management_id") var countryManagementId : String,
    @SerializedName("title") var title : String,
    @SerializedName("location") var location : String,
    @SerializedName("latitude") var latitude : String,
    @SerializedName("longitude") var longitude : String,
    @SerializedName("risk_type") var risk_type : String,
    @SerializedName("risk_category") var riskCategory : String,
    @SerializedName("risk_description") var riskDescription : String,
    @SerializedName("analysis") var analysis : String,
    @SerializedName("background") var background : String,
    @SerializedName("intel_gathering") var intelGathering : String,
    @SerializedName("media_file") var mediaFile : String,
    @SerializedName("security_advice") var securityAdvice : String,
    @SerializedName("status") var status : String,
    @SerializedName("created_at") var createdAt : String,
    @SerializedName("updated_at") var updatedAt : String,
    @SerializedName("country_id") var countryId : String,
    @SerializedName("country_flag") var countryFlag : String,
    @SerializedName("country_name") var countryName : String,
    @SerializedName("country_code") var countryCode : String,
    @SerializedName("category_name") var categoryName : String,
    @SerializedName("riskType") var riskType : String
):Parcelable