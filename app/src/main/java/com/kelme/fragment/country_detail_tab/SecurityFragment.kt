package com.kelme.fragment.country_detail_tab

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.kelme.R
import com.kelme.adapter.CalenderAdapter
import com.kelme.adapter.CountrySecurityAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentSecurityBinding
import com.kelme.model.response.CountryRiskLevelData
import com.kelme.model.response.CountryRiskLevelResponse
import com.kelme.model.response.Security


class SecurityFragment(var bundle: Bundle) : BaseFragment() {
    private lateinit var binding: FragmentSecurityBinding
    private lateinit var data: CountryRiskLevelData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = bundle.getParcelable("data")!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_security, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.recyclerviewCalender.adapter = CountrySecurityAdapter()
        (binding.recyclerviewCalender.adapter as CountrySecurityAdapter).setItems(data.security as ArrayList<Security>)

    }


}