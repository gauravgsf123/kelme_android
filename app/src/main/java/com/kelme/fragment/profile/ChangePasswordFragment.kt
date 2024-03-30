package com.kelme.fragment.profile

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentChangePasswordBinding
import com.kelme.model.request.ChangePasswordRequest
import com.kelme.utils.*


class ChangePasswordFragment : BaseFragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var viewmodel: MyProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_change_password, container, false)
        init()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle(getString(R.string.change_password))
            Utils.hideKeyboard(requireActivity(),binding.root)
            hideNotificationIcon()
            hideSearchBar()
            showBackArrow()
            changeSettingColor()
            hideUnreadCount()
        }
    }

    private fun init() {

        viewmodel = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            MyProfileViewModel::class.java
        )
        setObserver()
        binding.btnSubmit.setOnClickListener {
            Utils.hideKeyboard(requireContext(), binding.root)
            if (TextUtils.isEmpty(binding.oldPassword.text)) {
                Utils.showSnackBar(binding.root, getString(R.string.please_enter_old_password))
            } else if (TextUtils.isEmpty(binding.newPassword.text)) {
                Utils.showSnackBar(binding.root, getString(R.string.please_enter_new_password))
            } else if (TextUtils.isEmpty(binding.confirmPassword.text)) {
                Utils.showSnackBar(binding.root, getString(R.string.please_enter_confirm_password))
            } else if (binding.newPassword.text.toString() != binding.confirmPassword.text.toString()) {
                Utils.showSnackBar(
                    binding.root,
                    getString(R.string.new_password_confirm_password_not_match)
                )
            } else {
                changePassword()
            }
        }
    }

    private fun changePassword() {
        viewmodel.changePassword(
            ChangePasswordRequest(
                binding.oldPassword.text.toString(),
                binding.newPassword.text.toString(),
                binding.confirmPassword.text.toString()
            )
        )
    }

    private fun setObserver() {
        viewmodel.logout.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
//                    PrefManager.clearUserPref()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    startActivity(
                        Intent(
                            activity,
                            LoginActivity::class.java
                        )
                    )
                    activity?.finish()
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    if (response.message == "Your session has been expired, Please login again.") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                activity,
                                LoginActivity::class.java
                            )
                        )
                        activity?.finish()
                    }
                }
            }
        })

        viewmodel.changePasswordResponse.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewmodel.logout()
                    } else {
                        if (response.data.toString() == "Success.") {
                            changePasswordFirebase()
//                        Toast.makeText(
//                            requireContext(),
//                            "Password change successfully",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        }
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewmodel.logout()
                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }
        })
    }

    private fun changePasswordFirebase(){
        val user = FirebaseAuth.getInstance().currentUser
        user!!.updatePassword(binding.newPassword.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ProgressDialog.hideProgressBar()
                    Toast.makeText(
                            requireContext(),
                            "User password updated.",
                            Toast.LENGTH_SHORT
                        ).show()
                    (activity as DashboardActivity?)?.run {
                        onBackPressed()
                    }
                }
            }
    }


}