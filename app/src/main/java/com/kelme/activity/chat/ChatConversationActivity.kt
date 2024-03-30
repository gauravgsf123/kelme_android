package com.kelme.activity.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kelme.R
import com.kelme.adapter.ChatAdapter
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityChatConversionBinding
import com.kelme.fragment.profile.BottomSheetOptionForDocumentUploadFragment
import com.kelme.interfaces.MediaInterface
import com.kelme.model.ChatListModelNewUser
import com.kelme.model.ChatMembersDetails
import com.kelme.model.ContactModel
import com.kelme.model.response.ChatListModelWithName
import com.kelme.model.response.ChatModel
import com.kelme.utils.*
import java.io.File
import java.text.SimpleDateFormat

class ChatConversationActivity : BaseActivity(), View.OnClickListener, MediaInterface {

    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference
    private val FILE_NAME = "photo.jpg"
    private var chatListModel: ChatListModelWithName?=null
    private lateinit var contactModel: ContactModel
    private lateinit var adapter: ChatAdapter
    private lateinit var binding: ActivityChatConversionBinding
    private lateinit var conversationReference: DatabaseReference
    private lateinit var chatMessagesReference: DatabaseReference
    private lateinit var dbRef: DatabaseReference
    var msgList: ArrayList<ChatModel?> = ArrayList()
    private var chatModelFirst: ChatModel = ChatModel()
    private var chatModel: ChatModel = ChatModel()
    var msgListWithDate: ArrayList<ChatModel?> = ArrayList()
    private var uid = ""
    private var chatType = ""
    private var userName = ""
    private var groupName = ""
    private var chatId = ""
    private var userDP = ""
    private var isMemberLeave = 0L
    private val REQUEST_CODE_CAMERA = 200
    private val REQUEST_CODE_MEDIA = 100
    private val REQUEST_CODE_DOC = 300
    private val REQUEST_CODE_GROUP = 400
    var sendMsg = ""
    private var imageFilePath: String = ""
    private lateinit var filePhoto: File
    var count = 0
    private lateinit var lastMsg: String
    var userIdDetails:String=""
    val chatMembers: HashMap<String, Any> = HashMap()
    var userList: ArrayList<ChatListModelWithName?> = ArrayList()

    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_conversion)
        conversationReference = FirebaseDatabase.getInstance().getReference("conversations")
        chatMessagesReference = FirebaseDatabase.getInstance().getReference("chatMessages")
        dbRef = FirebaseDatabase.getInstance().reference

        if (PrefManager.read(
                PrefManager.IS_CALL_FROM_CHAT_LIST_FRAG,
                Constants.ActivityType.YES
            ) == Constants.ActivityType.YES
        ) {
            getDataIntentFromChatList()
        } else if(PrefManager.read(PrefManager.IS_CALL_FROM_CHAT_LIST_FRAG,
        Constants.ActivityType.YES
        ) == Constants.ActivityType.NO){
            getDataIntentFromSearchActivity()
        }else{
            getDataIntentFromContactList()
        }
        setUI()
        clickListner()

        searchGroupTags()
    }

    private fun searchGroupTags() {

        val queryUser = FirebaseDatabase.getInstance().getReference("users")
        val valueEventListenerUser: ValueEventListener =
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val temp = ChatListModelWithName()
                        for (userSnapshot in dataSnapshot.children) {
                            val name = dataSnapshot.child("name").getValue(String::class.java)
                            val userId = dataSnapshot.child("userId").getValue(String::class.java)
                            temp.name = name
                            temp.chatId = userId.toString()
                            userList.add(temp)
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            }
        queryUser.addListenerForSingleValueEvent(valueEventListenerUser)
    }

    private fun getDataIntentFromContactList(){
        ProgressDialog.showProgressBar(this)
        ProgressDialog.setCancelable()
        userIdDetails = intent.getStringExtra("otherUserId")!!
        val name = intent.getStringExtra("name")!!
        val image = intent.getStringExtra("image")!!
        val userid = intent.getStringExtra("userid")!!
        chatType = Constants.ChatType.SINGLE
        userDP = image
        userName = name
        uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        searchChatId(uid, userid)
    }

    private fun getDataIntentFromSearchActivity() {
        ProgressDialog.showProgressBar(this)
        ProgressDialog.setCancelable()
        userIdDetails = try {
            intent.getStringExtra("otherUserId")!!
        } catch (e: Exception) {
            ""
        }
        contactModel = intent.getParcelableExtra(Constants.SINGLE_CHAT_MODEL)!!
        chatType = Constants.ChatType.SINGLE
        userDP = contactModel.profilePicture!!
        userName = contactModel.name.toString()
        uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        searchChatId(uid, contactModel.userId!!)
    }

    private fun searchChatId(uid: String, userId: String) {
        val query = conversationReference.orderByChild(uid).equalTo(userId)
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        chatId = userSnapshot.key.toString()
                        PrefManager.write(PrefManager.FCM_CHAT_ID, chatId)
                        PrefManager.write(PrefManager.SEARCH_CHAT_ID, chatId)
                    }
                    retrieveData()
                    PrefManager.write(PrefManager.CHAT_ID_NOT_FOUND, Constants.ActivityType.NO)
                } else {
                    ProgressDialog.hideProgressBar()
                    PrefManager.write(PrefManager.CHAT_ID_NOT_FOUND, Constants.ActivityType.YES)
                    PrefManager.write(PrefManager.SEARCH_CHAT_ID, "")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun capturePhoto() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        filePhoto = getPhotoFile(FILE_NAME)
        val providerFile =
            FileProvider.getUriForFile(this, "" +
                    "com.kelme.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)

        if (takePhotoIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_CODE_CAMERA)
        } else {
          //  Toast.makeText(this, "Camera could not open", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        conversationReference.child(chatId).child("lastUpdates").child(uid)
            .setValue(
                System.currentTimeMillis()
            ) { _, _ -> Log.e(">>>>> onBackPressed ", ">>>>> onComplete") }
    }

    private fun createNewNodeAndAddMsgData(uniqueID: String?) {
        val (uid, addMsg) = collectDataForNewCreatedUser(uniqueID)
        uniqueID?.let {
            conversationReference.child(uniqueID).setValue(addMsg).addOnCompleteListener {
                if (it.isSuccessful) {
                    chatId = uniqueID
                    PrefManager.write(PrefManager.FCM_CHAT_ID, chatId)
                    val txtMsg = sendMsg
                    val (uniqueID, addMsg) = collectMsgData("text", " ", txtMsg)
                    addMsgData(uniqueID, addMsg)
                }
            }
        }
        val map = HashMap<String, Any>()
        map.put("conversations/${uniqueID}/${uid}", contactModel.userId!!)
        map.put("conversations/${uniqueID}/${contactModel.userId}", uid)
        dbRef.updateChildren(map){ error, ref ->
            if (error != null)
                Log.e(">>>>> Inserted ", "Fail")
            else
                Log.e(">>>>> Inserted ", "Success")
        }
    }

    private fun collectDataForNewCreatedUser(uniqueID: String?): Pair<String, ChatListModelNewUser> {
        uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val chatMembersDetails: ArrayList<ChatMembersDetails> = addDataToArraylist(uid)
        val (lastUpdates: HashMap<String, Long>, chatMembers: HashMap<String, Boolean>) = addDataToHashMap(
            uid
        )
        val timestamp = System.currentTimeMillis()
        val addMsg = ChatListModelNewUser(
            uniqueID, " ", " ", chatMembers, " ", "single",
            timestamp, uid, 0, lastUpdates, chatMembersDetails
        )

        //VOIP
        chatListModel = ChatListModelWithName(chatId= uniqueID!!, chatPic = userDP,chatMembers=chatMembers)
        return Pair(uid, addMsg)
    }

    private fun addDataToArraylist(uid: String): ArrayList<ChatMembersDetails> {
        val ChatMembersDetails: ArrayList<ChatMembersDetails> = ArrayList()
        val obj1 = ChatMembersDetails()
        val obj2 = ChatMembersDetails()
        obj1.memberLeave = 0
        obj1.memberDelete = 0
        obj1.memberId = uid
        obj1.memberJoin = 0
        obj2.memberLeave = 0
        obj2.memberDelete = 0
        obj2.memberId = contactModel.userId
        obj2.memberJoin = 0
        ChatMembersDetails.add(obj1)
        ChatMembersDetails.add(obj2)
        return ChatMembersDetails
    }

    private fun addDataToHashMap(uid: String): Pair<HashMap<String, Long>, HashMap<String, Boolean>> {
        val lastUpdates: HashMap<String, Long> = HashMap()
        lastUpdates[uid] = 0
        contactModel.userId.let { lastUpdates.put(it!!, 0) }
        val chatMembers: HashMap<String, Boolean> = HashMap()
        chatMembers[uid] = true
        contactModel.userId.let { chatMembers.put(it!!, true) }
        return Pair(lastUpdates, chatMembers)
    }

    private fun getDataIntentFromChatList() {
        PrefManager.write(PrefManager.CHAT_ID_NOT_FOUND, Constants.ActivityType.NO)

        chatListModel = intent.getParcelableExtra(Constants.CHATLIST_MODEL)!!
        userIdDetails = intent.getStringExtra("otherUserId")!!
        uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        chatId = chatListModel?.chatId!!
        userDP = chatListModel?.chatPic.toString()
        chatType = chatListModel?.chatType.toString()
        userName = chatListModel?.name.toString()
        groupName = chatListModel?.chatTitle.toString()
        PrefManager.write(PrefManager.FCM_CHAT_ID, chatId)
      //  PrefManager.write(PrefManager.FCM_GROUP_NAME, groupName)
        Log.d(TAG, "getDataIntentFromChatList: ChatId $chatId")

        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val uniqueID = PrefManager.read(PrefManager.CHAT_UNIQUE_ID, "")
        uniqueID.let {
            conversationReference.child(chatId).child("lastUpdates").child(uid)
                .setValue(System.currentTimeMillis())
        }
    }

    private fun retrieveData() {
        val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "chatId")
      //  ProgressDialog.showProgressBar(this)
        val query = chatMessagesReference.child(chatId)
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            @SuppressLint("SimpleDateFormat")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                msgList.clear()
                msgListWithDate.clear()
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val msg: ChatModel? = userSnapshot.getValue(ChatModel::class.java)
                        msgList.add(msg)
                        Log.d(TAG, "onDataChange: msgList$msgList")
                    }
                    Log.d(TAG, "onDataChange_msg: msgList" + msgList.size)
                    val date = msgList[0]!!.timestamp
                    val formatter = SimpleDateFormat("dd/MM/yyyy")
                    chatModelFirst.message = "" + formatter.format(date).toString()
                    chatModelFirst.type = "date"
                    msgListWithDate.add(0, chatModelFirst)
                    var previousStamp: Long?
                    msgList.forEachIndexed { index, user ->
                        val currentStamp = user!!.timestamp
                        previousStamp = if (index > 0) {
                            msgList[index - 1]!!.timestamp
                        } else {
                            msgList[0]!!.timestamp
                        }
                        val currentDate = "" + formatter.format(currentStamp).toString()
                        val previousDate = "" + formatter.format(previousStamp).toString()

                        if (currentDate == previousDate) {
                            msgListWithDate.add(user)
                        } else {
                            val dateUser = user.timestamp
                            chatModel.message = "" + formatter.format(dateUser).toString()
                            chatModel.type = "date"
                            msgListWithDate.add(chatModel)
                            msgListWithDate.add(user)
                        }
                    }
                        adapter.updateItems(msgListWithDate)
                        binding.rvChat.scrollToPosition(adapter.itemCount - 1)
                        ProgressDialog.hideProgressBar()
                  //  }, 1500)

                } else {
                    Log.d(TAG, "onDataChange: no data found hereeeeeeeeee")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addValueEventListener(valueEventListener)
    }

    override fun initializerControl() {
        TODO("Not yet implemented")
    }

    private fun setUI() {

        if (chatType == Constants.ChatType.SINGLE) {
            binding.myToolbar.tvMessageSnippet.visibility = View.GONE
            binding.myToolbar.tvName.text = userName
        } else {
            binding.myToolbar.tvMessageSnippet.visibility = View.VISIBLE
            binding.myToolbar.tvName.text = groupName
        }

        binding.myToolbar.tvName.setOnClickListener {
            if (chatType == Constants.ChatType.GROUP) {
                val intent = Intent(this@ChatConversationActivity, GroupDetailsActivity::class.java)
                intent.putExtra("GroupName", groupName)
                intent.putExtra("GroupProfile", userDP)
                startActivityForResult(intent,REQUEST_CODE_GROUP)
            }else{
                val intent = Intent(this,UserDetailsActivity::class.java)
                intent.putExtra("userId",userIdDetails)
                startActivity(intent)
            }
        }

        userDP.let { Utils.loadImage(baseContext, binding.myToolbar.civProfileImage, it) }

        binding.rvChat.setHasFixedSize(true)
        binding.rvChat.layoutManager = GridLayoutManager(
            baseContext,
            1,
            RecyclerView.VERTICAL,
            false
        )
        setChatAdapter()
    }

    private fun setChatAdapter() {
        adapter = ChatAdapter(baseContext, msgListWithDate)
        binding.rvChat.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (PrefManager.read(
                PrefManager.IS_CALL_FROM_CHAT_LIST_FRAG,
                Constants.ActivityType.YES
            ) == Constants.ActivityType.YES
        ) retrieveData()
//        if (chatType == Constants.ChatType.GROUP) {
//            updateMembersInfoInGroup()
//            binding.myToolbar.ivVideo.visibility=View.GONE
//        }else{
//            binding.myToolbar.ivVideo.visibility=View.VISIBLE
//        }
    }

    private fun updateMembersInfoInGroup() {
        ProgressDialog.showProgressBar(this)
        val query = conversationReference.child(chatId)
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val createrId = dataSnapshot.child("createrId").value.toString()
                    PrefManager.write(PrefManager.CREATER_ID, createrId)

                    if (chatType != Constants.ChatType.SINGLE) {
                        val members = dataSnapshot.child("chatMembers")
                        isMemberLeave = dataSnapshot.child("chatMembersDetails").child(uid)
                            .child("memberLeave").getValue(Long::class.java)!!
                        PrefManager.write(PrefManager.IS_MEMBER_LEAVE, isMemberLeave)
                        if (isMemberLeave > 0) {
                            binding.layoutChatBottomNoMember.root.visibility = View.VISIBLE
                            binding.layoutChatBottom.root.visibility = View.INVISIBLE
                        } else {
                            binding.layoutChatBottomNoMember.root.visibility = View.INVISIBLE
                            binding.layoutChatBottom.root.visibility = View.VISIBLE
                        }
                        for (s in members.children) {
                            Log.d(TAG, "onDataChange: " + dataSnapshot.value.toString())
                            chatMembers[s.key!!] = s.value!!
                        }
                        binding.myToolbar.tvMessageSnippet.text =
                            chatMembers.size.toString() + " Members"
                    }
                } else {
                    Log.d(TAG, "onDataChange: no data found")
                }
                ProgressDialog.hideProgressBar()
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addValueEventListener(valueEventListener)
    }

    private fun clickListner() {
        binding.myToolbar.backArrow.setOnClickListener(this)
        binding.myToolbar.ivPhone.setOnClickListener(this)
        binding.myToolbar.ivVideo.setOnClickListener(this)
        binding.layoutChatBottom.ivAttachment.setOnClickListener(this)
        binding.layoutChatBottom.ivSend.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.myToolbar.backArrow.id -> onBackPressed()
            binding.myToolbar.ivPhone.id -> {
                if (!PermissionUtil.hasPermissions(this, *PERMISSIONS)) requestPermission(this)
                else {
                    if (chatListModel != null && chatListModel?.chatId!!.isNotEmpty()) {
                        val intent = Intent(this, CommonCallActivity::class.java)
                        intent.putExtra(Constants.CALL_TYPE_AV, Constants.CallType.AUDIO)
                        intent.putExtra(Constants.CALLER_TYPE, Constants.CallerType.CALLER)
                        intent.putExtra(Constants.CHATLIST_MODEL, chatListModel)
                        intent.putExtra(Constants.USER_NAME, userName)
                        startActivity(intent)

                        // Toast.makeText(this, "if....", Toast.LENGTH_SHORT).show()
                    } else {
                        var chatListModel: ChatListModelWithName
                        val intent = Intent(this, CommonCallActivity::class.java)
                        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
                        val chatMembers: HashMap<String, Boolean> = HashMap()
                        chatMembers[uid] = true
                        chatMembers[userIdDetails] = true
                        intent.putExtra(Constants.CALL_TYPE_AV, Constants.CallType.AUDIO)
                        intent.putExtra(Constants.CALLER_TYPE, Constants.CallerType.CALLER)
                        intent.putExtra(Constants.USER_NAME, userName)
                        if (PrefManager.read(PrefManager.SEARCH_CHAT_ID, "") == "") {
                            val uniqueID = conversationReference.push().key
                            chatListModel = ChatListModelWithName(
                                chatId = uniqueID!!,
                                chatMembers = chatMembers
                            )
                            intent.putExtra(Constants.CHATLIST_MODEL, chatListModel)
                            //  Toast.makeText(this, "At least send a single msg", Toast.LENGTH_SHORT).show()
                        } else {
                            val uniqueID = PrefManager.read(PrefManager.SEARCH_CHAT_ID, "")
                            chatListModel =
                                ChatListModelWithName(chatId = uniqueID, chatMembers = chatMembers)
                            intent.putExtra(Constants.CHATLIST_MODEL, chatListModel)
                            /*Toast.makeText(
                                this,
                                "" + PrefManager.read(PrefManager.SEARCH_CHAT_ID, ""),
                                Toast.LENGTH_SHORT
                            ).show()*/
                        }
                        startActivity(intent)
                    }
                }
            }

            binding.myToolbar.ivVideo.id -> {
                if (!PermissionUtil.hasPermissions(this, *PERMISSIONS)) requestPermission(this)
                else {
                    if (chatListModel != null && chatListModel?.chatId!!.isNotEmpty()) {
                        val intent = Intent(this, CommonCallActivity::class.java)
                        intent.putExtra(Constants.CALL_TYPE_AV, Constants.CallType.VIDEO)
                        intent.putExtra(Constants.CALLER_TYPE, Constants.CallerType.CALLER)
                        intent.putExtra(Constants.CHATLIST_MODEL, chatListModel)
                        intent.putExtra(Constants.USER_NAME, userName)
                        startActivity(intent)
                    } else {
                        //   Toast.makeText(this, "At least send a single msg", Toast.LENGTH_SHORT).show()
                        var chatListModel: ChatListModelWithName
                        val intent = Intent(this, CommonCallActivity::class.java)
                        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
                        val chatMembers: HashMap<String, Boolean> = HashMap()
                        chatMembers[uid] = true
                        chatMembers[userIdDetails] = true
                        intent.putExtra(Constants.CALL_TYPE_AV, Constants.CallType.VIDEO)
                        intent.putExtra(Constants.CALLER_TYPE, Constants.CallerType.CALLER)
                        intent.putExtra(Constants.USER_NAME, userName)
                        if (PrefManager.read(PrefManager.SEARCH_CHAT_ID, "") == "") {
                            val uniqueID = conversationReference.push().key
                            chatListModel = ChatListModelWithName(
                                chatId = uniqueID!!,
                                chatMembers = chatMembers
                            )
                            intent.putExtra(Constants.CHATLIST_MODEL, chatListModel)
                            //  Toast.makeText(this, "At least send a single msg", Toast.LENGTH_SHORT).show()
                        } else {
                            val uniqueID = PrefManager.read(PrefManager.SEARCH_CHAT_ID, "")
                            chatListModel =
                                ChatListModelWithName(chatId = uniqueID, chatMembers = chatMembers)
                            intent.putExtra(Constants.CHATLIST_MODEL, chatListModel)
                            Toast.makeText(
                                this,
                                "" + PrefManager.read(PrefManager.SEARCH_CHAT_ID, ""),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        startActivity(intent)
                    }
                }
            }

            binding.layoutChatBottom.ivAttachment.id -> BottomSheetOptionForDocumentUploadFragment()
                .apply {
                    show(supportFragmentManager, "BottomSheetOptionForDocumentUploadFragment")
                }

            binding.layoutChatBottom.ivSend.id -> {
                sendMsg = binding.layoutChatBottom.etMessage.text.toString()
                binding.layoutChatBottom.etMessage.setText("")
                if (sendMsg.isNotBlank()) {
                    if (PrefManager.read(PrefManager.CHAT_ID_NOT_FOUND, "") == "NO") {  //found the user firebase id and its existing user in chatlist
                        val (uniqueID, addMsg) = collectMsgData("text", "uri", sendMsg)
                        addMsgData(uniqueID, addMsg)
                        Log.d("insendclick", "onClick:NO")
                    } else {
                        PrefManager.write(PrefManager.CHAT_ID_NOT_FOUND, Constants.ActivityType.NO)  //new user from search
                        Log.d("insendclick", "onClick:Yes")
                        val uniqueID = conversationReference.push().key
                        if (uniqueID != null) {
                            PrefManager.write(PrefManager.CHAT_UNIQUE_ID, uniqueID)
                        }
                        createNewNodeAndAddMsgData(uniqueID)
                    }
                } else {
                   // Toast.makeText(this, "Enter Message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addMsgData(uniqueID: String?, addMsg: ChatModel) {
        val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, chatId)
        lastMsg = when {
            addMsg.type.equals("image") -> {
                "image"
            }
            addMsg.type.equals("document") -> {
                "document"
            }
            else -> {
                sendMsg
            }
        }
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val map = HashMap<String, Any>()
        map["chatMessages/${chatId}/${uniqueID}"] = addMsg
        map["conversations/${chatId}/chatLastMessage"] = lastMsg
        map["conversations/${chatId}/lastUpdate"] = System.currentTimeMillis()
        map["conversations/${chatId}/lastUpdates/${uid}"] = System.currentTimeMillis()

        uniqueID?.let {
            dbRef.updateChildren(
                map
            ) { error, ref ->
                if (error != null)
                    Log.e(">>>>> Inserted ", "Fail")
                else
                    Log.e(">>>>> Inserted ", "Success")
            }
        }
        retrieveData()
    }

    private fun collectMsgData(
        msgType: String, uri: String, txtMsg: String
    ): Pair<String?, ChatModel> {
        val addMsg: ChatModel
        uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val status: Long = 0
        val uniqueID = chatMessagesReference.push().key
        if (uniqueID != null) {
            PrefManager.write(PrefManager.CHAT_UNIQUE_ID, uniqueID)
        }
        Log.d(TAG, "onClick:id$uniqueID")
        val timestamp = System.currentTimeMillis()
        addMsg = if (msgType == "image" || msgType == "document") {
            ChatModel("", uri, uniqueID, uid, status, timestamp, msgType)
        } else {
            ChatModel("", txtMsg, uniqueID, uid, status, timestamp, msgType)
        }
        return Pair(uniqueID, addMsg)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CAMERA) {
            imageFilePath = filePhoto .absolutePath
            uploadImageMsg(imageFilePath)
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_MEDIA && data != null) {
            val imagePath = getRealPathFromURI(data.data)
            if (imagePath != null) {
                imageFilePath = imagePath
                uploadImageMsg(imageFilePath)
            }
        } else if (requestCode == REQUEST_CODE_GROUP) {
            val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
            val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "")
            ProgressDialog.showProgressBar(this)
            ProgressDialog.setCancelable()
            val query = FirebaseDatabase.getInstance().getReference("conversations").child(chatId)
            val valueEventListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val chatPic = dataSnapshot.child("chatPic").getValue(String::class.java)!!
                        val chatTitle = dataSnapshot.child("chatTitle").getValue(String::class.java)!!
                        binding.myToolbar.tvName.text = chatTitle
                        Utils.loadImage(baseContext, binding.myToolbar.civProfileImage, chatPic)
                        groupName=chatTitle
                 //       PrefManager.write(PrefManager.FCM_GROUP_NAME, groupName)
                        userDP=chatPic
                } else {
                    //ProgressDialog.hideProgressBar()
                    Log.d(TAG, "onDataChange: no data found")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_DOC && data != null)
    {
       // Toast.makeText(this, "doc selected", Toast.LENGTH_SHORT).show()

        val pathHolder = getRealPathFromUri(this, data.data)
        if (pathHolder != null) {
            uploadDocument(pathHolder)
        }else{
            val picturePath: String = getPath(this, data.data)
 //           Toast.makeText(this, ""+picturePath, Toast.LENGTH_SHORT).show()
            Log.d("Picture Path", picturePath)
//            val selectedFileUri: Uri? = data.data
//            val selectedFilePath = FilePath.getPath(this, selectedFileUri)
//            Log.i(TAG, "Selected File Path:$selectedFilePath")
//
//            if (selectedFilePath != null && !selectedFilePath.equals("")) {
//               // tvFileName.setText(selectedFilePath)
//            } else {
//                Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show()
//            }

//            val uri: Uri = data.data!!
//            val uriString = uri.toString()
//            val myFile = File(uriString)
//            Toast.makeText(this, "else$myFile", Toast.LENGTH_LONG).show()
//         //   uploadDocumentFile(myFile)
//            val path = myFile.absolutePath
//           // uploadDocument(path)
//            var displayName: String? = null
//
//            if (uriString.startsWith("content://")) {
//                var cursor: Cursor? = null
//                val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
//                try {
//                    cursor = contentResolver.query(uri, null, null, null, null)
//                    if (cursor != null && cursor.moveToFirst()) {
//                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
//                        Toast.makeText(applicationContext, " ------Name-----"+displayName, Toast.LENGTH_SHORT).show()
//                    }
//                } finally {
//                    cursor!!.close()
//                }
//            } else if (uriString.startsWith("file://")) {
//                displayName = myFile.name
//                Toast.makeText(applicationContext, ""+displayName, Toast.LENGTH_SHORT).show()
//            }
        }
    }else {
        super.onActivityResult(requestCode, resultCode, data)
    }
}

    private fun getPath(context: Context, uri: Uri?): String {
        var result: String? = null
        val proj = arrayOf(MediaStore.Files.FileColumns.DATA)
        val cursor = context.contentResolver.query(uri!!, proj, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(proj[0])
                result = cursor.getString(column_index)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }

//    fun getPath(uri: Uri?): String {
//        var path:String=""
//        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
//        var cursor = contentResolver.query(uri!!, projection, null, null, null)

//        if(cursor == null){
//            path = uri.path.toString()
//        }
//        else{
//            cursor.moveToFirst()
//            val column_index = cursor.getColumnIndexOrThrow(projection[0])
//            path = cursor.getString(column_index)
//            cursor.close()
//        }
//        return if (path == null || path.isEmpty()) uri.path!! else path
//        return try {
//            val proj = arrayOf(MediaStore.Images.Media.DATA)
//            val cursor = contentResolver.query(uri, proj, null, null, null);
//            cursor?.moveToFirst()
//            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            cursor.getString(columnIndex)
//        } finally {
//            cursor?.close()
//        }
 //   }

    private fun getRealPathFromUri(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            cursor?.moveToFirst()
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }

    private fun uploadDocument(pdfUri: String) {
        val fileName = File(pdfUri).name
        val stream = Uri.fromFile(File(pdfUri))
        val riversRef = storageReference.child("document/${fileName}")
        val uploadTask = riversRef.putFile(stream)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            riversRef.downloadUrl.addOnSuccessListener { uri ->
                val (uniqueID, addMsg) = collectMsgData("document", "" + uri, "msg")
                addMsgData(uniqueID, addMsg)
            }
        }
    }

    private fun uploadDocumentFile(pdfUri: File) {
       // val fileName = File(pdfUri).name
        val stream = Uri.fromFile(pdfUri)
        val riversRef = storageReference.child("document/${stream.lastPathSegment}")
        val uploadTask = riversRef.putFile(stream)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
         //   Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            riversRef.downloadUrl.addOnSuccessListener { uri ->
             //   Toast.makeText(this, "yuppppp", Toast.LENGTH_SHORT).show()
                val (uniqueID, addMsg) = collectMsgData("document", "" + uri, "msg")
                addMsgData(uniqueID, addMsg)
            }
        }
    }

    private fun uploadImageMsg(imageFilePath: String) {

        ProgressDialog.showProgressBar(this)

        val imgfile = Uri.fromFile(File(imageFilePath))
        val riversRef = storageReference.child("message_images/${imgfile.lastPathSegment}")
        val uploadTask = riversRef.putFile(imgfile)

        uploadTask.addOnFailureListener {

        }.addOnSuccessListener {

            riversRef.downloadUrl.addOnSuccessListener { uri ->
                val (uniqueID, addMsg) = collectMsgData("image", "" + uri, "msg")
                addMsgData(uniqueID, addMsg)
            }
        }
    }

    private fun getRealPathFromURI(contentURI: Uri?): String? {
        val result: String?
        val cursor: Cursor? =
            contentURI?.let { baseContext.contentResolver.query(it, null, null, null, null) }
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI?.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_MEDIA)
    }

    override fun onCameraClick() {
        capturePhoto()
    }

    override fun onGalleryClick() {
        openGalleryForImage()
    }

    override fun onDocumentClick() {
        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.type = "*/*"
        chooseFile = Intent.createChooser(chooseFile, "Choose a file")
        startActivityForResult(chooseFile, REQUEST_CODE_DOC)

//        intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "application/pdf"
//        startActivityForResult(intent, REQUEST_CODE_DOC)
    }

    private fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            PERMISSIONS,
            Constants.PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var res = true
        if (requestCode == Constants.PERMISSIONS_REQUEST_CODE && grantResults.isNotEmpty()) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    res = false
                }
            }
            if (res) {
                //launch()
            } else {
                Toast.makeText(
                    applicationContext,
                    "All permissions are required",
                    Toast.LENGTH_LONG
                ).show()
               // finish()
            }
        }
    }
}
