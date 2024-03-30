package com.kelme.fragment.country_detail_tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.kelme.R
import com.kelme.adapter.CountryOtherAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentOtherBinding
import com.kelme.model.AlertModel
import com.kelme.model.response.CountryRiskLevelData
import com.kelme.model.response.Other


class OtherFragment(var bundle: Bundle) : BaseFragment() {
    private lateinit var binding: FragmentOtherBinding
    private lateinit var data: CountryRiskLevelData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = bundle.getParcelable<CountryRiskLevelData>("data")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_other, container, false)
        init()
        return binding.root
    }

    private fun init() {
        binding.recyclerviewCalender.adapter = CountryOtherAdapter()
        (binding.recyclerviewCalender.adapter as CountryOtherAdapter).setItems(getList(data?.other))
    }

    private fun getList(other: List<Other>?):ArrayList<Other>{
        var list: MutableList<Other> = mutableListOf()
        for(i in 0 until other?.size!!){
            if(other[i].description!="" && other[i].description.isNotEmpty()){
                list.add(other[i])
            }
        }
        return list as ArrayList<Other>
    }


}