package com.kelme.fragment.chat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.kelme.R
import com.kelme.activity.chat.ChatConversationActivity
import com.kelme.activity.chat.SearchUserActivity
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.ChatListAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentChatListBinding
import com.kelme.event.CreateChatGroupEvent
import com.kelme.event.SearchChatUserEvent
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.response.ChatListModelWithName
import com.kelme.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatListFragment : BaseFragment() {

    private lateinit var binding: FragmentChatListBinding
    private lateinit var viewModal: ChatViewModal

    private val instance = FirebaseDatabase.getInstance()

    var userList: ArrayList<ChatListModelWithName?> = ArrayList()
    var userListClone: ArrayList<String> = ArrayList()

    private lateinit var adapter: ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_list, container, false)
        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(ChatViewModal::class.java)

        setUI()
        setObserver()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveChatList()
        //PrefManager.write(PrefManager.UNSEENMSGCOUNT,"0")
    }

    private fun retrieveChatList() {
        binding.tvNoChat.visibility = View.GONE
        ProgressDialog.showProgressBar(requireContext())
        ProgressDialog.setCancelable()
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val query = instance.getReference("conversations").orderByChild("chatMembers/${uid}").equalTo(true)
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userListClone.clear()
                userList.clear()
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        collectUserListData(userSnapshot)
                    }
                    if (userList.size > 0) {
                        userList.forEachIndexed { user, chatListModelWithName ->
                            searchUserName(user, uid, chatListModelWithName)
                           // userListDisplay.add(userList[user])
                        }

                        for (userTime in userList) {
                            var unSeenMsgCount = 0
                            val dbLastUpdates = FirebaseDatabase.getInstance().getReference("conversations")
                            val queryLastUpdates = dbLastUpdates.child(userTime!!.chatId).child("lastUpdates").child(uid)
                            queryLastUpdates.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val lastUpdate = dataSnapshot.getValue(Long::class.java)
                                    val db = FirebaseDatabase.getInstance().reference.child("chatMessages")
                                            .child(userTime.chatId)
                                    val queryUnseenMsg = db.orderByKey().limitToLast(30)
                                    queryUnseenMsg.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            for (ds in dataSnapshot.children) {
                                                val time = ds.child("timestamp").getValue(Long::class.java)
                                                userTime.lastUpdateTime = time
                                                if (lastUpdate != null && time !=null) {
                                                        if (time.toLong() > lastUpdate.toLong()) {
                                                            unSeenMsgCount += 1
                                                           // Log.d("time_stamp","$unSeenMsgCount ${getShortDate(time.toLong())} ${getShortDate(lastUpdate.toLong())}")
                                                        }
                                                }
                                            }
                                            userTime.unSeenMsg = unSeenMsgCount
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                        }
                                    })
                                }
                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                        }

                        val handler = Handler()
                        handler.postDelayed({
                            userList.sortWith(compareByDescending { it?.lastUpdate })
                            adapter.updateItems(userList)
                            val gson = Gson()
                            val json = gson.toJson(userList)
                            Log.d("json_data",json)
                            goToChatPage()
                        }, 1500)
                        ProgressDialog.hideProgressBar()

                        if (userList.size > 0) {
                            ProgressDialog.hideProgressBar()
                            binding.tvNoChat.visibility = View.GONE
                        } else {
                            ProgressDialog.hideProgressBar()
                            binding.tvNoChat.visibility = View.VISIBLE
                        }

                    } else {
                        ProgressDialog.hideProgressBar()
                        binding.tvNoChat.visibility = View.VISIBLE
                    }
                } else {
                    ProgressDialog.hideProgressBar()
                    binding.tvNoChat.visibility = View.VISIBLE
                }
            }



            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addValueEventListener(valueEventListener)
    }

    fun getShortDate(ts:Long?):String{
        if(ts == null) return ""
        //Get instance of calendar
        val calendar = Calendar.getInstance(Locale.getDefault())
        //get current date from ts
        calendar.timeInMillis = ts
        //return formatted date
        return android.text.format.DateFormat.format("dd/MM/yy hh:mm a", calendar).toString()
    }

    private fun collectUserListData(userSnapshot: DataSnapshot) {
        val temp = ChatListModelWithName()
        val chatId = userSnapshot.child("chatId").getValue(String::class.java)
        val chatLastMessage = userSnapshot.child("chatLastMessage").getValue(String::class.java)
        val chatPic = userSnapshot.child("chatPic").getValue(String::class.java)
        val chatTitle = userSnapshot.child("chatTitle").getValue(String::class.java)
        val chatType = userSnapshot.child("chatType").getValue(String::class.java)
        val created = userSnapshot.child("created").getValue(Long::class.java)
        val createrId = userSnapshot.child("createrId").getValue(String::class.java)
        val lastUpdate = userSnapshot.child("lastUpdate").getValue(Long::class.java)
//        PrefManager.write("lastUpdate", lastUpdate!!)
        val members = userSnapshot.child("chatMembers")
        val chatMembers: HashMap<String, Boolean> = HashMap()
        for (s in members.children) {
            chatMembers[s.key!!] = s.value!! as Boolean
        }
        temp.chatId = chatId.toString()
        Log.d(TAG, "collectUserListData: "+chatId.toString())
        temp.chatPic = chatPic.toString()
        temp.chatLastMessage = chatLastMessage.toString()
        temp.chatTitle = chatTitle.toString()
        temp.chatType = chatType.toString()
        temp.created = created
        temp.createrId = createrId.toString()
        temp.lastUpdate = lastUpdate
        temp.chatMembers = chatMembers
        userList.add(temp)
    }

    private fun searchUserName(
        user: Int, uid: String,
        chatListModelWithName: ChatListModelWithName?
    ) {
        if (userList[user]?.chatType == "single") {
            val filteredKeysMap = userList[user]?.chatMembers?.filterKeys { it != uid }
            val otherUser = filteredKeysMap?.keys.toString().replace(Regex("[\\[\\],]"), "")
            val queryUser = instance.getReference("users").child(otherUser)
            userListClone.add(otherUser)
            val valueEventListenerUser: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val name = dataSnapshot.child("name").getValue(String::class.java)
                        val profileImage =
                            dataSnapshot.child("profilePicture").getValue(String::class.java)

                        chatListModelWithName?.name = name
                        chatListModelWithName?.chatPic = profileImage

                    } else {
                        binding.tvNoChat.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            }
            queryUser.addListenerForSingleValueEvent(valueEventListenerUser)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        (activity as DashboardActivity?)?.run {
            setTitle("Chat")
            showNotificationIcon()
            showBackArrow()
            showSearchBar()
            showBottom()
            showAddChatGroupIcon()
            changeSearchBarBackground()
            showOrHideCount()
        }
    }

    private fun setUI() {
        binding.rvChat.setHasFixedSize(true)
        binding.rvChat.layoutManager = GridLayoutManager(
            requireContext(),
            1,
+            RecyclerView.VERTICAL,
            false,
        )
        setAdapterMethod()
    }

    private fun goToChatPage() {
        if(PrefManager.read(PrefManager.FROMNOTIFICATION, "0") == "2") {
            PrefManager.write(PrefManager.FROMNOTIFICATION, "0")
            val chatId =  PrefManager.read(PrefManager.FROMNOTIFICATIONCHATID,"")
            if(chatId!="") {
                val position =userList.indexOfFirst { it?.chatId == chatId }
                if(position>=0) {
                    getUserName(position)
                    PrefManager.write(
                        PrefManager.IS_CALL_FROM_CHAT_LIST_FRAG,
                        Constants.ActivityType.YES
                    )
                    val user = Constants.userName.replace(Regex("[\\[\\],]"), "")
                    val intent = Intent(context, ChatConversationActivity::class.java)
                    intent.putExtra(Constants.CHATLIST_MODEL, userList[position])
                    intent.putExtra("otherUserId", user)
                    startActivity(intent)
                }
            }
        }

    }

    private fun setAdapterMethod() {
        adapter = ChatListAdapter(requireContext(), userList)
        binding.rvChat.adapter = adapter
        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                getUserName(position)
                PrefManager.write(
                    PrefManager.IS_CALL_FROM_CHAT_LIST_FRAG,
                    Constants.ActivityType.YES
                )
                val data = PrefManager.readUnreadData(PrefManager.MESSAGECOUNT)
                data?.remove(userList[position]?.chatId)
                data?.let { PrefManager.writeUnReadData(PrefManager.MESSAGECOUNT, it) }
                (activity as DashboardActivity?)?.showOrHideCount()
                val user=Constants.userName.replace(Regex("[\\[\\],]"), "")
                val intent = Intent(context, ChatConversationActivity::class.java)
                intent.putExtra(Constants.CHATLIST_MODEL, userList[position])
                intent.putExtra("otherUserId",user)
                startActivity(intent)
            }
        })
    }

    private fun getUserName(position: Int) {
        if (userList[position]?.chatType == "single") {
            val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
            val filteredKeysMap =
                userList[position]!!.chatMembers.filterKeys { it != uid }
            Constants.userName = filteredKeysMap.keys.toString()
        } else {
            Constants.userName = userList[position]?.chatTitle.toString()
        }
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
                    if (response.message == "240") {
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
                else -> {}
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as DashboardActivity?)?.run {
            setTitle("Chats")
            hideAddChatGroupIcon()
            resetSearchBarBackground()
        }
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CreateChatGroupEvent?) {
        createChatGroup()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SearchChatUserEvent) {
        searchUser(event.str)
    }

    private fun searchUser(str: String) {
        startActivity(Intent(context, SearchUserActivity::class.java))
    }

    private fun createChatGroup() {
        (activity as DashboardActivity).replaceFragment(CreateChatGroupFragment(), Bundle.EMPTY)
    }
}