package com.kelme.utils

import android.Manifest
import android.os.Build
import com.kelme.model.ContactModel

/**
 * Created by Amit on 28,June,2021
 */
object Constants {

    //const val BASE_URL = "https://quytech.net/kelmerisk_live/"
    const val BASE_URL = "https://portal.krisk24.com/"
    const val SERVER_URL = BASE_URL+"api/"
    const val SERVER_IMAGE_URL = BASE_URL+"assets/uploads/country_flag/"

    const val DEVICE_TYPE_ID = "1"    // 1 -> ANDROID 2 -> IOS
    const val COUNTRY_OUTLOOK_MODEL = "countryOutlookModel"
    const val COUNTRY_MODEL = "countryModel"
    const val SINGLE_CHAT_MODEL = "singlechatmodel"
    const val CHATLIST_MODEL = "chatlistModel"
    const val USER_NAME = "user_name"
    const val SECURITY_ALERT_MODEL = "securityAlertModel"
    const val MY_PROFILE_DATA_MODEL = "my_profile_data"
    const val DOCUMENT_DATA = "document_data"
    const val PAGE_NUMBER_KEY  = "true"
    const val DATA = "data"
    const val SECURITY_ALERT_DATA_LIST = "security_alert_data_list"
    const val RISK_LIST = "risk_list"
    const val CHANNEL_NAME = "channel_name"
    const val AGORA_TOKEN = "agora_token"
    const val CALLER_TYPE = "caller_type"
    const val CALL_TYPE_AV = "calltypeav"
    const val NOTIFICATION_INTENT_TYPE = "caller_type"
    const val CALL_TYPE = "call_type"
    const val CALL_CHANNEL_NAME = "call_channel_name"
    const val FIREBASE_RESPONSE = "firebase_response"
    const val FCM_DATA_KEY = "fcm_data_key"
    const val CALL_RESPONSE_ACTION_KEY = "fcm_data_key"
    const val CALL_RECEIVE_ACTION = "fcm_data_key"
    const val CALL_CANCEL_ACTION = "fcm_data_key"
    const val OTP = "otp"
    const val EMAIL = "email"
    const val PASSWORD = "password"
    const val DOCUMENT_URL = "document_url"
    const val DOCUMENT_TYPE = "document_type"
    var userName = "user_name"

    var userList: ArrayList<ContactModel?> = ArrayList()

    const val PERMISSIONS_REQUEST_CODE_DEMO = 9
    const val PERMISSIONS_REQUEST_CODE = 10
    const val REQUEST_CODE_LOCATION = 11
    const val REQUEST_CODE_ADDRESS = 12
    const val REQUEST_CODE_COUNTRY = 13
    const val REQUEST_CODE_PUSH_NOTIFICATION = 14
    const val CAMERA_PERMISION = 1000;
    const val STORAGE_PERMISION = 1001;
    var globalChatCount:Int = 0

    val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    object CallerType{
        const val CALLER = "caller"
        const val RECEIVER = "receiver"
    }

    object ActivityType{
        const val YES = "YES"
        const val NO = "NO"
    }

    object CallType {
        const val AUDIO = "audio"
        const val VIDEO = "video"
    }

    object NotificationIntentType {
        const val NOTIFICATION = "notification"
        const val RECEIVE = "receive"
        const val END = "end"
    }

    object CallReject {
        const val SENDER = "sender"
        const val RECEIVER = "receiver"
    }

    object ChatType {
        const val SINGLE = "single"
        const val GROUP = "group"
    }

    object NotificationType {
        const val EMERGENCY_BUTTON_ACTIVATION = "1"
        const val CHECK_IN = "2"
        const val PUBLICATION_OF_A_SECURITY_ALERT = "3"
        const val PUBLICATION_OF_A_VARIOUS_TYPE_OF_REPORTS = "4"
        const val PUBLICATION_OF_A_VARIOUS_TYPE_OF_NOTIFICATION = "5"
        const val GEOFENCE_ALERTS = "6"
        const val SAFETY_CHECK = "7"
        const val CHAT = "8"
        const val CALLING = "9"
    }

}