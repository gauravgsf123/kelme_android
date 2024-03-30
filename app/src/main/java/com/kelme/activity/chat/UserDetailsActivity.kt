package com.kelme.activity.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kelme.R
import com.kelme.activity.login.LoginActivity
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityUserDetailsBinding
import com.kelme.fragment.country.CountryViewModal
import com.kelme.model.ContactModel
import com.kelme.model.OtherUserDetailsModel
import com.kelme.model.request.GetOtherUserRequest
import com.kelme.utils.*

class UserDetailsActivity : BaseActivity() {

    private lateinit var userProfileData: OtherUserDetailsModel
    val temp: ArrayList<ContactModel?> = ArrayList()
    lateinit var binding: ActivityUserDetailsBinding
    private var contactModel: ContactModel = ContactModel()
    var userId: String = ""
    private lateinit var viewModal: CountryViewModal
    var list = ArrayList<OtherUserDetailsModel>()
    var name = ""
    var email = ""
    var mobile = ""
    var profile = ""
    var gender = ""
    var address = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_details)

        viewModal =
            ViewModelProvider(this, ViewModalFactory(application)).get(CountryViewModal::class.java)

        userId = intent.getStringExtra("userId").toString()

        onClickMethod()
        getUserDetails()
        setObserver()
    }

    private fun setUi() {
        if (name == ""|| name == "NA"||name == "null") {
            binding.clname.visibility = View.GONE
        } else {
            binding.clname.visibility = View.VISIBLE
        }
        if (email == ""|| email == "NA"||email == "null") {
            binding.clemail.visibility = View.GONE
        } else {
            binding.clemail.visibility = View.VISIBLE
        }
        if (address == ""|| address == "NA"||address == null) {
            binding.claddress.visibility = View.GONE
        } else {
            binding.claddress.visibility = View.VISIBLE
        }
        if (mobile == ""|| mobile == "NA"||mobile == "null") {
            binding.clmobile.visibility = View.GONE
        } else {
            binding.clmobile.visibility = View.VISIBLE
        }
        if (gender == ""|| gender == "NA"||gender == "null") {
            binding.clgender.visibility = View.GONE
        } else {
            binding.clgender.visibility = View.VISIBLE
        }
        binding.name.text = name
        binding.email.text = email
        binding.address.text = address
        binding.mobileNo.text = mobile
        binding.gender.text = gender
        Utils.loadImage(this, binding.profileImage, profile)

        try {
            userProfileData.name = name
            userProfileData.image = profile
            userProfileData.user_id = userId
        } catch (e: Exception) {
            userProfileData.name = ""
            userProfileData.image = ""
            userProfileData.user_id = ""
        }
    }

    private fun setObserver() {
        viewModal.logout.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
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
            }
        }

        viewModal.getOtherUserData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        userProfileData = response.data!!
                        name = response.data.name.toString()
                        email = response.data.email.toString()
                        mobile = response.data.phone_number.toString()
                        profile = response.data.image.toString()
                        gender = response.data.gender.toString()
                        address = response.data.address.toString()
                        contactModel.name = name
                        contactModel.userId = userId
                        contactModel.profilePicture = profile
                        setUi()
                        Log.d("TAG", "setObserver: $list")
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

    private fun onClickMethod() {
        binding.profileImage.setOnClickListener {
            val intent = Intent(this, FullScreenImageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        binding.backArrow.setOnClickListener { onBackPressed() }

        binding.ivChat.setOnClickListener {
            PrefManager.write(PrefManager.IS_CALL_FROM_CHAT_LIST_FRAG, Constants.ActivityType.NO)
            val intent = Intent(this, ChatConversationActivity::class.java)
            val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
            intent.putExtra(Constants.SINGLE_CHAT_MODEL, contactModel)
            intent.putExtra("otherUserId", userId)
            intent.putExtra("name", userProfileData.name)
            intent.putExtra("image", userProfileData.image)
            intent.putExtra("userid", userProfileData.user_id)
            startActivity(intent)
            finish()
        }
    }

    private fun getUserDetails() {
        val request = GetOtherUserRequest(userId)
        viewModal.otherUser(request)
    }

    override fun initializerControl() {}
}