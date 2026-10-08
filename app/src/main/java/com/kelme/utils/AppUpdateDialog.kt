package com.kelme.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kelme.R

object AppUpdateDialog {

    private var dialog: AlertDialog? = null

    fun show(
        context: Context,
        message: String
    ) {

        if (dialog?.isShowing == true) {
            return
        }

        dialog = AlertDialog.Builder(context)
            .setTitle("Update Required")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Update") { _, _ ->

                openPlayStore(context)

            }
            .create()

        dialog?.show()
    }

    private fun openPlayStore(context: Context) {

        val packageName = context.packageName

        try {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.kelme")
            )

            context.startActivity(intent)

        } catch (e: Exception) {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://play.google.com/store/apps/details?id=com.kelme"
                )
            )

            context.startActivity(intent)
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}