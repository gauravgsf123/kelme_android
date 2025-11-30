package com.kelme.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kelme.R
import com.kelme.activity.chat.ChatConversationActivity
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.dashboard.DashboardViewModal
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.ContactListAdapter
import com.kelme.databinding.FragmentContactBinding
import com.kelme.fragment.country.CountryViewModal
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.ContactModel
import com.kelme.model.ContactUserDetailsModel
import com.kelme.model.request.ContactListRequest
import com.kelme.utils.*
import java.util.Locale
import java.util.Locale.getDefault

class ContactFragment : Fragment() {
    private lateinit var binding: FragmentContactBinding
    private lateinit var viewModal: DashboardViewModal
    private lateinit var viewModalCountry: CountryViewModal
    private var contactModel: ContactModel = ContactModel()
    val temp: ArrayList<ContactUserDetailsModel> = ArrayList()
    private var list: ArrayList<ContactUserDetailsModel> = ArrayList()
    private lateinit var adapter: ContactListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_contact, container, false)

        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            DashboardViewModal::class.java)

        viewModalCountry = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            CountryViewModal::class.java)

        setUI()
        setObserver()
        contactList()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle("Contacts")
            hideNotificationIcon()
            showBackArrow()
            hideSearchBar()
            hideUnreadCount()
        }
    }

    private fun setUI() {
        binding.rvContact.setHasFixedSize(true)
        binding.rvContact.layoutManager = GridLayoutManager(
            requireContext(),
            1,
            RecyclerView.VERTICAL,
            false
        )

        binding.etChatSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })

        adapter = ContactListAdapter(requireContext(), list)

        binding.rvContact.adapter = adapter

        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                PrefManager.write(PrefManager.IS_CALL_FROM_CHAT_LIST_FRAG, Constants.ActivityType.NO)
                if(temp.size>0) {
                    contactModel.profilePicture = temp[position].image.toString()
                    contactModel.name = temp[position].name.toString()
                    contactModel.userId = temp[position].fireBaseId.toString()
                    val intent = Intent(requireContext(), ChatConversationActivity::class.java)
                    intent.putExtra(Constants.SINGLE_CHAT_MODEL, contactModel)
                    intent.putExtra("otherUserId", temp[position].fireBaseId)
                    startActivity(intent)
                }else{
                    contactModel.profilePicture = list[position].image.toString()
                    contactModel.name = list[position].name.toString()
                    contactModel.userId = list[position].fireBaseId.toString()
                    val intent = Intent(requireContext(), ChatConversationActivity::class.java)
                    intent.putExtra(Constants.SINGLE_CHAT_MODEL, contactModel)
                    intent.putExtra("otherUserId", list[position].fireBaseId)
                    startActivity(intent)
                }
            }
        })
    }

    fun filter(text: String?) {
        temp.clear()
        for (d in list) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d != null) {
                try {
                    if(d.name!="") {
                        if ((d.name!!.lowercase(getDefault())).contains(text.toString())) {
                            temp.add(d)
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
        //update recyclerview
        adapter.updateItemsTemp(temp)
    }

    private fun setObserver() {
        viewModal.logout.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    ProgressDialog.hideProgressBar()
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
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
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

        viewModal.contactList.observe(viewLifecycleOwner) { response ->
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
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
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

    private fun contactList() {
        val companyId= PrefManager.read(PrefManager.COMPANYID,"")
        viewModal.contactList(ContactListRequest(companyId))
    }

}