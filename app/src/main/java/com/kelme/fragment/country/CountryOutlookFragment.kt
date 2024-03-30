package com.kelme.fragment.country

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.activity.CountryPickerActivity
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.CountryOutlookListAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentCountryOutlookBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.CountryModel
import com.kelme.model.CountryOutlookModel
import com.kelme.model.CountrySearchModel
import com.kelme.model.request.GetCountryRequest
import com.kelme.utils.*


class CountryOutlookFragment : BaseFragment() {

    private lateinit var binding: FragmentCountryOutlookBinding
    private lateinit var viewModal: CountryViewModal
    private var listCountryModel: ArrayList<CountryOutlookModel> = ArrayList()
    private lateinit var adapter: CountryOutlookListAdapter
    private var check = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_country_outlook, container, false)

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            CountryViewModal::class.java)

        setUI()
        setObserver()
        countryOutlookList()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle(getString(R.string.country_outlook))
            hideNotificationIcon()
            showBackArrow()
            showSearchBar()
            hideUnreadCount()
        }
    }

    private fun setUI() {
        binding.rvCountryOutlook.setHasFixedSize(true)

        adapter = CountryOutlookListAdapter(requireContext(), listCountryModel)

        binding.rvCountryOutlook.adapter = adapter

        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                val bundle = Bundle()
                bundle.putParcelable(Constants.COUNTRY_OUTLOOK_MODEL, listCountryModel[position])
                (activity as DashboardActivity).replaceFragment(CountryListFragment(), bundle)
            }
        })

        check = false
        binding.imgSearch.setOnClickListener {
            if (TextUtils.isEmpty(binding.etSearch.text)) {
//                Toast.makeText(
//                    requireContext(),
//                    resources.getString(R.string.enter_country),
//                    Toast.LENGTH_SHORT
//                ).show()
            } else {
                getCountryByName()
            }
        }

        binding.etSearch.setOnClickListener {
            startActivityForResult(Intent(activity, CountryPickerActivity::class.java), Constants.REQUEST_CODE_COUNTRY)
        }
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
                   // ProgressDialog.showProgressBar(requireContext())
                    binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                   // ProgressDialog.hideProgressBar()
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


        viewModal.countryOutlookList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        //ProgressDialog.hideProgressBar()
                        binding.loader.visibility = View.INVISIBLE
                        listCountryModel.clear()
                        listCountryModel = response.data as ArrayList<CountryOutlookModel>
                        adapter.updateItems(listCountryModel)

                    }
                }
                is Resource.Loading -> {
                    //ProgressDialog.showProgressBar(requireContext())
                    binding.loader.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.loader.visibility = View.INVISIBLE
                   // ProgressDialog.hideProgressBar()
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

        viewModal.countrySearchData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                       // ProgressDialog.hideProgressBar()
                        val countrySearchModel = response.data as CountrySearchModel
                        val countryModel = CountryModel(
                            countrySearchModel.country_code,
                            "",
                            countrySearchModel.country_id,
                            countrySearchModel.country_name
                        )
                        binding.etSearch.setText("")
                        if (check) {
                            val bundle = Bundle()
                            bundle.putParcelable(Constants.COUNTRY_MODEL, countryModel)
                            (activity as DashboardActivity).replaceFragment(
                                CountryDetailFragment(),
                                bundle
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    binding.loader.visibility = View.VISIBLE
                   // ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                  //  ProgressDialog.hideProgressBar()
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

    private fun countryOutlookList() {
        viewModal.countryOutlookList()
    }

    private fun getCountryByName() {
        check = true
        val request = GetCountryRequest(
            binding.etSearch.text.toString().trim()
        )
        viewModal.getCountryByName(request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_COUNTRY && resultCode == Activity.RESULT_OK) {
            val bundle = data?.extras
            val countryModel = CountryModel(
                bundle?.getString("code").toString(),
                "",
                bundle?.getString("id").toString(),
                bundle?.getString("name").toString()
            )
            binding.etSearch.text = ""

            val bundleSend = Bundle()
            bundleSend.putParcelable(Constants.COUNTRY_MODEL, countryModel)
            (activity as DashboardActivity).replaceFragment(CountryDetailFragment(), bundleSend)
        }
    }

}