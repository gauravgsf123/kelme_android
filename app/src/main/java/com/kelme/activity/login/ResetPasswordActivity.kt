package com.kelme.activity.login


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityResetPasswordBinding
import com.kelme.model.request.ResetPasswordRequest
import com.kelme.model.request.VerifyOtpRequest
import com.kelme.utils.Constants
import com.kelme.utils.ProgressDialog
import com.kelme.utils.Resource
import com.kelme.utils.Utils
import com.kelme.utils.ViewModalFactory

class ResetPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var viewModal: LoginViewModal
    private lateinit var email:String
    private lateinit var password:String
    private lateinit var otp:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)

        viewModal = ViewModelProvider(this, ViewModalFactory(application)).get(
            LoginViewModal::class.java)
        intent.run {
            email = getStringExtra(Constants.EMAIL).toString()
            password = getStringExtra(Constants.PASSWORD).toString()
            otp = getStringExtra(Constants.OTP).toString()
        }
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

        binding.ivNewPassword.setOnClickListener {
            showHidePassword(binding.etNewPassword,binding.ivNewPassword)
        }

        binding.ivConfirmPassword.setOnClickListener {
            showHidePassword(binding.etConfirmPassword,binding.ivConfirmPassword)
        }

    }

    private fun setObserver() {
        viewModal.resetPassword.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    authFIrebase()
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        })

        viewModal.verifyOtp.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    binding.etNewPassword.visibility = View.VISIBLE
                    binding.etConfirmPassword.visibility = View.VISIBLE
                    binding.ivNewPassword.visibility = View.VISIBLE
                    binding.ivConfirmPassword.visibility = View.VISIBLE
                    binding.btnSubmit.text = resources.getString(R.string.reset_password)
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        })

    }

    private fun showHidePassword(editText: EditText,imageView: ImageView){
        if (editText.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
            imageView.setImageResource(R.drawable.show_password)
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            imageView.setImageResource(R.drawable.hide_password)
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
        }
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

    private fun authFIrebase() {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(email, password)
        user?.reauthenticate(credential)?.addOnCompleteListener {
            if(it.isSuccessful){
                user!!.updatePassword(binding.etNewPassword.text.toString().trim())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            ProgressDialog.hideProgressBar()
                            Toast.makeText(
                                this,
                                "User password updated.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
            }
        }
    }
}