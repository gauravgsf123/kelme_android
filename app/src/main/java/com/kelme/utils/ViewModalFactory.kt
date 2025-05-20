package com.kelme.utils

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.kelme.activity.dashboard.DashboardViewModal
import com.kelme.activity.login.LoginViewModal
import com.kelme.fragment.chat.ChatViewModal
import com.kelme.fragment.country.CountryViewModal
import com.kelme.fragment.profile.MyProfileViewModel
import com.kelme.fragment.security.SecurityViewModal

/**
 * Created by Amit on 28,June,2021
 */
@Suppress("UNCHECKED_CAST")
class ViewModalFactory(private val app: Application) : ViewModelProvider.Factory {

    /*override fun <T : ViewModel?> create(modelClass: Class<T>): T {

    }*/

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModal::class.java)) {
            return LoginViewModal(app) as T
        }
        if (modelClass.isAssignableFrom(DashboardViewModal::class.java)) {
            return DashboardViewModal(app) as T
        }
        if (modelClass.isAssignableFrom(CountryViewModal::class.java)) {
            return CountryViewModal(app) as T
        }
        if (modelClass.isAssignableFrom(SecurityViewModal::class.java)) {
            return SecurityViewModal(app) as T
        }
        if (modelClass.isAssignableFrom(ChatViewModal::class.java)) {
            return ChatViewModal(app) as T
        }
        if (modelClass.isAssignableFrom(MyProfileViewModel::class.java)) {
            return MyProfileViewModel(app) as T
        }


        throw IllegalArgumentException("Unknown class name")
    }
}
