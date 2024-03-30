package com.kelme.network

import com.kelme.model.request.*
import com.kelme.model.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Created by Amit on 10,June,2021
 */

interface Apis {
    //login view modal
    @POST("sign_in")
    suspend fun signIn(@Body request: LoginRequest): Response<LoginResponse>

    @POST("forget_password")
    suspend fun forgetPassword(@Body request: ForgetPasswordRequest): Response<ForgetPasswordResponse>

    @POST("change_forget_password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<CommonResponse>

    @POST("verify_otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<CommonResponse>

    @POST("insert_user_tracking")
    suspend fun trackUser(@Body request: CurrentLocationRequest): Response<CommonResponse>

    //dashboard view modal
    @POST("logout")
    suspend fun logout(): Response<CommonResponse>

    @POST("contact_list")
    suspend fun contactList(@Body request: ContactListRequest): Response<ContactListResponse>

    @POST("get_user_setting")
    suspend fun getUserSetting(): Response<SettingResponse>

    @POST("update_user_setting")
    suspend fun updateUserSetting(@Body request: UpdateSettingRequest): Response<SettingResponse>

    @POST("notofication_list")
    suspend fun notificationList(): Response<NotificationListResponse>

    @POST("notofication_delete")
    suspend fun notificationDelete(@Body request: NotificationDeleteRequest): Response<CommonResponse>

    @POST("notofication_detail")
    suspend fun notificationDetails(@Body request: NotificationDeleteRequest): Response<CommonResponse>

    @POST("delete_all_notification_list")
    suspend fun deleteAllNotification(@Body request: NotificationDeleteRequest): Response<CommonResponse>

    @POST("get_static_page")
    suspend fun getStaticData(@Body request: StaticDataRequest): Response<StaticDataResponse>

    @POST("send_sos_alert")
    suspend fun sosAlert(@Body request: SosAlertRequest): Response<SosAlertResponse>

    @POST("user_check_in")
    suspend fun checkIn(@Body request: CheckInRequest): Response<CheckInResponse>

    @POST("near_by_alerts")
    suspend fun nearbyAlerts(@Body request: NearByRequest): Response<NearbyAlertsResponse>

    @POST("get_near_by_user_tracking")
    suspend fun nearbyTracking(@Body request: NearByRequest): Response<NearbyTrackingResponse>

    @POST("country_outlook")
    suspend fun countryOutlookList(): Response<CountryOutlookResponse>

    @POST("country_list")
    suspend fun countryList(@Body request: CountryListRequest): Response<CountryListResponse>

    @POST("country_list")
    suspend fun allCountryList(): Response<CountryListResponse>

    @POST("get_country_by_name")
    suspend fun getCountryByName(@Body request: GetCountryRequest): Response<CountrySearchResponse>

    @POST("global_country_list")
    suspend fun globalCountryByName(@Body request: GetGlobalCountryRequest): Response<GlobalCountrySearchResponse>

    @POST("other_user_profile")
    suspend fun otherUser(@Body request: GetOtherUserRequest): Response<GetOtherUserResponse>

    @POST("country_outlook_category")
    suspend fun countryOutlookCategory(@Body request: CountryRiskLevelRequest): Response<CountryOutlookCategoryResponse>

    @POST("country_risk_levels")
    suspend fun countryRiskLevel(@Body request: CountryRiskLevelRequest): Response<CountryRiskLevelResponse>

    @POST("security_alert_list")
    suspend fun securityAlertList(@Body request: CountryRiskLevelRequest): Response<SecrityAlertListResponse>

    @POST("country_calendar")
    suspend fun countryCalendar(@Body request: CountryRiskLevelRequest): Response<CalendarResponse>

    //my profile view modal
    @POST("my_profile")
    suspend fun myProfile(): Response<MyProfileResponse>

    @POST("change_password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>

    //security view modal
    @POST("security_alert_list")
    suspend fun securityAlertList(@Body request: SecurityAlertListRequest): Response<SecurityAlertListResponse>

    @POST("security_alert_detail")
    suspend fun securityAlertDetails(@Body request: SecurityAlertDetailsRequest): Response<SecurityAlertDetailsResponse>

    @POST("safety_check_alert")
    suspend fun safetyCheckAlert(@Body request: SafetyCheckAlertRequest): Response<SafetyCheckAlertResponse>

    //chat view model
    @POST("chat_list")
    suspend fun chatList(@Body request: ChatListRequest): Response<ChatListResponse>

    @POST("risk_category_list")
    suspend fun riskLevelList(): Response<RiskLevelListResponse>

    @POST("notification_unread")
    suspend fun unreadNotification(): Response<UnreadNotificationResponse>

    @POST("update_user_profile")
    suspend fun updateUserProfile(
        @Body requestBody: RequestBody
    ): Response<UpdaeUserProfieResponse?>?

    @Multipart
    @POST("upload_document")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("title") name: RequestBody?
    ): Response<UploadDocumentResponse?>?

    @POST("delete_document")
    suspend fun deleteDocument(@Body request: DeleteDocumentRequest): Response<DeleteDocumentResponse>

    //voip
    @POST("get_token_agora")
    suspend fun getTokenAgora(@Body request: GetTokenAgoraRequest): Response<GetTokenAgoraResponse>

    @POST("get_voip_token")
    suspend fun getVoipToken(@Body request: GetVoipTokenRequest): Response<GetVoipTokenResponse>

    @POST("other_user_request_call")
    suspend fun otherUserRequestCall(@Body request: OtherUserRequestCallRequest): Response<OtherUserRequestCallResponse>

    @POST("sender_join_call_successfully")
    suspend fun senderJoinCalSuccessfully(@Body request: SenderJoinCallSuccessfullyRequest): Response<SenderJoinCalSuccessfullyResponse>

    @POST("other_user_join_call_successfully")
    suspend fun otherUserJoinCallSuccessfully(@Body request: OtherUserJoinCallSuccessfullyRequest): Response<OtherUserJoinCallSuccessfullyResponse>

    @POST("sender_can_not_join_call")
    suspend fun senderCanNotJoinCall(@Body request: SenderCanNotJoinCallRequest): Response<SenderCanNotJoinCallResponse>

    @POST("other_user_join_reject_call")
    suspend fun otherUserjoinRejectCall(@Body request: OtherUserJoinRejectCallRequest): Response<OtherUserJoinCallSuccessfullyResponse>

    @POST("call_end")
    suspend fun callEnd(@Body request: CallEndRequest): Response<CallEndResponse>

}