package com.kelme.utils

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.view.LayoutInflater
import com.kelme.R

/**
 * Created by GAURAV KUMAR on 05,December,2021
 * Quytech,
 */
object Ringtone {
    private var ringtone: MediaPlayer? = null

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

    fun stopRingtone() {
        if (ringtone != null) {
            ringtone?.stop()
            ringtone = null
        }
    }
}