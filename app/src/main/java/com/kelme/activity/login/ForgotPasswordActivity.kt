package com.kelme.activity.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityForgotPasswordBinding
import com.kelme.model.request.ForgetPasswordRequest
import com.kelme.utils.*

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModal: LoginViewModal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)

        viewModal = ViewModelProvider(this, ViewModalFactory(application)).get(
            LoginViewModal::class.java
        )

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

        binding.btnContinue.setOnClickListener {
            if (TextUtils.isEmpty(binding.etEmail.text)) {
                Utils.showSnackBar(binding.root, "Enter Email")
            }
            if (!(Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches())) {
                Utils.showSnackBar(binding.root, "Enter Valid Email")
            } else {
                forgetPassword()
            }
        }
    }

    private fun setObserver() {
        viewModal.forgetPassword.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    response.data?.auth_token?.let { PrefManager.write(PrefManager.AUTH_TOKEN, it) }
                    response.data?.otp?.let { PrefManager.write(PrefManager.OTP, it) }
//                    Toast.makeText(
//                        applicationContext,
//                        response.message,
//                        Toast.LENGTH_LONG
//                    ).show()
                    var intent = Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                    response.data?.run {
                        intent.putExtra(Constants.OTP,otp)
                        intent.putExtra(Constants.EMAIL,binding.etEmail.text?.trim().toString())
                        intent.putExtra(Constants.PASSWORD,user_pass)
                    }
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

    }

    private fun forgetPassword() {
        val request = ForgetPasswordRequest(
            binding.etEmail.text.toString(),
            Constants.DEVICE_TYPE_ID,
            PrefManager.read(PrefManager.DEVICE_ID, ""),
            PrefManager.read(PrefManager.FCM_TOKEN, "")
        )
        viewModal.forgetPassword(request)
    }
}