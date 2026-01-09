package com.kelme.fragment.country_detail_tab

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentOverAllRiskLevelBinding
import com.kelme.dialogs.SingleButtonDialogLikeIos
import com.kelme.fragment.country.CountryDetailFragment
import com.kelme.fragment.country.CountryViewModal
import com.kelme.model.CountryModel
import com.kelme.model.SubCategory
import com.kelme.model.response.CountryRiskLevelData
import com.kelme.model.response.RiskLevelListData
import com.kelme.model.response.SecrityAlertListData
import com.kelme.utils.*

class OverAllRiskLevelFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding: FragmentOverAllRiskLevelBinding
    private lateinit var viewModal: CountryViewModal
    private val mHandler: Handler = Handler()
    private var mProgressStatus = 0
    private lateinit var countryRiskLevelData: CountryRiskLevelData
    private lateinit var riskLevelListData: List<RiskLevelListData>
    private lateinit var listSubCategory: List<SubCategory>
    private var securityAlertListData: ArrayList<SecrityAlertListData>?=null
    private var isContainRiskLevel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            countryRiskLevelData = it.getParcelable(Constants.DATA)!!
            if(it.containsKey(Constants.SECURITY_ALERT_DATA_LIST)) {
                securityAlertListData =
                    it.getParcelableArrayList(Constants.SECURITY_ALERT_DATA_LIST)!!
            }
            listSubCategory = it.getParcelableArrayList(Constants.RISK_LIST)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_over_all_risk_level,
            container,
            false
        )
        init()
        setUp()
        return binding.root
    }

    private fun init() {
        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            CountryViewModal::class.java
        )
        viewModal.riskLevelList()
        setRiskButtonColorAndText()
        binding.tvCountryName.text = countryRiskLevelData.countryName
        Glide.with(this).load(countryRiskLevelData.countryFlag).into(binding.ivCountry)
        binding.ivCrime.setOnClickListener(this)
        binding.ivTerrorism.setOnClickListener(this)
        binding.ivGeopolitical.setOnClickListener(this)
        binding.ivSocialUnrest.setOnClickListener(this)
        binding.tvRiskOverall.setOnClickListener(this)
        binding.tvCountryName.setOnClickListener(this)

        // binding.ivCrime.callOnClick()
        binding.tvLevel.setOnClickListener {
            val bundle = Bundle()
            securityAlertListData?.let {
                bundle.putParcelableArrayList(
                    Constants.SECURITY_ALERT_DATA_LIST,
                    it as ArrayList<out Parcelable>
                )
            }

            bundle.putParcelableArrayList(
                Constants.RISK_LIST,
                listSubCategory as ArrayList<out Parcelable>
            )

            bundle.putParcelableArrayList(
                "data",
                riskLevelListData as ArrayList<out Parcelable>
            )
            bundle.putParcelable("countryRiskLevelData", countryRiskLevelData)
            bundle.putString("isSelected", binding.tvRiskOverall.text.toString())
            (activity as DashboardActivity).addFragment(RiskLevelFragment(), bundle)
        }
        setObserver()
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

        viewModal.riskLevelData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        riskLevelListData = response.data!!
                        Log.d(TAG, response.data.toString())
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
    }


    @SuppressLint("ResourceType")
    override fun onClick(p0: View?) {
        isContainRiskLevel = false
        var riskType = ""
        when (p0?.id) {
            binding.ivTerrorism.id -> {
                for (index in listSubCategory.indices) {
                    if (listSubCategory[index].risk_category_name.contains("Terrorism")) {
                        isContainRiskLevel = true
                        riskType = listSubCategory[index].risk_type_name
                        break
                    }
                }

                if (isContainRiskLevel) {
                    binding.ivTerrorismBackground.visibility = View.VISIBLE
                    binding.ivGeopoliticalBackground.visibility = View.GONE
                    binding.ivCrimeBackground.visibility = View.GONE
                    binding.ivSocialUnrestBackground.visibility = View.GONE
                    setColorAndProgress(
                        riskType,
                        getString(R.string.terrorism_risk_level),
                        binding.ivTerrorismBackground, true
                    )
                } else {
                    showSingleButtonDialog()
                }
            }

            binding.ivCrime.id -> {
                for (index in listSubCategory.indices) {
                    if (listSubCategory[index].risk_category_name.contains("Crime")) {
                        isContainRiskLevel = true
                        riskType = listSubCategory[index].risk_type_name
                        break
                    }
                }

                if (isContainRiskLevel) {
                    binding.ivTerrorismBackground.visibility = View.GONE
                    binding.ivGeopoliticalBackground.visibility = View.GONE
                    binding.ivCrimeBackground.visibility = View.VISIBLE
                    binding.ivSocialUnrestBackground.visibility = View.GONE
                    setColorAndProgress(
                        riskType,
                        getString(R.string.crime_risk_level),
                        binding.ivCrimeBackground, true
                    )

                } else {
                    showSingleButtonDialog()
                }
            }

            binding.ivGeopolitical.id -> {
                for (index in listSubCategory.indices) {
                    if (listSubCategory[index].risk_category_name.contains("Geopolitical")) {
                        isContainRiskLevel = true
                        riskType = listSubCategory[index].risk_type_name
                        break
                    }
                }

                if (isContainRiskLevel) {
                    binding.ivTerrorismBackground.visibility = View.GONE
                    binding.ivGeopoliticalBackground.visibility = View.VISIBLE
                    binding.ivCrimeBackground.visibility = View.GONE
                    binding.ivSocialUnrestBackground.visibility = View.GONE
                    setColorAndProgress(
                        riskType,
                        getString(R.string.geopolitical_risk_level),
                        binding.ivGeopoliticalBackground, true
                    )
                } else {
                    showSingleButtonDialog()
                }
            }

            binding.ivSocialUnrest.id -> {
                for (index in listSubCategory.indices) {
                    if (listSubCategory[index].risk_category_name.contains("Geopolitical")) {
                        isContainRiskLevel = true
                        riskType = listSubCategory[index].risk_type_name
                        break
                    }
                }

                if (isContainRiskLevel) {
                    binding.ivTerrorismBackground.visibility = View.GONE
                    binding.ivGeopoliticalBackground.visibility = View.GONE
                    binding.ivCrimeBackground.visibility = View.GONE
                    binding.ivSocialUnrestBackground.visibility = View.VISIBLE
                    setColorAndProgress(
                        riskType,
                        getString(R.string.social_unrest_risk_level),
                        binding.ivSocialUnrestBackground, true
                    )
                } else {
                    showSingleButtonDialog()
                }
            }

            binding.tvRiskOverall.id -> {
                val bundle = Bundle()
                securityAlertListData?.let {
                    bundle.putParcelableArrayList(
                        Constants.SECURITY_ALERT_DATA_LIST,
                        it as ArrayList<out Parcelable>
                    )
                }

                bundle.putParcelableArrayList(
                    Constants.RISK_LIST,
                    listSubCategory as ArrayList<out Parcelable>
                )

                bundle.putParcelableArrayList(
                    "data",
                    riskLevelListData as ArrayList<out Parcelable>
                )
                bundle.putParcelable("countryRiskLevelData", countryRiskLevelData)
                bundle.putString("isSelected", binding.tvRiskOverall.text.toString())
                (activity as DashboardActivity).addFragment(RiskLevelFragment(), bundle)
            }

            binding.tvCountryName.id -> {
                val countryModel =CountryModel("","",countryRiskLevelData.countryId,"")
                val bundle = Bundle()
                bundle.putParcelable(Constants.COUNTRY_MODEL, countryModel)
                (activity as DashboardActivity).replaceFragment(CountryDetailFragment(), bundle)
            }
        }
    }

    private fun setUp() {
        (activity as DashboardActivity?)?.run {
            //setTitle(countryRiskLevelData.countryName)
            setTitle("")
            hideNotificationIcon()
            showBackArrow()
            showSearchBar()
            //setPositiveToolbarPadding()
            //  showCountryTopBar()
        }
    }


    private fun showSingleButtonDialog() {
        val dialog = SingleButtonDialogLikeIos("",
            getString(R.string.risk_level_data_not_found),
            object : SingleButtonDialogLikeIos.OnClickListener {
                override fun onConfirmationDialogButtonClick(which: Int) {
                }
            })
        dialog.show(childFragmentManager, "dialog")
    }

    private fun setColorAndProgress(
        riskType: String,
        textCenter: String,
        view: ImageView,
        isView: Boolean
    ) {
        when (riskType) {
            "Minimal Risk" -> {
                binding.progressBar.progressTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.minimal
                    )
                )
                binding.progressBar.progress = 37
                binding.tvRiskOverall.text = textCenter
                binding.tvRiskLevel.text = riskType
                binding.tvRiskLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.minimal
                    )
                )
                if (isView) {
                    view.setImageResource(R.color.light_minimal)
                }
            }
            "Low Risk" -> {
                binding.progressBar.progressTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.low
                    )
                )
                binding.progressBar.progress = 25
                binding.tvRiskOverall.text = textCenter
                binding.tvRiskLevel.text = riskType
                binding.tvRiskLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.low
                    )
                )
                if (isView) {
                    view.setImageResource(R.color.light_low)
                }
            }
            "Moderate Risk" -> {
                binding.progressBar.progressTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.moderate
                    )
                )
                binding.progressBar.progress = 55
                binding.tvRiskOverall.text = textCenter
                binding.tvRiskLevel.text = riskType
                binding.tvRiskLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.moderate
                    )
                )
                if (isView) {
                    view.setImageResource(R.color.light_moderate)
                }
            }
            "High Risk" -> {
                binding.progressBar.progressTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.high
                    )
                )
                binding.progressBar.progress = 75
                binding.tvRiskOverall.text = textCenter
                binding.tvRiskLevel.text = riskType
                binding.tvRiskLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.high
                    )
                )
                if (isView) {
                    view.setImageResource(R.color.light_high)
                }
            }
            "Extreme Risk" -> {
                binding.progressBar.progressTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.extreme_select
                    )
                )
                binding.progressBar.progress = 85
                binding.tvRiskOverall.text = textCenter
                binding.tvRiskLevel.text = riskType
                binding.tvRiskLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray
                    )
                )
                if (isView) {
                    view.setImageResource(R.color.light_extreme)
                }
            }
        }
    }

    private fun setRiskButtonColorAndText() {
        binding.tvLevel.visibility = View.VISIBLE
        when (countryRiskLevelData.riskTypeName) {
            "Minimal Risk" -> {
                binding.tvLevel.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_minimal_risk)
                binding.tvLevel.text = countryRiskLevelData.riskTypeName
                binding.tvLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
            "Low Risk" -> {
                binding.tvLevel.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_low_risk)
                binding.tvLevel.text = countryRiskLevelData.riskTypeName
                binding.tvLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
            "Moderate Risk" -> {
                binding.tvLevel.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_moderate_risk)
                binding.tvLevel.text = countryRiskLevelData.riskTypeName
                binding.tvLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
            "High Risk" -> {
                binding.tvLevel.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_high_risk)
                binding.tvLevel.text = countryRiskLevelData.riskTypeName
                binding.tvLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
            "Extreme Risk" -> {
                binding.tvLevel.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_extreme_risk)
                binding.tvLevel.text = countryRiskLevelData.riskTypeName
                binding.tvLevel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
        }
        setColorAndProgress(
            countryRiskLevelData.riskTypeName,
            getString(R.string.overall_risk_level),
            binding.ivTerrorismBackground, false
        )
    }
}