package com.kelme.fragment.country_detail_tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentOverviewBinding
import com.kelme.model.response.CountryRiskLevelData
import com.kelme.model.response.CountryRiskLevelResponse


class OverviewFragment(var bundle: Bundle) : BaseFragment() {
    private lateinit var binding:FragmentOverviewBinding
    private lateinit var data: CountryRiskLevelData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = bundle.getParcelable<CountryRiskLevelData>("data")!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_overview, container, false)
        init()
        return binding.root
    }

    private fun init(){
        setUpToolbar()
        setUpData()
    }

    private fun setUpData()
    {
        binding.tvCapital.text = data.capital
        binding.tvPopulation.text = data.population
        binding.tvArea.text = data.area + "sqkm"
        binding.tvOfficialLanguage.text = data.officialLanguage
        binding.tvMajorReligion.text = data.religionName
        binding.tvCurrency.text = data.cuurencyName
        binding.tvTvTimezone.text = data.timezoneName
        binding.tvDialCodeVal.text = data.dialCode
        binding.tvTvPolice.text = data.policeNo
        binding.tvTvAmbulance.text = data.ambulanceNo
        binding.tvTvFire.text = data.fireNo
        binding.tvTvElectercity.text = data.electricity
    }
    private fun setUpToolbar() {
        (activity as DashboardActivity?)?.run {
            setTitle("")
            hideNotificationIcon()
            showBackArrow()
            showSearchBar()
            //setPositiveToolbarPadding()
        }
    }
}