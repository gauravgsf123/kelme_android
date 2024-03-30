package com.kelme.repo

import com.kelme.model.request.*
import com.kelme.network.RetrofitInstance

/**
 * Created by Amit Gupta on 16-05-2021.
 */
class CountryRepository {

    suspend fun countryOutlookList( ) =
        RetrofitInstance.apiService?.countryOutlookList()

    suspend fun countryList(request: CountryListRequest) =
        RetrofitInstance.apiService?.countryList(request)

    suspend fun allCountryList() =
        RetrofitInstance.apiService?.allCountryList()

    suspend fun getCountryByName(request: GetCountryRequest) =
        RetrofitInstance.apiService?.getCountryByName(request)

    suspend fun globalCountryByName(request: GetGlobalCountryRequest) =
        RetrofitInstance.apiService?.globalCountryByName(request)

    suspend fun otherUser(request: GetOtherUserRequest) =
        RetrofitInstance.apiService?.otherUser(request)

    suspend fun countryOutlookCategory(request: CountryRiskLevelRequest ) =
        RetrofitInstance.apiService?.countryOutlookCategory(request)

    suspend fun countryRiskLevel(request: CountryRiskLevelRequest) =
        RetrofitInstance.apiService?.countryRiskLevel(request)

    suspend fun securityAlertList(request: CountryRiskLevelRequest) =
        RetrofitInstance.apiService?.securityAlertList(request)

    suspend fun countryCalendar(request: CountryRiskLevelRequest) =
        RetrofitInstance.apiService?.countryCalendar(request)

    suspend fun riskLevelList() = RetrofitInstance.apiService?.riskLevelList()

    suspend fun securityAlertDetails(request: SecurityAlertDetailsRequest) =
        RetrofitInstance.apiService?.securityAlertDetails(request)

    suspend fun logout() = RetrofitInstance.apiService?.logout()


}