package com.kelme.activity.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.kelme.R
import com.kelme.adapter.ChatSingleUserListAdapter
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivitySearchUserBinding
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.ContactModel
import com.kelme.utils.Constants
import com.kelme.utils.PrefManager
import com.kelme.utils.ProgressDialog
import java.util.Locale
import java.util.Locale.getDefault

class SearchUserActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchUserBinding
    private lateinit var viewModal: ContactModel
    val temp: ArrayList<ContactModel?> = ArrayList()
    private lateinit var conversationReference: DatabaseReference
    private lateinit var chatSingleUserListAdapter: ChatSingleUserListAdapter
    var userList: ArrayList<ContactModel?> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_search_user)
        ViewCompat.setOnApplyWindowInsetsListener(binding.clTop) { v, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            // extension from androidx.core.view: updatePadding
            v.updatePadding(top = statusBarTop)
            insets
        }
        setUi()
    }

    override fun initializerControl() {}

    private fun setUi() {

        binding.rvChat.setHasFixedSize(true)
        binding.rvChat.layoutManager = GridLayoutManager(
            applicationContext,
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

        binding.backArrow.setOnClickListener { onBackPressed() }

        chatSingleUserListAdapter = ChatSingleUserListAdapter(applicationContext, userList)

        binding.rvChat.adapter = chatSingleUserListAdapter

        chatSingleUserListAdapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                PrefManager.write(PrefManager.IS_CALL_FROM_CHAT_LIST_FRAG, Constants.ActivityType.NO)
                val intent = Intent(this@SearchUserActivity, ChatConversationActivity::class.java)
                val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
                if(temp.size>0) {
                    intent.putExtra(Constants.SINGLE_CHAT_MODEL, temp[position])
                    intent.putExtra("otherUserId",temp[position]?.userId)
                }else{
                    intent.putExtra(Constants.SINGLE_CHAT_MODEL, userList[position])
                    intent.putExtra("otherUserId",userList[position]?.userId)
                }
                startActivity(intent)
                finish()
            }
        })
    }

    private fun searchChatId(uid: String, userId: String) {
        conversationReference = FirebaseDatabase.getInstance().getReference("conversations")
        val query = conversationReference.orderByChild(uid).equalTo(userId)
        ProgressDialog.showProgressBar(baseContext)
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val chatId = userSnapshot.key.toString()
                        PrefManager.write(PrefManager.FCM_CHAT_ID, chatId)
                    }
                } else {
                    PrefManager.write(PrefManager.CHAT_ID_NOT_FOUND, Constants.ActivityType.YES)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    fun filter(text: String?) {
        temp.clear()
        for (d in userList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d != null) {
                try {
                    if(d.name!="") {
                        if ((d.name!!.lowercase(getDefault())).contains(text.toString())) {
                            temp.add(d)
                        }
                    }
                } catch (e: Exception) { }
            }
        }
        //update recyclerview
        chatSingleUserListAdapter.updateItems(temp)
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
                        val user = ContactModel()
                        val deviceToken = userSnapshot.child("deviceToken").getValue(String::class.java)
                        val deviceType = userSnapshot.child("deviceType").getValue(String::class.java)
                        val email = userSnapshot.child("email").getValue(String::class.java)
                        val isDeleted = userSnapshot.child("isDeleted").getValue(Boolean::class.java)
                        val isNotificationOn = userSnapshot.child("isNotificationOn").getValue(Boolean::class.java)
                        val isOnline = userSnapshot.child("isOnline").getValue(Boolean::class.java)
                        val lastSeen = userSnapshot.child("lastSeen").getValue(Long::class.java)
                        val name = userSnapshot.child("name").getValue(String::class.java)
                        val profilePicture = userSnapshot.child("profilePicture").getValue(String::class.java)
                        val userId = userSnapshot.child("userId").getValue(String::class.java)
                        val userRole = userSnapshot.child("userRole").getValue(Long::class.java)
                        val companyId = userSnapshot.child("company_id").getValue(Any::class.java)
                        user.deviceToken = deviceToken
                        user.deviceType = deviceType
                        user.email = email
                        user.isDeleted = isDeleted
                        user.isNotificationOn = isNotificationOn
                        user.isOnline = isOnline
                        user.lastSeen = lastSeen
                        user.name = name
                        user.profilePicture = profilePicture
                        user.userId = userId
                        user.userRole = userRole
                        if(uid != user.userId) {
                            if(PrefManager.read(PrefManager.COMPANYID, "") == companyId) {
                                Log.d(TAG, "onDataChange: ${PrefManager.read(PrefManager.COMPANYID, "")} ${companyId} "+userList.count())
                                userList.add(user)
                            }
                        }
                        chatSingleUserListAdapter.updateItems(userList)
                        ProgressDialog.hideProgressBar()
                    }
                } else{
                    Log.d(TAG, "onDataChange: no data found")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onResume() {
        super.onResume()
        if(PrefManager.read(PrefManager.USERROLE, "") != "country manager") {
            retrieveChatList()
        }
    }
}