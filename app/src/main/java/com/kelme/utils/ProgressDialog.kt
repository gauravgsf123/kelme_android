package com.kelme.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.kelme.R

/**
 * Created by Amit on 28,June,2021
 */
object ProgressDialog {

    private lateinit var alertDialog: AlertDialog

    fun showProgressBar(context: Context) {
        try {
            if (::alertDialog.isInitialized) {
                if (alertDialog.isShowing) {
                    alertDialog.hide()
                    alertDialog.dismiss()
                } else {
                    val alertDialogBuilder = AlertDialog.Builder(context)
                    val view = LayoutInflater.from(context).inflate(R.layout.layout_progress_dialog, null)
                    alertDialogBuilder.setView(view)
                    alertDialog = alertDialogBuilder.create()
                    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    try {
                        alertDialog.show()
                    } catch (e: Exception) {
                    }
                }
            } else {
                val alertDialogBuilder = AlertDialog.Builder(context)
                val view = LayoutInflater.from(context).inflate(R.layout.layout_progress_dialog, null)
                alertDialogBuilder.setView(view)
                alertDialog = alertDialogBuilder.create()
                alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                alertDialog.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideProgressBar() {
        try {
            if (::alertDialog.isInitialized) {
                if (alertDialog.isShowing) {
                    alertDialog.hide()
                    alertDialog.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
        fun setCancelable() {
            try {
                if (::alertDialog.isInitialized) {
                    if (alertDialog.isShowing) {
                        alertDialog.setCanceledOnTouchOutside(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}