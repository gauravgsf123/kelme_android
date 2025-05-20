package com.kelme

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.kelme.app.BaseActivity
import com.kelme.utils.Ringtone

class EmergencyAlertActivity : BaseActivity() {

    var mMediaPlayer:MediaPlayer= MediaPlayer()
    var r: android.media.Ringtone? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        setContentView(R.layout.activity_emergency_alert)
        val ivClose = findViewById<ImageView>(R.id.ivClose)

        ivClose.setOnClickListener {
            Ringtone.stopSiren()
            //r?.stop()
            finish()
        }
       // playSiren(this)
    }

    override fun initializerControl() {

    }

    private fun playSiren(context: Context?) {
        mMediaPlayer = MediaPlayer.create(context, R.raw.siren)
        mMediaPlayer.start()
        mMediaPlayer.isLooping = true
    }

    override fun onPause() {
        super.onPause()
        finish()
        Ringtone.stopSiren()
    }
}