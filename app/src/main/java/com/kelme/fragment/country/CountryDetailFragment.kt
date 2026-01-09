package com.kelme.fragment.country

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.ViewPagerAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentCountryDetailBinding
import com.kelme.fragment.country_detail_tab.*
import com.kelme.model.CountryModel
import com.kelme.model.SubCategory
import com.kelme.model.request.CountryRiskLevelRequest
import com.kelme.model.response.CalendarData
import com.kelme.model.response.CountryRiskLevelData
import com.kelme.model.response.SecrityAlertListData
import com.kelme.utils.*


class CountryDetailFragment : BaseFragment() {

    private lateinit var binding: FragmentCountryDetailBinding
    private lateinit var viewModal: CountryViewModal
    private lateinit var calendarData: ArrayList<CalendarData>
    private val mTabTitleList = ArrayList<String>()
    private val mTabFragmentList = ArrayList<BaseFragment>()
    private lateinit var mTabAdapter: ViewPagerAdapter
    private lateinit var countryModel: CountryModel
    private lateinit var countryRiskLevelData: CountryRiskLevelData
    private var securityAlertListData: List<SecrityAlertListData>?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_country_detail, container, false)
        arguments?.let {
            countryModel = it.getParcelable(Constants.COUNTRY_MODEL)!!
        }
        init()
        return binding.root
    }

    private fun init()
    {
        setUp()
        setupTabLayout()

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            CountryViewModal::class.java
        )
        binding.cvLevelIcon.setOnClickListener {
            (activity as DashboardActivity).replaceFragment(
                OverAllRiskLevelFragment(),
                Bundle.EMPTY
            )
        }
        viewModal.countryOutlookCategory(CountryRiskLevelRequest(countryModel.country_id.toInt()))
        setCountryOutlookCategoryObserver()
        setCountryCalenderDataObserver()
        setCountryRiskLevelObserver()
        setSecurityAlertListObserver()

        binding.tvLevel.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(Constants.DATA, countryRiskLevelData)
            securityAlertListData?.let { bundle.putParcelableArrayList(Constants.SECURITY_ALERT_DATA_LIST, it as ArrayList<out Parcelable>) }

            val list = ArrayList<SubCategory>()
            countryRiskLevelData.subCategory?.forEachIndexed { _, subCategory ->
                val subCategoryModel = SubCategory(
                    subCategory.countryManagementId,
                    subCategory.description,
                    subCategory.id,
                    subCategory.riskCategoryId,
                    subCategory.riskCategoryName,
                    subCategory.riskTypeCategoryId,
                    subCategory.riskTypeName,
                    subCategory.riskCategoryDesc,
                    false
                )
                list.add(subCategoryModel)
            }

            bundle.putParcelableArrayList(
                Constants.RISK_LIST,
                list as ArrayList<out Parcelable>
            )
            (activity as DashboardActivity).addFragment(OverAllRiskLevelFragment(), bundle)
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
    }

    private fun setCountryOutlookCategoryObserver() {
        viewModal.countryOutlookCategoryData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                        viewModal.countryRiskLevel(CountryRiskLevelRequest(countryModel.country_id.toInt()))
                        for (i in 0 until response.data?.size!! - 1) {
                            if (response.data[i].categoryTitle.isNotEmpty() && response.data[i].categoryTitle != "") {
                                // mTabTitleList.add(i, response.data[i].categoryTitle)
                            }
                        }
                    }
                }
                is Resource.Loading -> {
                    binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.loader.visibility = View.INVISIBLE
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

    private fun setCountryRiskLevelObserver() {
        viewModal.countryRiskLevelData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                        if (!OverviewFragment(Bundle.EMPTY).isAdded) {
                            setupViewPager(response.data)
                        }
                        countryRiskLevelData = response.data!!
                        setRiskButtonColorAndText()
                        viewModal.securityAlertList(CountryRiskLevelRequest(countryModel.country_id.toInt()))
                        binding.tvLevel.text = response.data.riskTypeName
                        binding.tvCountryName.text = response.data.countryName
                        Glide.with(this).load(response.data.countryFlag).into(binding.ivCountry)
                        viewModal.countryCalenderData(CountryRiskLevelRequest(countryModel.country_id.toInt()))
                    }
                }
                is Resource.Loading -> {
                    binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.loader.visibility = View.INVISIBLE
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

    private fun setSecurityAlertListObserver()
    {
        viewModal.secrityAlertListData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                        addAlertTab(response.data)
                        securityAlertListData = response.data!!
                    }
                }
                is Resource.Loading -> {
                    binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.loader.visibility = View.INVISIBLE
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            //setPositiveToolbarPadding()
            showSearchBar() }
    }


    @SuppressLint("SuspiciousIndentation")
    private fun setCountryCalenderDataObserver()
    {
        viewModal.logout.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
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
                }
                is Resource.Loading -> {
                    binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.loader.visibility = View.INVISIBLE
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

        viewModal.calendarData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                        calendarData = response.data!! as ArrayList<CalendarData>
                        addCalenderTab()
                    }
                }
                is Resource.Loading -> {
                    binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.loader.visibility = View.INVISIBLE
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        /*Toast.makeText(
                            requireContext(),
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()*/
                    }
                }
            }
        }
    }

        val bundle = Bundle()
        @SuppressLint("SetTextI18n", "ResourceAsColor")
        private fun addAlertTab(data: List<SecrityAlertListData>?)
        {
            bundle.putParcelableArrayList("data", data as ArrayList<out Parcelable>)
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.alerts), 3)
            mTabTitleList.add(3, getString(R.string.alerts))
            mTabFragmentList.add(3, AlertFragment(bundle))
            mTabAdapter.notifyDataSetChanged()
        }


    private fun addCalenderTab() {
        val bundle = Bundle()
        bundle.putParcelableArrayList("data", calendarData)
        try {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.calendar), 4)
            mTabTitleList.add(4, getString(R.string.calendar))
            mTabFragmentList.add(4, CalendarFragment(bundle))
            mTabAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.calendar), 3)
            mTabTitleList.add(3, getString(R.string.calendar))
            mTabFragmentList.add(3, CalendarFragment(bundle))
            mTabAdapter.notifyDataSetChanged()
        }
    }


    private fun setupTabLayout()
    {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                tab?.let {
                    val textView = (it.view as ViewGroup).getChildAt(1)
                    textView?.let {
                        val typeFace = ResourcesCompat.getFont(
                            requireContext(),
                            R.font.poppins_medium)

                        (textView as TextView).typeface = typeFace
                        textView.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white))
                    }
                }

                if(binding.tabLayout.tabCount>3)
               {
                   tab?.let {
                       val textView = (binding.tabLayout.getTabAt(3)?.view as ViewGroup).getChildAt(1)
                       textView?.let {
                           val typeFace = ResourcesCompat.getFont(
                               requireContext(),
                               R.font.poppins_medium
                           )
                           (textView as TextView).typeface = typeFace
                           if (textView.text == "Alerts") {
                               textView.setTextColor(
                                   ContextCompat.getColor(
                                       requireContext(),
                                       R.color.orange
                                   )
                               )
                           }else{
//                               textView.setTextColor(
//                                   ContextCompat.getColor(
//                                       requireContext(),
//                                       R.color.white))
                           }
                       }
                   }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val textView = (it.view as ViewGroup).getChildAt(1)
                    textView?.let {
                        val typeFace = getFontFromRes(R.font.poppins_medium)
                        (textView as TextView).setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.tab_unselected_color
                            )
                        )
                        textView.typeface = typeFace
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val textView = (it.view as ViewGroup).getChildAt(1)
                    textView?.let {
                        val typeFace = getFontFromRes(R.font.poppins_medium)
                        (textView as TextView).setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        textView.typeface = typeFace
                    }
                }
            }
        })

        binding.tabLayout.setupWithViewPager(binding.vpHome)
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
    }

    private fun setupViewPager(data: CountryRiskLevelData?) {

        mTabTitleList.addAll(
            arrayListOf(
                getString(R.string.overview),
                getString(R.string.security),
                getString(R.string.other)
            )
        )
        val bundle = Bundle()
        bundle.putParcelable("data", data)
        mTabFragmentList.addAll(
            arrayListOf(
                OverviewFragment(bundle),
                SecurityFragment(bundle),
                OtherFragment(bundle)
            )
        )

        binding.vpHome.adapter = ViewPagerAdapter(
            childFragmentManager,
            mTabTitleList,
            mTabFragmentList
        )

        mTabAdapter = ViewPagerAdapter(
            childFragmentManager,
            mTabTitleList,
            mTabFragmentList
        )
        binding.vpHome.adapter = mTabAdapter
    }

    private fun setUp() {
        (activity as DashboardActivity?)?.run {
            setTitle("")
            hideNotificationIcon()
            showBackArrow()
            showSearchBar()
            hideUnreadCount()
            setAppTopBackgroundColor(R.color.backgroundColor)
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as DashboardActivity?)?.run {
            setAppTopBackgroundColor(R.color.backgroundColor)
        }
    }
}

