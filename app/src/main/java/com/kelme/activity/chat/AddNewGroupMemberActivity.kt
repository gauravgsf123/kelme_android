package com.kelme.activity.chat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.kelme.R
import com.kelme.adapter.GroupNewMemberListAdapter
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityAddNewGroupMemberBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.interfaces.QuantityListner
import com.kelme.model.ChatMembersDetails
import com.kelme.model.ContactModel
import com.kelme.utils.PrefManager
import com.kelme.utils.ProgressDialog
import java.util.Locale
import java.util.Locale.getDefault

class AddNewGroupMemberActivity : BaseActivity(), QuantityListner {

    lateinit var binding:ActivityAddNewGroupMemberBinding
    val temp: ArrayList<ContactModel?> = ArrayList()
    var selectedUserList: ArrayList<ContactModel?> = ArrayList()
    var membersInGroup = 0
    private lateinit var groupNewMemberListAdapter: GroupNewMemberListAdapter
    var userList: ArrayList<ContactModel?> = ArrayList()
    val chatMembers: HashMap<String, Boolean> = HashMap()
    val lastUpdates: HashMap<String, Long> = HashMap()
    val memberDetails: HashMap<String, ChatMembersDetails> = HashMap()
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_add_new_group_member)
        membersInGroup = intent.getIntExtra(getString(R.string.member_count),0)
        setUi()
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
            ProgressDialog.showProgressBar(this)
            val selectedUser = selectedUserList.size
            val totalMembers = membersInGroup + selectedUser
            if(totalMembers > 15){
                // Toast.makeText(this, "You can add maximum 15 members", Toast.LENGTH_SHORT).show()
            }else{
                lastUpdates.clear()
                memberDetails.clear()
                chatMembers.clear()
                for (s in selectedUserList) {
                    if(s!!.isSelected) {
                        s.userId.let { lastUpdates.put(it!!, 0) }
                        s.userId.let { chatMembers.put(it!!, true) }
                 //   s.userId.let { lastMessages.put(it, " ") }
                        s.userId.let { memberDetails.put(it!!, ChatMembersDetails(0, it, 0, 0)) }
                        val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "0")
                        databaseReference = FirebaseDatabase.getInstance().getReference("conversations").child(chatId)
                        databaseReference.child("chatMembers").updateChildren(chatMembers as Map<String, Any>)
                        databaseReference.child("chatMembersDetails").updateChildren(memberDetails as Map<String, Any>)
                        databaseReference.child("lastUpdates").updateChildren(lastUpdates as Map<String, Any>)
                    }
                }
                val intent = Intent(this, GroupDetailsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtra("Activity","AddNewGroupMember")
                intent.putExtra("chatMembers", chatMembers)
                intent.putExtra("memberDetails", memberDetails)
                intent.putExtra("lastUpdates", lastUpdates)
                startActivity(intent)
            }
        }

        binding.etChatSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })

        binding.backArrow.setOnClickListener { onBackPressed() }

        groupNewMemberListAdapter = GroupNewMemberListAdapter(applicationContext, userList,this)

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
                if ((d.name!!.lowercase(getDefault())).contains(text.toString())) {
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
                        Log.d(TAG, "onDataChange: "+userList.count())
                        groupNewMemberListAdapter.updateItems(userList)
                        ProgressDialog.hideProgressBar()
                    }
                }else{
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