package com.kelme.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.kelme.model.ChatUnreadCount
import com.kelme.model.ContactModel

object PrefManager {

    fun init(context: Context) {
        if (!(::preferences.isInitialized)) {
            preferences = context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
        }
    }

    const val AUTH_TOKEN: String = "authToken"

    val LoginUser: ContactModel = ContactModel()

    const val SLONGITUDE: String = "slong"
    const val SLATITUDE: String = "slat"
    const val NLONGITUDE: String = "long"
    const val NTITLE: String = "title"
    const val NSAFETY: String = "safety"
    const val NFIREBASEID: String = "nFirebaseId"
    const val MODULEID: String = "moduleId"
    const val NMODULE: String = "module"
    const val NLATITUDE: String = "lat"
    const val UNSEENMSGCOUNT: String = "unSeenMsgCount"
    const val MESSAGECOUNT: String = "messageCount"
    const val FROMNOTIFICATION: String = "fromNotification"
    const val FROMNOTIFICATIONCHATID: String = "fromNotificationchatId"
    const val CHAT_ID_NOT_FOUND: String = "chatidnotfound"
    const val CREATER_ID: String = "createrId"
    const val IS_MEMBER_LEAVE: String = "ismemberleave"
    const val CHAT_UNIQUE_ID: String = "chatUniqueId"
    const val USER_ID: String = "user_id"
    const val NAME: String = "name"
    const val PHONE: String = "phone_number"
    const val EMAIL: String = "email"
    const val USERROLE: String = "role"
    const val COMPANYID: String = "companyId"
    const val PASSWORD: String = "password"
    const val GENDER: String = "gender"
    const val COUNTRY_NAME: String = "country_name"
    const val COUNTRY_ID: String = "country_id"
    const val IMAGE: String = "image"
    const val ADDRESS: String = "address"
    const val ROLE: String = "role"
    const val OTP: String = "otp"
    const val REMEMBER_LOGIN_DETAILS: String = "remember"

    const val LATITUDE: String = "latitude"
    const val LONGITUDE: String = "longitude"

    const val IS_LOGIN: String = "isLogin"

    const val IS_CALL_FROM_CHAT_LIST_FRAG: String = "isChatListFrag"

    const val IS_CALL_CONTACT: String = "IS_CALL_CONTACT"

    const val FCM_TOKEN: String = "fcmToken"
    const val DEVICE_ID: String = "deviceId"
    const val FCM_USER_ID: String = "fcmUserid"
    const val ALL_CHAT_DELETE: String = "all_chat_delete"

    const val FCM_CHAT_ID: String = "fcmChatid"
    const val SEARCH_CHAT_ID: String = "searchChatId"
    const val FCM_GROUP_NAME: String = "fcmChatid"

    private lateinit var preferences: SharedPreferences

    fun clearUserPref() {
        preferences.edit()?.remove(AUTH_TOKEN)?.apply()
        preferences.edit()?.remove(USER_ID)?.apply()
        preferences.edit()?.remove(NAME)?.apply()
        preferences.edit()?.remove(PHONE)?.apply()
        preferences.edit()?.remove(GENDER)?.apply()
        preferences.edit()?.remove(COUNTRY_NAME)?.apply()
        preferences.edit()?.remove(COUNTRY_ID)?.apply()
        preferences.edit()?.remove(IMAGE)?.apply()
        preferences.edit()?.remove(ADDRESS)?.apply()
        preferences.edit()?.remove(ROLE)?.apply()
        preferences.edit()?.remove(OTP)?.apply()
        preferences.edit()?.remove(IS_LOGIN)?.apply()
    }

    //calling this method will clear FCM key
    fun clearAllPref() {
        preferences.edit()?.clear()?.apply()
    }

    fun read(key: String, defValue: String): String {
        return preferences.getString(key, defValue)!!
    }

    fun read(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    fun read(key: String, defValue: Long): Long {
        return preferences.getLong(key, defValue)
    }

    fun read(key: String, defValue: Int): Int {
        return preferences.getInt(key, defValue)
    }


    fun write(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun write(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    fun write(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun write(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun writeUnReadData(key: String?, list: MutableSet<String>) {
        val gson = Gson()
        val json: String = gson.toJson(list)
        preferences.edit().putString(key,json).apply()
    }

    fun readUnreadData(key: String?): MutableSet<String>? {
        val gson = Gson()
        val json = preferences.getString(key, "")
        Log.d("json_data","${preferences.getString(key,null)}")
        //val type = object : TypeToken<HashSet<String>?>(){}.type
        return if(json.isNullOrEmpty()){
            HashSet<String>()
        }else gson.fromJson(json, Array<String>::class.java).toHashSet()
        //return preferences.getStringSet(key,null)
    }
}