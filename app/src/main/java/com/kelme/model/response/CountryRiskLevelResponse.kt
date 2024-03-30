package com.kelme.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kelme.model.OtherModel
import com.kelme.model.SecurityModel
import com.kelme.model.SubCategoryModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CountryRiskLevelResponse(
    @SerializedName("status") var status : Boolean,
    @SerializedName("message") var message : String,
    @SerializedName("data") var data : CountryRiskLevelData,
    @SerializedName("code") var code : Int
):Parcelable

@Parcelize
data class CountryRiskLevelData (
    @SerializedName("country_management_id") var countryManagementId : String,
    @SerializedName("country_id") var countryId : String,
    @SerializedName("capital") var capital : String,
    @SerializedName("population") var population : String,
    @SerializedName("official_language") var officialLanguage : String,
    @SerializedName("currency_id") var currencyId : String,
    @SerializedName("dial_code") var dialCode : String,
    @SerializedName("fire_no") var fireNo : String,
    @SerializedName("electricity") var electricity : String,
    @SerializedName("risk_type_id") var riskTypeId : String,
    @SerializedName("country_flag") var countryFlag : String,
    @SerializedName("country_outlook_id") var countryOutlookId : String,
    @SerializedName("area") var area : String,
    @SerializedName("religion_id") var religionId : String,
    @SerializedName("time_zone_id") var timeZoneId : String,
    @SerializedName("ambulance_no") var ambulanceNo : String,
    @SerializedName("police_no") var policeNo : String,
    @SerializedName("risk_assesment_id") var riskAssesmentId : String,
    @SerializedName("security_summary") var securitySummary : String,
    @SerializedName("security_terrorism") var securityTerrorism : String,
    @SerializedName("security_crime") var securityCrime : String,
    @SerializedName("geo_political") var geoPolitical : String,
    @SerializedName("security_kidnapping") var securityKidnapping : String,
    @SerializedName("security_social_stability") var securitySocialStability : String,
    @SerializedName("business_risk") var businessRisk : String,
    @SerializedName("other_culture") var otherCulture : String,
    @SerializedName("other_security_transportation") var otherSecurityTransportation : String,
    @SerializedName("other_health") var otherHealth : String,
    @SerializedName("status") var status : String,
    @SerializedName("created_at") var createdAt : String,
    @SerializedName("updated_at") var updatedAt : String,
    @SerializedName("risk_type_name") var riskTypeName : String,
    @SerializedName("timezone_name") var timezoneName : String,
    @SerializedName("country_name") var countryName : String,
    @SerializedName("religionName") var religionName : String,
    @SerializedName("cuurencyName") var cuurencyName : String,
    @SerializedName("safety_check") var safety_check : String,
    @SerializedName("is_safety_check") var is_safety_check : String,
    @SerializedName("security") var security : List<Security>?,
    @SerializedName("other") var other : List<Other>?,
    @SerializedName("sub_category") var subCategory : List<SubCategory>?

):Parcelable

@Parcelize
data class SubCategory (

    @SerializedName("id") var id : String,
    @SerializedName("country_management_id") var countryManagementId : String,
    @SerializedName("risk_category_id") var riskCategoryId : String,
    @SerializedName("risk_type_category_id") var riskTypeCategoryId : String,
    @SerializedName("risk_type_name") var riskTypeName : String,
    @SerializedName("risk_category_name") var riskCategoryName : String,
    @SerializedName("risk_category_desc") var riskCategoryDesc : String,
    @SerializedName("description") var description : String

):Parcelable

@Parcelize
data class Other (

    @SerializedName("title") var title : String,
    @SerializedName("description") var description : String

):Parcelable

@Parcelize
data class Security (
    @SerializedName("title") var title : String,
    @SerializedName("description") var description : String
):Parcelable