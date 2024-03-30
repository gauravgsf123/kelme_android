package com.kelme.activity.login


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityResetPasswordBinding
import com.kelme.model.request.ResetPasswordRequest
import com.kelme.model.request.VerifyOtpRequest
import com.kelme.utils.ProgressDialog
import com.kelme.utils.Resource
import com.kelme.utils.Utils
import com.kelme.utils.ViewModalFactory

class ResetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var viewModal: LoginViewModal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)

        viewModal = ViewModelProvider(this, ViewModalFactory(application)).get(
            LoginViewModal::class.java)

        setUi()
        setObserver()
    }

    override fun initializerControl() {
        //TODO("Not yet implemented")
    }

    private fun setUi() {

        binding.backArrow.setOnClickListener {
            onBackPressed()
        }

        binding.btnSubmit.setOnClickListener {
            if (binding.btnSubmit.text == resources.getString(R.string.verify_otp)) {
                if (TextUtils.isEmpty(binding.etOtp.text)) {
                    Utils.showSnackBar(binding.root, "Enter Otp")
                } else {
                    verifyOtp()
                }
            } else {
                if (TextUtils.isEmpty(binding.etNewPassword.text)) {
                    Utils.showSnackBar(binding.root, "Enter New Password")
                } else if (TextUtils.isEmpty(binding.etConfirmPassword.text)) {
                    Utils.showSnackBar(binding.root, "Enter Confirm Password")
                } else if (!(binding.etNewPassword.text.toString()
                        .equals(binding.etConfirmPassword.text.toString()))
                ) {
                    Utils.showSnackBar(binding.root, "Both Password are not same")
                } else {
                    resetPassword()
                }
            }
        }

    }

    private fun setObserver() {
        viewModal.resetPassword.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        applicationContext,
//                        response.message,
//                        Toast.LENGTH_LONG
//                    ).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        applicationContext,
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }
        })

        viewModal.verifyOtp.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        applicationContext,
//                        response.message,
//                        Toast.LENGTH_LONG
//                    ).show()
                    binding.etNewPassword.visibility = View.VISIBLE
                    binding.etConfirmPassword.visibility = View.VISIBLE
                    binding.btnSubmit.text = resources.getString(R.string.reset_password)
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        applicationContext,
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }
        })

    }

    private fun verifyOtp() {
        val request = VerifyOtpRequest(
            binding.etOtp.text.toString(),
            "2",
            "Provider"
        )
        viewModal.verifyOtp(request)
    }

    private fun resetPassword() {
        val request = ResetPasswordRequest(
            binding.etNewPassword.text.toString(),
            binding.etNewPassword.text.toString()
        )
        viewModal.resetPassword(request)
    }
}