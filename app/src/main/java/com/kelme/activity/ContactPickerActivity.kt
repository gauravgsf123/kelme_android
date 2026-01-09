package com.kelme.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.ContactListAdapter
import com.kelme.databinding.ActivityContactPickerBinding
import com.kelme.fragment.chat.ChatViewModal
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.ContactUserDetailsModel
import com.kelme.utils.*

class ContactPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactPickerBinding
    private lateinit var viewModal: ChatViewModal

    private var list: ArrayList<ContactUserDetailsModel> = ArrayList()
    private lateinit var adapter: ContactListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_picker)

        viewModal = ViewModelProvider(this, ViewModalFactory(application)).get(
            ChatViewModal::class.java
        )

        setUI()
        setObserver()
      //  contactList()
    }

    private fun setUI() {
        binding.rvContact.setHasFixedSize(true)
        /*binding.rvContact.layoutManager = GridLayoutManager(
            this,
            1,
            RecyclerView.VERTICAL,
            false
        )*/

        adapter = ContactListAdapter(this, list)

        binding.rvContact.adapter = adapter

        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                val bundle = Bundle()
                bundle.putParcelable(Constants.COUNTRY_OUTLOOK_MODEL, list[position])
                //(activity as DashboardActivity).replaceFragment(CountryListFragment(), bundle)
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
                }else -> {
                    ProgressDialog.hideProgressBar()
                }
            }
        }


        viewModal.contactList.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        list.clear()
                        list = response.data as ArrayList<ContactUserDetailsModel>
                        adapter.updateItems(list)
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
            }
        }
    }

//    private fun contactList() {
//        viewModal.contactList()
//    }

}