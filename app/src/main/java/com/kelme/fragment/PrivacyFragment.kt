package com.kelme.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.dashboard.DashboardViewModal
import com.kelme.activity.login.LoginActivity
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentPrivacyBinding
import com.kelme.model.StaticDataModel
import com.kelme.model.request.StaticDataRequest
import com.kelme.utils.*

/**
 * Created by Amit Gupta on 12-07-2021.
 */
class PrivacyFragment : BaseFragment() {

    private lateinit var binding: FragmentPrivacyBinding
    private lateinit var viewModal: DashboardViewModal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_privacy, container, false)

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            DashboardViewModal::class.java
        )

        setUI()
        setObserver()
        getData()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle(getString(R.string.privacy_policy))
            Utils.hideKeyboard(requireActivity(),binding.root)
            hideNotificationIcon()
            showBackArrow()
            hideSearchBar()
            changeSettingColor()
            hideUnreadCount()
        }
    }

    private fun setUI() {

    }

    private fun setObserver() {
        viewModal.logout.observe(viewLifecycleOwner, Observer { response ->
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
        })

        viewModal.staticData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        val data = response.data as StaticDataModel
                        binding.tvDescription.text =
                            HtmlCompat.fromHtml(data.description, HtmlCompat.FROM_HTML_MODE_LEGACY);
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

    private fun getData() {
        val request = StaticDataRequest(
            2
        )
        viewModal.getStaticData(request)
    }


}