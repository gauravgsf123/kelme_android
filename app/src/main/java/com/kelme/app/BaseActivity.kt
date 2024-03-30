package com.kelme.app

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kelme.utils.Utils
import java.security.AccessController.getContext

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var TAG: String
    protected var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG = this.javaClass.simpleName
        handler = Handler(Looper.getMainLooper())

    }

     abstract fun initializerControl()

//
//    override fun onBackPressed() {
//        super.onBackPressed()
//        Utils.hideKeyboard(this)
//    }

    protected open fun showAlert(message: String?) {
        val context: Context = (getContext() ?: return) as Context
        AlertDialog.Builder(context).setTitle("Tips").setMessage(message)
            .setPositiveButton("OK") { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            .show()
    }

    protected fun showLongToast(msg: String?) {
        handler?.post(Runnable {
            if (this == null || getContext() == null) {
                return@Runnable
            }
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
        })
    }

    protected fun showLog(message:String){
        Log.d(TAG,message)
    }

}