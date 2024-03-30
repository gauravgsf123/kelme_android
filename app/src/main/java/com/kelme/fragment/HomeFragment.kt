package com.kelme.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.dashboard.DashboardViewModal
import com.kelme.activity.login.LoginActivity
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentHomeBinding
import com.kelme.fragment.country.CountryOutlookFragment
import com.kelme.fragment.security.SecurityAlertsFragment
import com.kelme.utils.PrefManager
import com.kelme.utils.ProgressDialog
import com.kelme.utils.Resource
import com.kelme.utils.ViewModalFactory

class HomeFragment : BaseFragment() {
    private lateinit var viewModal: DashboardViewModal
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            DashboardViewModal::class.java)
        setUI()
        setObserver()
        return binding.root
    }

    private fun setObserver() {
        /*viewModal.logout.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        ProgressDialog.hideProgressBar()
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                requireContext(),
                                LoginActivity::class.java
                            )
                        )
                        activity?.finish()
                    } else {
                        ProgressDialog.hideProgressBar()
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                requireContext(),
                                LoginActivity::class.java
                            )
                        )
                        activity?.finish()
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
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
        }*/

        viewModal.notificationCount.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        startActivity(
                            Intent(
                                activity,
                                LoginActivity::class.java
                            )
                        )
                        activity?.finish()
                    } else {
                        // ProgressDialog.hideProgressBar()
                        val count = response.data?.unread_count
                        if (count != null && count > 0) {
                            (activity as DashboardActivity?)?.run {
                                showUnreadCount(count.toString())
                            }
                        } else {
                            (activity as DashboardActivity?)?.run {
                                hideUnreadCount()
                            }
                        }
                    }
                }
                is Resource.Loading -> {
                    // ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    // ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
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
        }
    }

    override fun onResume() {
        super.onResume()
        unreadNotificationCount()
        (activity as DashboardActivity?)?.run {
            setTitle("")
            showNotificationIcon()
            showSearchBar()
            showMenu()
        }
    }

    private fun unreadNotificationCount() {
        viewModal.unreadNotification()
    }

    private fun setUI() {
        binding.constraintOne.setOnClickListener {
            (activity as DashboardActivity?)?.run {
                replaceFragment(CountryOutlookFragment(), Bundle.EMPTY)
            }
        }
        binding.constraintTwo.setOnClickListener {
            (activity as DashboardActivity?)?.run {
                replaceFragment(SecurityAlertsFragment(), Bundle.EMPTY)
            }
        }
        binding.constraintThee.setOnClickListener {
            (activity as DashboardActivity?)?.run {
                replaceFragment(MapsFragment(), Bundle.EMPTY)
            }
        }
        binding.constraintFour.setOnClickListener {
            (activity as DashboardActivity?)?.run {
                replaceFragment(CheckInFragment(), Bundle.EMPTY)
            }
        }
    }
}