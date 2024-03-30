package com.kelme.app

import android.os.Bundle
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

open abstract class BaseFragment : Fragment() {
    protected lateinit var TAG: String
   // lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG = this.javaClass.simpleName
       // pDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
       // navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
    }

//    protected open fun showToast(message: String) {
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
//    }

//    protected open fun isDialogShow(): Boolean {
//        var value: Boolean = false
//        pDialog?.let {
//            when {
//                it.isShowing -> {
//                    value = true
//                }
//            }
//        }
//        return value
//    }
//
//    protected open fun showDialog(context: Context) {
//        pDialog.progressHelper.barColor = Color.parseColor("#C70B0D")
//        pDialog.titleText = "Loading ..."
//        pDialog.setCancelable(true)
//        pDialog.show()
//    }
//
//    protected open fun hideDialog() {
//        pDialog?.let {
//            when {
//                it.isShowing -> pDialog.dismiss()
//            }
//        }
//    }
//
//    protected open fun showSuccess(title: String, message: String) {
//        SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
//            .setTitleText(title)
//            .setContentText(message)
//            .show()
//    }
//
//    protected open fun showError(title: String, message: String) {
//        SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
//            .setTitleText(title)
//            .setContentText(message)
//            .show()
//    }

    fun getFontFromRes(@FontRes font: Int) = ResourcesCompat.getFont(requireContext(), font)

    fun getPart(filed: String): RequestBody {
        return filed.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
    }
}