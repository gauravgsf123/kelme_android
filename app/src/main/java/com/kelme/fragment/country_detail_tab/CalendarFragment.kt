package com.kelme.fragment.country_detail_tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.adapter.CalenderAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentCalendarBinding
import com.kelme.fragment.country.CountryViewModal
import com.kelme.model.CalenderModel
import com.kelme.model.response.CalendarData
import com.kelme.utils.ViewModalFactory

class CalendarFragment(var bundle: Bundle) : BaseFragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var viewModal: CountryViewModal
    private lateinit var calenderData: ArrayList<CalendarData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        calenderData = bundle.getParcelableArrayList<CalendarData>("data")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container, false)
        init()
        return binding.root
    }

    private fun init() {
        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            CountryViewModal::class.java
        )

        binding.recyclerviewCalender.adapter = CalenderAdapter()
        (binding.recyclerviewCalender.adapter as CalenderAdapter).setItems(calenderData)
    }



}