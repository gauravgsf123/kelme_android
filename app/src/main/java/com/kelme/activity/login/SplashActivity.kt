package com.kelme.activity.login

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.utils.PrefManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var videoView : VideoView
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        videoView = findViewById(R.id.videoView)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController

            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        PrefManager.write(
            PrefManager.DEVICE_ID,
            Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        )

        val token = PrefManager.read(PrefManager.AUTH_TOKEN, "")
        Log.d("auth_token", token)

        val video = Uri.parse ("android.resource://" + packageName + "/" + R.raw.kelme_splash)
        videoView.setVideoURI(video)
        videoView.start()

        videoView.setOnCompletionListener {
            startNextActivity()
        }

       // (activity as DashboardActivity).getCurrentLocation()
//        videoView.setOnCompletionListener(new MediaPlayer . OnCompletionListener () {
//            public void onCompletion(MediaPlayer mp) {
//
//            }
//        });
    }

//    private fun requestPermission() {
//        ActivityCompat.requestPermissions(
//            this@SplashActivity,
//            Constants.PERMISSIONS,
//            Constants.PERMISSIONS_REQUEST_CODE
//        )
//    }

//    private fun hasPermissions(
//        vararg permissions: String?
//    ): Boolean {
//        for (permission in permissions) {
//            if (ContextCompat.checkSelfPermission(
//                    this@SplashActivity,
//                    permission!!
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return false
//            }
//        }
//        return true
//    }

    //    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        var res = true
//        if (requestCode == Constants.PERMISSIONS_REQUEST_CODE && grantResults.isNotEmpty()) {
//            for (result in grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    res = false
//                }
//            }
//            if (res) {
//                videoView.start()
//            } else {
//                Toast.makeText(
//                    applicationContext,
//                    "Restart app and grant all permissions in order to continue..",
//                    Toast.LENGTH_LONG
//                ).show()
//                finish()
//            }
//        }
//    }

    private fun startNextActivity() {
        if (isFinishing)
            return
        if (PrefManager.read(PrefManager.IS_LOGIN, false)) {
            startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
        } else {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        }
        finish()
    }
}