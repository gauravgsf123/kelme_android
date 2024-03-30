package com.kelme.fragment.security

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.activity.GlobalCountryPickerActivity
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.SecurityAlertListAdapter
import com.kelme.adapter.StringRecyclerAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentSecurityAlertsBinding
import com.kelme.databinding.PopupDistanceBinding
import com.kelme.databinding.PopupFilterBinding
import com.kelme.event.DateEvent
import com.kelme.event.DateEventTo
import com.kelme.event.FilterEvent
import com.kelme.fragment.country.CountryDetailFragment
import com.kelme.fragment.country.CountryViewModal
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.CountryModel
import com.kelme.model.CountrySearchModel
import com.kelme.model.SecurityAlertModel
import com.kelme.model.request.SecurityAlertListRequest
import com.kelme.model.response.RiskLevelListData
import com.kelme.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SecurityAlertsFragment : BaseFragment() {

    private lateinit var binding: FragmentSecurityAlertsBinding
    private lateinit var viewModal: SecurityViewModal
    private lateinit var viewModalCountry: CountryViewModal
    private var list: ArrayList<SecurityAlertModel> = ArrayList()
    private lateinit var adapter: SecurityAlertListAdapter
    private lateinit var popupBinding: PopupFilterBinding
    private lateinit var popupWindow: PopupWindow

    var countryCode = ""
    private var listCategory: ArrayList<RiskLevelListData> = ArrayList()

    private var latitude = ""
    private var longitude = ""
    private var category = ""
    private var date = ""
    private var isPopUpVisible = true

    private var dateValue = ""
    private var dateValueTo = ""
    private var categoryValue = ""
    private var locationValue = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_security_alerts, container, false)

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            SecurityViewModal::class.java)

        viewModalCountry = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            CountryViewModal::class.java)

        if(PrefManager.read(PrefManager.MODULEID,"")!="" && PrefManager.read(PrefManager.MODULEID,"")!=null) {
            val bundle = Bundle()
            bundle.putString(
                Constants.SECURITY_ALERT_MODEL,
                PrefManager.read(PrefManager.MODULEID,"")
            )
            (activity as DashboardActivity).replaceFragment(
                SecurityAlertsDetailFragment(),
                bundle
            )
        }

        setUI()
        setObserver()
        securityAlertList()
        riskCategoryData()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        (activity as DashboardActivity?)?.run {
            setTitle(getString(R.string.security_alerts))
            hideNotificationIcon()
            showBackArrow()
            showSearchBar()
            hideUnreadCount()
            //setPositiveToolbarPadding()
            showFilter()
        }
    }


    private fun setUI() {

        binding.imageView3.setOnClickListener {
            val request = SecurityAlertListRequest(
                    countryCode,
                    dateValue,
                    dateValueTo,
                    category,
                    binding.etSearch.text.toString()
                )
                viewModal.securityAlertList(request)
        }

        binding.rvSecurityAlert.setHasFixedSize(true)
        binding.rvSecurityAlert.layoutManager = GridLayoutManager(
            requireContext(),
            1,
            RecyclerView.VERTICAL,
            false
        )

        adapter = SecurityAlertListAdapter(requireContext(), list)

        binding.rvSecurityAlert.adapter = adapter

        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                val bundle = Bundle()
                    bundle.putString(
                        Constants.SECURITY_ALERT_MODEL,
                        list[position].security_alert_id
                    )
                    (activity as DashboardActivity).replaceFragment(
                        SecurityAlertsDetailFragment(),
                        bundle)
            }
        })
    }

    private fun setObserver() {
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
            }
        }

        viewModal.securityAlertList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                        list.clear()
                        list = response.data as ArrayList<SecurityAlertModel>
                        adapter.updateItems(list)
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
                        list.clear()
                        adapter.notifyDataSetChanged()
