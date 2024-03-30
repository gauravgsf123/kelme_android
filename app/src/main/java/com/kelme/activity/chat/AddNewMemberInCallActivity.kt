package com.kelme.activity.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kelme.R
import com.kelme.adapter.GroupNewMemberListAdapter
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityAddNewCallMemberBinding
import com.kelme.fragment.chat.ChatViewModal
import com.kelme.interfaces.ItemClickListener
import com.kelme.interfaces.QuantityListner
import com.kelme.model.ContactModel
import com.kelme.model.request.OtherUserRequestCallRequest
import com.kelme.utils.*

class AddNewMemberInCallActivity : BaseActivity(), QuantityListner {

    lateinit var binding: ActivityAddNewCallMemberBinding
    val temp: ArrayList<ContactModel?> = ArrayList()
    var selectedUserList: ArrayList<ContactModel?> = ArrayList()
    var membersInGroup = 0
    private lateinit var groupNewMemberListAdapter: GroupNewMemberListAdapter
    var userList: ArrayList<ContactModel?> = ArrayList()
    val chatMembers: HashMap<String, Boolean> = HashMap()
    val lastUpdates: HashMap<String, Long> = HashMap()
    private var agoraToken: String = ""
    private var channelName = ""
    private var callType = ""
    private lateinit var viewModal: ChatViewModal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_new_call_member)
        viewModal = ViewModelProvider(this, ViewModalFactory(application)).get(ChatViewModal::class.java)

        agoraToken = intent.getStringExtra("agoratoken")!!
        channelName = intent.getStringExtra("channel")!!
        callType = intent.getStringExtra("callType")!!
        setUi()
        setObserver()
    }

    private fun setObserver() {
        viewModal.otherUserRequestCallResponse.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        viewModal.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        //agoraToken = response.data?.token.toString()
                        response.data?.let {
                            showLog(response.data.message)
                            //Toast.makeText(this, "" + response.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "240") {
                        viewModal.logout()
                    } else if (response.message == "201") {
                       // showLongToast(response.message)
                    } else {
                       // showLongToast(response.message)
                    }
                }
            }
        }
    }

    private fun setUi() {
        binding.rvChat.setHasFixedSize(true)
        binding.rvChat.layoutManager = GridLayoutManager(
            applicationContext,
            1,
            RecyclerView.VERTICAL,
            false
        )

        binding.btnAdd.setOnClickListener {
//            ProgressDialog.showProgressBar(this)
            val firebaseList = mutableListOf<String>()
            val selectedUser = selectedUserList.size
            val totalMembers = membersInGroup + selectedUser
            if (totalMembers <= 0) {
              //  Toast.makeText(this, "Please Select Member", Toast.LENGTH_SHORT).show()
            } else {
                chatMembers.clear()
                for (s in selectedUserList) {
                    if (s!!.isSelected) {
                        s.userId?.let { it1 -> firebaseList.add(it1) }
                        //val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "0")
                       // Toast.makeText(this, ""+s.userId, Toast.LENGTH_SHORT).show()
                    }
                }
                val request = OtherUserRequestCallRequest(
                    channelName,
                    firebaseList,//listOf(filteredKeysMap?.keys?.first()) as List<String>,
                    PrefManager.read(PrefManager.USER_ID, "").toInt(),
                    agoraToken,
                    callType,
                    Constants.ChatType.SINGLE
                )
                viewModal.otherUserRequestCall(request)
            }
            finish()
        }

        binding.etChatSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.backArrow.setOnClickListener { onBackPressed() }

        groupNewMemberListAdapter =
            GroupNewMemberListAdapter(applicationContext, userList, this)

        binding.rvChat.adapter = groupNewMemberListAdapter

        groupNewMemberListAdapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
//                PrefManager.write(PrefManager.IS_CALL_FROM_CHAT_LIST_FRAG, Constants.ActivityType.NO)
//                val intent = Intent(this@AddNewGroupMemberActivity, ChatConversationActivity::class.java)
//                if(temp.size>0) {
//                    intent.putExtra(Constants.SINGLE_CHAT_MODEL, temp[position])
//                }else{
//                    intent.putExtra(Constants.SINGLE_CHAT_MODEL, userList[position])
//                }
//                startActivity(intent)
//                finish()
            }
        })
    }

    fun filter(text: String?) {
        temp.clear()
        for (d in userList) {
            if (d != null) {
                if ((d.name!!.toLowerCase()).contains(text.toString())) {
                    temp.add(d)
                }
            }
        }
        //update recyclerview
        groupNewMemberListAdapter.updateItems(temp)
    }

    private fun retrieveChatList() {

        ProgressDialog.showProgressBar(this)
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val query = FirebaseDatabase.getInstance().getReference("users")

        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val companyId = userSnapshot.child("company_id").getValue(Any::class.java)
                        val user: ContactModel? =
                            userSnapshot.getValue(ContactModel::class.java)
                        if(uid != user?.userId) {
                            if(PrefManager.read(PrefManager.COMPANYID, "")=="" ||
                                PrefManager.read(PrefManager.COMPANYID, "") == companyId) {
                                userList.add(user)
                            }
                        }
                        /*if (uid != user?.userId) {
                            userList.add(user)
                        }*/
                        Log.d(TAG, "onDataChange: " + userList.count())
                        groupNewMemberListAdapter.updateItems(userList)
                        ProgressDialog.hideProgressBar()
                    }
                } else {
                    Log.d(TAG, "onDataChange: no data found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    override fun onResume() {
        super.onResume()
        retrieveChatList()
    }

    override fun initializerControl() {
        //TODO("Not yet implemented")
    }

    override fun onQuantitychanged(userlist: ArrayList<ContactModel>) {
        selectedUserList.clear()
        selectedUserList.addAll(userlist.toList())
    }
}