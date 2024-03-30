package com.kelme.app

import android.app.Application
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kelme.db.AppDatabase
import com.kelme.utils.PrefManager

class MyApp : Application() {

    companion object {
        lateinit var application: Application


        fun getInstance(): Application {
            return application
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        PrefManager.init(applicationContext)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Firebase.database.setPersistenceEnabled(true)
       // AppDatabase.invoke(applicationContext)
    }


}