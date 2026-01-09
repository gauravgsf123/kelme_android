package com.kelme.fragment.country

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.CountryListAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentCountryListBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.CountryModel
import com.kelme.model.CountryOutlookModel
import com.kelme.model.request.CountryListRequest
import com.kelme.utils.*


class CountryListFragment : BaseFragment() {

    private lateinit var binding: FragmentCountryListBinding
    private lateinit var viewModal: CountryViewModal
    private lateinit var countryOutlookModel: CountryOutlookModel

    private var listCountry: ArrayList<CountryModel> = ArrayList()
    private lateinit var adapter: CountryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            countryOutlookModel = it.getParcelable(Constants.COUNTRY_OUTLOOK_MODEL)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_country_list, container, false)

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            CountryViewModal::class.java
        )

        setUI()
        setObserver()
        countryList()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle(countryOutlookModel.country_outlook_name)
            hideNotificationIcon()
            showBackArrow()
            showSearchBar()
            hideUnreadCount()
            //setPositiveToolbarPadding()
        }
    }


    private fun setUI() {

        binding.rvCountry.setHasFixedSize(true)

        adapter = CountryListAdapter(requireContext(), listCountry)

        binding.rvCountry.adapter = adapter

        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                val bundle = Bundle()
                bundle.putParcelable(Constants.COUNTRY_MODEL, listCountry[position])
                (activity as DashboardActivity).replaceFragment(CountryDetailFragment(), bundle)
            }
        })
    }

    private fun setObserver() {
        viewModal.logout.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
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

        viewModal.countryList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        binding.loader.visibility = View.INVISIBLE
                        listCountry.clear()
                        listCountry = response.data as ArrayList<CountryModel>
                        //  listCountry.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER, { it.country_name }))
                        adapter.updateItems(listCountry)

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

    private fun countryList() {
        val request = CountryListRequest(
            countryOutlookModel.country_outlook_id
        )
        viewModal.countryList(request)
    }
}