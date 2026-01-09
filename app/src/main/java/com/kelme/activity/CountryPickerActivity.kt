package com.kelme.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.CountryPickerListAdapter
import com.kelme.databinding.ActivityCountryPickerBinding
import com.kelme.fragment.country.CountryViewModal
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.CountryModel
import com.kelme.utils.PrefManager
import com.kelme.utils.ProgressDialog
import com.kelme.utils.Resource
import com.kelme.utils.ViewModalFactory

class CountryPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountryPickerBinding
    private lateinit var viewModal: CountryViewModal
    private var listCountry: ArrayList<CountryModel> = ArrayList()
    private var listCountryMaster: ArrayList<CountryModel> = ArrayList()
    private lateinit var adapter: CountryPickerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_country_picker)

        viewModal = ViewModelProvider(this, ViewModalFactory(application)).get(
            CountryViewModal::class.java
        )
        setUI()
        setObserver()
        countryList()
    }

    private fun setUI() {
        binding.backArrow.setOnClickListener {
            onBackPressed()
        }

        binding.rvCountry.setHasFixedSize(true)
        binding.rvCountry.layoutManager = GridLayoutManager(
            this,
            1,
            RecyclerView.VERTICAL,
            false
        )

        adapter = CountryPickerListAdapter(this, listCountry)

        binding.rvCountry.adapter = adapter

        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                val intent= Intent()
                intent.putExtra("code",listCountry[position].country_code)
                intent.putExtra("id",listCountry[position].country_id)
                intent.putExtra("name",listCountry[position].country_name)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
        })


        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                listCountry = listCountryMaster.filter {
                    it.country_name.contains( p0.toString(),true)
                } as ArrayList<CountryModel>
                adapter.updateItems(listCountry)
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    private fun setObserver() {
        viewModal.logout.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        PrefManager.clearUserPref()
//                    Toast.makeText(
//                        applicationContext,
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        applicationContext,
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    if (response.message == "Your session has been expired, Please login again.") {
                        PrefManager.clearUserPref()
                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }
                }
                else -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        }

        viewModal.countryList.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        listCountry.clear()
                        listCountryMaster.clear()
                        listCountry = response.data as ArrayList<CountryModel>
                        listCountryMaster.addAll(listCountry)
                        adapter.updateItems(listCountry)
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
//                        Toast.makeText(
//                            this,
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
        viewModal.allCountryList()
    }
}