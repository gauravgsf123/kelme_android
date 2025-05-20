package com.kelme.utils

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import com.kelme.R

/**
 * Created by GAURAV KUMAR on 05,December,2021
 * Quytech,
 */
object Ringtone {
    private var ringtone: MediaPlayer? = null
    private var siren: Ringtone? = null

    fun playRingtone(context: Context) {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            if (ringtone == null) {
                ringtone = MediaPlayer.create(context, notification)
            }
            ringtone?.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSiren(context: Context){
        try {
            siren = RingtoneManager.getRingtone(context, Uri.parse("android.resource://" + context.packageName + "/" + R.raw.siren))
            siren?.play()
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                siren?.isLooping = true
            }*/
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }



    fun stopRingtone() {
        if (ringtone != null) {
            ringtone?.stop()
            ringtone = null
        }
    }

    fun stopSiren() {
        if (siren != null) {
            siren?.stop()
            siren = null
        }
    }
}