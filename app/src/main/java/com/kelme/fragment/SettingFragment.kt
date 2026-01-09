package com.kelme.fragment

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.dashboard.DashboardViewModal
import com.kelme.activity.login.LoginActivity
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentSettingBinding
import com.kelme.fragment.profile.ChangePasswordFragment
import com.kelme.model.SettingModel
import com.kelme.model.request.UpdateSettingRequest
import com.kelme.utils.*

/**
 * Created by Amit Gupta on 12-07-2021.
 */
class SettingFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var viewModal: DashboardViewModal

    private var enableTracking = "1"
    private var enablePush = "1"
   // private var miles = "100"
    private var check = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false)

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            DashboardViewModal::class.java
        )

        setUI()
        setObserver()
        getSetting()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        (activity as DashboardActivity?)?.run {
            showNotificationIcon()
            setTitle("Settings")
            changeSettingColor()
            showSearchBar()
            //setPositiveToolbarPadding()
            showMenu()
        }
    }

    override fun onStop() {
        super.onStop()
        Utils.hideKeyboard(requireActivity(),binding.root)
        (activity as DashboardActivity?)?.run {
           resetColorChange()
        }
    }

    private fun setUI() {
        binding.changePasswordTxt.setOnClickListener {
            (activity as DashboardActivity?)?.run {
                replaceFragment(ChangePasswordFragment(), Bundle.EMPTY)
            }
        }
        binding.privacyPolicyTxt.setOnClickListener {
            (activity as DashboardActivity?)?.run {
                replaceFragment(PrivacyFragment(), Bundle.EMPTY)
            }
        }
        binding.termConditionTxt.setOnClickListener {
            (activity as DashboardActivity?)?.run {
                replaceFragment(TermsFragment(), Bundle.EMPTY)
            }
        }

        if (!checkPermission()) {
            requestPermission()
        }

        binding.enableTrackingSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                enableTracking = "1"
            } else {
                enableTracking = "2"
            }

            if (check) {
                updateSetting()
            }else {
                check = true
            }
        }
        binding.pushNotificationsSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                enablePush = "1"
            } else {
                enablePush = "2"
            }
            if (check) {
                 updateSetting()
            }else {
                check = true
            }
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
        val result1 = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(Activity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1)
    }

    private fun setObserver() {
        viewModal.logout.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    PrefManager.clearUserPref()
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
                else -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        }

        viewModal.setting.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        check = false
                        val setting = response.data as SettingModel
                        if (setting.push_notification == "1") {
                            check = false
                            binding.pushNotificationsSwitch.isChecked = true
                            enablePush = "1"
                        } else {
                            check = false
                            binding.pushNotificationsSwitch.isChecked = false
                            enablePush = "2"
                        }
                        if (setting.enable_tracking == "1") {
                            check = false
                            binding.enableTrackingSwitch.isChecked = true
                            enableTracking = "1"
                        } else {
                            check = false
                            binding.enableTrackingSwitch.isChecked = false
                            enableTracking = "2"
                        }
//                    miles = setting.set_miles
//                    check = false
//                    binding.etMilesCount.setText(setting.set_miles)
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
                else -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        }

        viewModal.updateSetting.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
                else -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        })
    }

    private fun getSetting() {
        viewModal.getSetting()
    }

    private fun updateSetting() {
        val request = UpdateSettingRequest(
            enableTracking,
            enablePush
        )
        viewModal.updateSetting(request)
    }


}