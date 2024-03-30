package com.kelme

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class EmergencyAlertActivity : AppCompatActivity() {

    var mMediaPlayer:MediaPlayer= MediaPlayer()
    var r: android.media.Ringtone? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_alert)
        val ivClose = findViewById<ImageView>(R.id.ivClose)
        try {
             r = RingtoneManager.getRingtone(
                applicationContext,
                Uri.parse("android.resource://" + this.packageName + "/" + R.raw.siren)
            )
            r?.play()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                r?.isLooping = true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        ivClose.setOnClickListener {
            r?.stop()
            finish()
        }
       // playSiren(this)
    }
    private fun playSiren(context: Context?) {
        mMediaPlayer = MediaPlayer.create(context, R.raw.siren)
        mMediaPlayer.start()
        mMediaPlayer.isLooping = true
    }

    override fun onPause() {
        super.onPause()
        finish()
        r?.stop()
    }
}