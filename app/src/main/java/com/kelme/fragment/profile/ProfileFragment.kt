package com.kelme.fragment.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.dashboard.ImagePreviewActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.DocumentListMyProfileAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentProfileBinding
import com.kelme.model.response.DocumentData
import com.kelme.model.response.MyProfileData
import com.kelme.utils.*

class ProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var myProfileViewModel: MyProfileViewModel
    private lateinit var myProfileData: MyProfileData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        myProfileViewModel = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            MyProfileViewModel::class.java
        )
        myProfileViewModel.myProfile()
        init()
        setObserver()
        return binding.root
    }

    private fun init()
    {
        binding.rvDocument.adapter = DocumentListMyProfileAdapter().apply {
            imageViewClick = { item ->
                val bundle = Bundle()
                bundle.putParcelable(Constants.DOCUMENT_DATA, item)

                val intent = Intent(context, ImagePreviewActivity::class.java)
                intent.putExtra(Constants.DATA, item)
                startActivity(intent)
            }
            downloadClick = { item ->
                val asyncTasks = DownloadAndSaveImageTask(requireContext())
                asyncTasks.execute(item.document)
            }
        }
    }

    private fun setObserver() {
        myProfileViewModel.logout.observe(viewLifecycleOwner) { response ->
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

        myProfileViewModel.myProfileResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        myProfileViewModel.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        myProfileData = response.data!!
                        binding.name.text = response.data.name
                        binding.country.text = response.data.country_name
                        binding.emailId.text = response.data.email
                        binding.mobileNo.text = response.data.phone_number
                        binding.address.text = response.data.address
                        binding.gender.text = response.data.gender

                        if (response.data.document?.size!! > 0) {
                            binding.tvNoDocument.visibility = View.GONE
                            binding.rvDocument.visibility = View.VISIBLE
                            (binding.rvDocument.adapter as DocumentListMyProfileAdapter).setItems(
                                response.data.document as ArrayList<DocumentData>
                            )
                        } else {
                            binding.tvNoDocument.visibility = View.VISIBLE
                            binding.rvDocument.visibility = View.GONE
                        }
                        if (response.data.image != "") {
                            Glide.with(this).load(response.data.image).into(binding.profileImage)
                        } else {
                            binding.profileImage.setBackgroundResource(R.drawable.user)
                        }

                        setDataInFirebase(myProfileData)
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(requireContext())
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        myProfileViewModel.logout()
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

    private fun setDataInFirebase(myProfileData: MyProfileData) {
        PrefManager.write(PrefManager.NAME, myProfileData.name.toString())
        PrefManager.write(PrefManager.EMAIL, myProfileData.email.toString())
        PrefManager.write(PrefManager.PHONE, myProfileData.phone_number.toString())
        PrefManager.write(PrefManager.IMAGE, myProfileData.image.toString())
        PrefManager.write(PrefManager.COUNTRY_ID, myProfileData.country_id.toString())
        PrefManager.write(PrefManager.COUNTRY_NAME, myProfileData.country_name.toString())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editProfile.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(Constants.MY_PROFILE_DATA_MODEL, myProfileData)
            (activity as DashboardActivity?)?.run {
                replaceFragment(EditProfileFragment(), bundle)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle(getString(R.string.profile))
            showNotificationIcon()
            changeProfileColor()
            hideSearchBar()
            showMenu()
            setAppTopBackgroundColor(R.color.foregroundColor)
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as DashboardActivity?)?.run {
            resetColorChange()
            removeAppTopBackgroundColor()
        }
    }
}