//                        Toast.makeText(
//                            requireContext(),
//                            response.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }
        }

        viewModalCountry.countrySearchData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                        val countrySearchModel = response.data as CountrySearchModel
                        val countryModel = CountryModel(
                            countrySearchModel.country_code,
                            "",
                            countrySearchModel.country_id,
                            countrySearchModel.country_name
                        )
                        binding.etSearch.setText("")
                        val bundle = Bundle()
                        bundle.putParcelable(Constants.COUNTRY_MODEL, countryModel)
                        (activity as DashboardActivity).replaceFragment(
                            CountryDetailFragment(),
                            bundle
                        )
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
            }
        }

        viewModal.riskLevelData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                        listCategory.clear()
                        listCategory = response.data as ArrayList<RiskLevelListData>
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
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FilterEvent?) {
        popupFilter(binding.root)
    }

    private fun securityAlertList() {
        if(dateValue=="From"){
            dateValue=""
        }else if(dateValueTo=="To"){
            dateValueTo=""
        }

        val request = SecurityAlertListRequest(
            countryCode,
            dateValue,
            dateValueTo,
            category,
            binding.etSearch.text.toString()
        )
        viewModal.securityAlertList(request)
    }

    private fun riskCategoryData() {
        viewModal.riskLevelList()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        if (::popupBinding.isInitialized && isPopUpVisible) {
            popupWindow.dismiss()
        }
    }

    private fun popupFilter(view: View?) {
        // inflate the layout of the popup window
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        popupBinding = DataBindingUtil.inflate(inflater, R.layout.popup_filter, null, false)
        popupWindow = PopupWindow(
            popupBinding.root,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        popupWindow.showAtLocation(view, Gravity.TOP, 0, 0)

        popupBinding.ivCategoryCancel.setOnClickListener {
            popupBinding.tvCategoryValue.text = " "
            category = ""
            categoryValue=""
            popupBinding.ivCategoryCancel.visibility = View.GONE
        }

        popupBinding.ivCountryCancel.setOnClickListener {
            popupBinding.tvLocationValue.text = " "
            countryCode = ""
            locationValue=""
            popupBinding.ivCountryCancel.visibility = View.GONE
        }

        popupBinding.tvDateTo.setOnClickListener {
            val newFragment = DatePickerFragment("datePickerTo")
            newFragment.show(childFragmentManager, "datePickerTo")
        }

        popupBinding.tvDateFrom.setOnClickListener {
            val newFragment = DatePickerFragment("datePickerFrom")
            newFragment.show(childFragmentManager, "datePickerFrom")
        }

        if (dateValue != "") {
            popupBinding.tvDateFrom.text = dateValue
        } else {
            date = ""
        }

        if (dateValueTo != "") {
            popupBinding.tvDateTo.text = dateValueTo
        } else {
            date = ""
        }

        if (categoryValue.isEmpty() || categoryValue.isBlank()) {
            popupBinding.ivCategoryCancel.visibility=View.INVISIBLE
        } else {
            popupBinding.tvCategoryValue.text = categoryValue
            popupBinding.ivCategoryCancel.visibility=View.VISIBLE
        }

        if (locationValue.isEmpty() || locationValue.isBlank()) {
            popupBinding.ivCountryCancel.visibility=View.INVISIBLE
        } else {
            popupBinding.tvLocationValue.text = locationValue
            popupBinding.ivCountryCancel.visibility=View.VISIBLE
        }

        popupBinding.tvApply.setOnClickListener {
            dateValue = popupBinding.tvDateFrom.text.toString()
            dateValueTo = popupBinding.tvDateTo.text.toString()
            if((dateValue=="From"&& dateValueTo!="To") || (dateValue!="From"&& dateValueTo=="To")) {
//                Toast.makeText(requireContext(), "Please enter both dates", Toast.LENGTH_SHORT).show()
            }else{
                popupWindow.dismiss()
                categoryValue = popupBinding.tvCategoryValue.text.toString()
                locationValue = popupBinding.tvLocationValue.text.toString()
                securityAlertList()
            }
        }
        popupBinding.tvCancel.setOnClickListener {
            dateValue = ""
            dateValueTo = ""
            categoryValue = ""
            locationValue = ""
            popupBinding.tvDateValue.text = ""
            popupBinding.tvDateFrom.text = ""
            popupBinding.tvDateTo.text = ""
            popupBinding.tvCategoryValue.text = ""
            popupBinding.tvLocationValue.text = ""
            popupWindow.dismiss()
            date = ""
            latitude = ""
            longitude = ""
            category = ""
            popupBinding.ivCategoryCancel.visibility = View.GONE
            popupBinding.ivCountryCancel.visibility = View.GONE
            securityAlertList()
        }

        popupBinding.tvLocation.setOnClickListener {
            isPopUpVisible = false
            startActivityForResult(
                Intent(requireContext(), GlobalCountryPickerActivity::class.java),
                Constants.REQUEST_CODE_ADDRESS
            )
        }

        popupBinding.tvCategory.setOnClickListener {
            popupFilterCategory(binding.root, popupBinding)
        }
    } //eof popup


    private fun popupFilterCategory(view: View?, popupFilterBinding: PopupFilterBinding) {
        // inflate the layout of the popup window
        val inflater =
            activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val popupBinding1: PopupDistanceBinding =
            DataBindingUtil.inflate(inflater, R.layout.popup_distance, null, false)
        val popupWindow1 = PopupWindow(
            popupBinding1.root,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        // popupBinding = DataBindingUtil.bind(customView)!!

        popupWindow1.isFocusable = true
        // Settings disappear when you click somewhere else
        popupWindow1.isOutsideTouchable = true

        // popupWindow.softInputMode = PopupWindow.INPUT_METHOD_NEEDED
        //popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING

        popupWindow1.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //  if (Build.VERSION.SDK_INT >= 21) {
        popupWindow1.elevation = 5.0f
        // }


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            popupWindow1.showAtLocation(binding.root, Gravity.CENTER or Gravity.START, 20, 0)
        } else {
            popupWindow1.showAsDropDown(popupFilterBinding.tvCategory, 0, 0)
        }

        Log.e("popCheck", popupWindow1.isShowing.toString())
        val list: ArrayList<String> = ArrayList()
       // listCategory.sortedWith(compareBy { it.name })
        listCategory.forEach { list.add(it.name) }
        val adapter = StringRecyclerAdapter(
            context = requireContext(),
            list = list
        )
       // list.sort()

        popupBinding1.rvDistance.setHasFixedSize(true)
        popupBinding1.rvDistance.layoutManager = GridLayoutManager(
            requireContext(),
            1,
            RecyclerView.VERTICAL,
            false
        )
        popupBinding1.rvDistance.adapter = adapter

        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                popupFilterBinding.tvCategoryValue.text = listCategory[position].name
                popupBinding.ivCategoryCancel.visibility = View.VISIBLE
                category = listCategory[position].risk_category_id
                popupWindow1.dismiss()
            }
        })

    } //eof popup


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: DateEvent) {
        Log.e("date", event.date)
        if (::popupBinding.isInitialized) {
            popupBinding.tvDateFrom.text = event.date
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: DateEventTo) {
        Log.e("date", event.date)
        if (::popupBinding.isInitialized) {
            popupBinding.tvDateTo.text = event.date
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        isPopUpVisible = true

        if (requestCode == Constants.REQUEST_CODE_ADDRESS && resultCode == Activity.RESULT_OK) {
            val bundle = data?.extras
            countryCode = bundle?.getString("id").toString()
            val countryModel = CountryModel(
                bundle?.getString("code").toString(),
                "",
                bundle?.getString("id").toString(),
                bundle?.getString("name").toString()
            )
            popupBinding.ivCountryCancel.visibility = View.VISIBLE
            popupBinding.tvLocationValue.text = bundle?.getString("name")
        }
    }
}