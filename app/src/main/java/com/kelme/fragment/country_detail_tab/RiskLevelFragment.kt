package com.kelme.fragment.country_detail_tab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.RiskLevelListAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentRiskLevelBinding
import com.kelme.fragment.country.CountryViewModal
import com.kelme.model.SecurityAlertDetailsModel
import com.kelme.model.SubCategory
import com.kelme.model.request.SecurityAlertDetailsRequest
import com.kelme.model.response.CountryRiskLevelData
import com.kelme.model.response.RiskLevelListData
import com.kelme.model.response.SecrityAlertListData
import com.kelme.utils.*

class RiskLevelFragment : BaseFragment() {
    private lateinit var binding: FragmentRiskLevelBinding
    private lateinit var riskLevelListData: List<RiskLevelListData>
    private lateinit var countryRiskLevelData: CountryRiskLevelData
    private lateinit var viewModal: CountryViewModal
    private var securityAlertListData: ArrayList<SecrityAlertListData>?=null
    private lateinit var listSubCategory: List<SubCategory>
    private var isSelect = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            riskLevelListData = it.getParcelableArrayList("data")!!
            listSubCategory = it.getParcelableArrayList(Constants.RISK_LIST)!!
            countryRiskLevelData = it.getParcelable("countryRiskLevelData")!!
            if(it.containsKey(Constants.SECURITY_ALERT_DATA_LIST)) {
                securityAlertListData =
                    it.getParcelableArrayList(Constants.SECURITY_ALERT_DATA_LIST)!!
            }
            isSelect = it.getString("isSelected").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_risk_level, container, false)
        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            CountryViewModal::class.java
        )

        binding.tvCountryName.text = countryRiskLevelData.countryName
        Glide.with(this).load(countryRiskLevelData.countryFlag).into(binding.ivCountry)
        setRiskButtonColorAndText()
        //binding.tvModerate.text = countryRiskLevelData.riskTypeName
        binding.rvRiskLevel.adapter = RiskLevelListAdapter()

        if(securityAlertListData==null){
            Log.d("securityAlertListData","null")
            (binding.rvRiskLevel.adapter as RiskLevelListAdapter).setItems(listSubCategory as ArrayList<SubCategory>)
            listSubCategory.forEachIndexed { index, subCategory ->
                if (isSelect.contains(subCategory.risk_category_name)) {
                    subCategory.isSelected = true
                }
                Log.d("response_", isSelect +" : "+ subCategory.risk_category_name)
            }
            (binding.rvRiskLevel.adapter as RiskLevelListAdapter).notifyDataSetChanged()
        }else {
            securityAlertListData?.let {
                Log.d("securityAlertListData","not null")
                viewModal.securityAlertDetails(SecurityAlertDetailsRequest(it[0].securityAlertId.toInt()))
            }
        }

        setUp()
        setSecurityAlertDetailObserver()
        return binding.root
    }

    private fun setSecurityAlertDetailObserver() {
        viewModal.logout.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
                    PrefManager.clearUserPref()
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
        }

        viewModal.securityAlertDetails.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        val model = response.data as SecurityAlertDetailsModel
                        (activity as DashboardActivity?)?.run {
                            // setBackground(R.color.country_outlook_background)
                            setTitle("")
                        }
                        (binding.rvRiskLevel.adapter as RiskLevelListAdapter).setItems(response.data.sub_category as ArrayList<SubCategory>)
                        //Log.d("response", model.toString())
                        //Log.d("response", isSelect.toString())
                        response.data.sub_category.forEachIndexed { index, subCategory ->
                            if (isSelect.contains(subCategory.risk_category_name)) {
                                subCategory.isSelected = true
                            }
                            Log.d("response_", isSelect +" : "+ subCategory.risk_category_name)
                        }
                        (binding.rvRiskLevel.adapter as RiskLevelListAdapter).notifyDataSetChanged()
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
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setUp()
    }

    private fun setUp() {
        (activity as DashboardActivity?)?.run {
            //setTitle(countryRiskLevelData.countryName)
            setTitle("")
            setAppTopBackgroundColor(R.color.backgroundColor)
        }
    }

    private fun setRiskButtonColorAndText() {
        binding.tvModerate.visibility = View.VISIBLE
        when (countryRiskLevelData.riskTypeName) {
            "Minimal Risk" -> {
                binding.tvModerate.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_minimal_risk_without_stroke)
                binding.tvModerate.text = countryRiskLevelData.riskTypeName
                binding.tvModerate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
            "Low Risk" -> {
                binding.tvModerate.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_low_risk_without_stroke)
                binding.tvModerate.text = countryRiskLevelData.riskTypeName
                binding.tvModerate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
            "Moderate Risk" -> {
                binding.tvModerate.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_moderate_risk_without_stroke)
                binding.tvModerate.text = countryRiskLevelData.riskTypeName
                binding.tvModerate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
            "High Risk" -> {
                binding.tvModerate.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_high_risk_without_stroke)
                binding.tvModerate.text = countryRiskLevelData.riskTypeName
                binding.tvModerate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
            "Extreme Risk" -> {
                binding.tvModerate.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_extreme_risk_without_stroke)
                binding.tvModerate.text = countryRiskLevelData.riskTypeName
                binding.tvModerate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
        }
    }

}