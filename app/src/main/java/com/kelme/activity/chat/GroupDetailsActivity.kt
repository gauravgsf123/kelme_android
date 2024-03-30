package com.kelme.activity.chat

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.adapter.AddAdminAdapter
import com.kelme.adapter.GroupDetailsAdapter
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityGroupDetailsBinding
import com.kelme.fragment.profile.BottomSheetOptionForDocumentUploadFragment
import com.kelme.fragment.profile.BottomSheetOptionsForProfileImageFragment
import com.kelme.fragment.profile.EditProfileFragment
import com.kelme.interfaces.ItemClickListener
import com.kelme.model.ChatMembersDetails
import com.kelme.model.response.ChatListModelWithName
import com.kelme.model.response.ChatModel
import com.kelme.utils.PrefManager
import com.kelme.utils.ProgressDialog
import com.kelme.utils.Utils
import java.io.File

class GroupDetailsActivity : BaseActivity(),
    BottomSheetOptionForDocumentUploadFragment.OptionClickListener, View.OnClickListener,
    BottomSheetOptionsForProfileImageFragment.ItemClickListener {

    val msgListMap: HashMap<String, ChatModel> = HashMap()
    var memberDetails: HashMap<String, ChatMembersDetails> = HashMap()
    var memberDetailsclone: HashMap<String, ChatMembersDetails> = HashMap()
    var userLeft: Boolean = false
    private lateinit var adapter: GroupDetailsAdapter
    private lateinit var adminAdapter: AddAdminAdapter
    lateinit var binding: ActivityGroupDetailsBinding
    val chatMembers: HashMap<String, Any> = HashMap()
    var membersList: ArrayList<ChatListModelWithName> = ArrayList()
    var adminMembersList: ArrayList<ChatListModelWithName> = ArrayList()
    private var groupName: String = "String"
    private var groupProfile: String = "String"
    private lateinit var databaseReferenceMessage: DatabaseReference
    private lateinit var databaseReferenceMessageType: DatabaseReference
    private var filePath: String = ""
    private var isDocumentUpload = false
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference
    var uploadedUri = ""
    private lateinit var filePhoto: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_details)
        groupName = intent.getStringExtra("GroupName").toString()
        groupProfile = intent.getStringExtra("GroupProfile").toString()

        setUi()
    }

    override fun initializerControl() {

    }

    private fun searchMemberDetails() {
        membersList.clear()
        adminMembersList.clear()
        memberDetails.clear()
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "")
        ProgressDialog.showProgressBar(this)
        ProgressDialog.setCancelable()
        val query = FirebaseDatabase.getInstance().getReference("conversations").child(chatId)
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val isMemberLeave = dataSnapshot.child("chatMembersDetails").child(uid)
                        .child("memberLeave").getValue(Long::class.java)!!
                    PrefManager.write(PrefManager.IS_MEMBER_LEAVE, isMemberLeave)
                    userLeft = isMemberLeave > 0
                    if (userLeft)
                        binding.btnLeave.text = getString(R.string.delete_group)
                    else
                        binding.btnLeave.text = getString(R.string.leavegroup)
                    memberDetails = dataSnapshot.child("chatMembersDetails").value as HashMap<String, ChatMembersDetails>
                    val members = dataSnapshot.child("chatMembers")
                    searchMembersDetails(members)
                    Log.d(TAG, "onDataChange: chatMembers$chatMembers")
                    val handler = Handler()
                    handler.postDelayed({
                        ProgressDialog.hideProgressBar()
                        adapter.updateItems(membersList)
                        Log.d(TAG, "onDataChange: memberList $membersList")
                    }, 1000)
                } else {
                    //ProgressDialog.hideProgressBar()
                    Log.d(TAG, "onDataChange: no data found")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun searchMembersDetails(members: DataSnapshot) {
        for (s in members.children) {
            Log.d(TAG, "onDataChange: skey" + s.key)

            val queryUser = FirebaseDatabase.getInstance().getReference("users").child(s.key!!)
            val valueEventListenerUser: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        addMembersInList(dataSnapshot, s)
                        Log.d(TAG, "onDataChange: members$membersList")
                    } else {
                        Log.d(TAG, "onDataChange: no data found")
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            }
            queryUser.addListenerForSingleValueEvent(valueEventListenerUser)
        }
    }

    private fun addMembersInList(
        dataSnapshot: DataSnapshot,
        s: DataSnapshot
    ) {
        val createrID = PrefManager.read(PrefManager.CREATER_ID, "")
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val name = dataSnapshot.child("name").getValue(String::class.java)
        val profileImage = dataSnapshot.child("profilePicture").getValue(String::class.java)
        val chatId = s.key
        val temp = ChatListModelWithName()
        ChatMembersDetails()

        temp.name = name
        temp.chatPic = profileImage
        temp.chatId = chatId!!    //its a user Id not chat Id but it was ready in model class so we used this

        if (createrID == temp.chatId) {
            temp.isAdmin = getString(R.string.admin)
        } else {
            temp.isAdmin = getString(R.string.remove)
            //adminMembersList.add(temp)
            checkMemberLeaveAdmin(temp)
        }
        checkMemberLeave(temp)
    }

    private fun checkMemberLeave(temp: ChatListModelWithName) {
        for (member in memberDetails) {
            if (member.key == temp.chatId) {
                memberDetailsclone.clear()
                memberDetailsclone[member.key] = member.value
                temp.chatMemberDetails = memberDetailsclone
                val modal = getObjectFromResponse(
                    memberDetailsclone[member.key].toString(),
                    ChatMembersDetails::class.java
                )
                if (modal.memberLeave == 0L)
                    membersList.add(temp)
            }
        }
    }

    private fun checkMemberLeaveAdmin(temp: ChatListModelWithName) {
        for (member in memberDetails) {
            if (member.key == temp.chatId) {
                memberDetailsclone.clear()
                memberDetailsclone[member.key] = member.value
                temp.chatMemberDetails = memberDetailsclone
                val modal = getObjectFromResponse(
                    memberDetailsclone[member.key].toString(),
                    ChatMembersDetails::class.java
                )
                if (modal.memberLeave == 0L)
                    adminMembersList.add(temp)
            }
        }
    }

    fun <T> getObjectFromResponse(response: String, convertToClass: Class<T>): T {
        val gson = Gson()
        return getObjectFromResponseTo(gson, response, convertToClass)
    }

    private fun <T> getObjectFromResponseTo(
        gson: Gson,
        response: String,
        convertToClass: Class<T>
    ): T {
        return gson.fromJson(response, convertToClass)
    }

    private fun setUi() {
        binding.cameraImg.setOnClickListener(this)
        binding.ivAddChatGroup.setOnClickListener {
            val intent = Intent(this, AddNewGroupMemberActivity::class.java)
            intent.putExtra(getString(R.string.member_count), membersList.size)
            intent.putParcelableArrayListExtra("Members",membersList)
            startActivity(intent)
        }

        Glide
            .with(this)
            .load(groupProfile)
            .into(binding.profileImage)

        val createrID = PrefManager.read(PrefManager.CREATER_ID, "")
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")

        setToolBarData(createrID, uid)

        setAdapterToRecyclerView()

        binding.ivFilter.setOnClickListener {
            ProgressDialog.showProgressBar(this)
            ProgressDialog.setCancelable()
            uploadImageMsg(filePath)
        }

        binding.etGroupName.setText(groupName)
        binding.backArrow.setOnClickListener { onBackPressed() }
        Utils.loadImage(this, binding.profileImage, groupProfile)

        binding.btnLeave.setOnClickListener {
            if (uid == createrID && !userLeft) {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.alert_select_admin)
                val rvMembersList = dialog.findViewById(R.id.rvMembers) as RecyclerView
                val noBtn = dialog.findViewById(R.id.btncancel) as Button
                val yesBtn = dialog.findViewById(R.id.btnleave) as Button

                //rvMembersList.setHasFixedSize(true)
                rvMembersList.layoutManager = GridLayoutManager(
                    this,
                    1,
                    RecyclerView.VERTICAL,
                    false
                )
                adminAdapter = AddAdminAdapter(this, adminMembersList)
                rvMembersList.adapter = adminAdapter
                adminAdapter.onItemClick(object : ItemClickListener {
                    override fun onClick(position: Int, view: View?) {
                        adminMembersList.forEach {
                            if (it.isSelected) {
                                it.isSelected = false
                            }
                        }
                        adminMembersList[position].isSelected = true
                        adminAdapter.updateItems(adminMembersList)
                    }
                })

                yesBtn.setOnClickListener {
                    val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "")
                    adminMembersList.forEach {
                        if (it.isSelected) {
                            val userId = it.chatId
                            val currentLoginUser = PrefManager.read(PrefManager.FCM_USER_ID, "")

                            databaseReferenceMessage = FirebaseDatabase.getInstance().getReference("conversations")
                                    .child(chatId)
                            databaseReferenceMessage.child("createrId").setValue(userId)
                            databaseReferenceMessage.child("chatMembersDetails")
                                .child(currentLoginUser)
                                .child("memberLeave")
                                .setValue(System.currentTimeMillis())
                        }
                    }
                    msgListMap.clear()
                    val uniqueIDMsg =
                        FirebaseDatabase.getInstance().getReference("chatMessages").push().key!!
                    msgListMap[uniqueIDMsg] =
                        ChatModel(uid, "", uniqueIDMsg, uid, 0, System.currentTimeMillis(), "left")
                    databaseReferenceMessageType =
                        FirebaseDatabase.getInstance().getReference("chatMessages")
                    databaseReferenceMessageType.child(chatId)
                        .updateChildren(msgListMap as Map<String, Any>)

                    dialog.dismiss()
                    onBackPressed()
                }
                noBtn.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            } else if (uid != createrID && !userLeft) {
                val textStr = getString(R.string.leave_group)
                showDialog(textStr)

            } else if (uid != createrID && userLeft) {
                val textStr = getString(R.string.sure_delete_group)
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.alert_dialog_yes_no)
                val body = dialog.findViewById(R.id.tvInfo) as TextView
                body.text = textStr
                val yesBtn = dialog.findViewById(R.id.btn_done) as TextView
                val noBtn = dialog.findViewById(R.id.btnDelete) as TextView
                yesBtn.setOnClickListener {
                    ProgressDialog.showProgressBar(this)
                    val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "")
                    ProgressDialog.showProgressBar(this)
                    databaseReferenceMessage = FirebaseDatabase.getInstance().getReference("conversations").child(chatId)
                    databaseReferenceMessage.child("chatMembers").child(uid).removeValue()
                    databaseReferenceMessage.child("chatMembersDetails").child(uid).removeValue()
                    databaseReferenceMessage.child("lastUpdates").child(uid).removeValue()
                    ProgressDialog.hideProgressBar()
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    ProgressDialog.hideProgressBar()
                    dialog.dismiss()
                    onBackPressed()
                }
                noBtn.setOnClickListener { dialog.dismiss() }
                dialog.show()
            } else if (uid == createrID && userLeft) {
                binding.btnLeave.text = getString(R.string.delete_grp)
            }
        }
    }

    private fun setAdapterToRecyclerView() {
        binding.rvContact.setHasFixedSize(true)
        binding.rvContact.layoutManager = GridLayoutManager(
            this,
            1,
            RecyclerView.VERTICAL,
            false
        )
        adapter = GroupDetailsAdapter(this, membersList)
        binding.rvContact.adapter = adapter

        adapter.onItemClick(object : ItemClickListener {
            override fun onClick(position: Int, view: View?) {
                if (membersList[position].isAdmin != getString(R.string.admin)) {
                    val dialog = Dialog(this@GroupDetailsActivity)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setCancelable(false)
                    dialog.setContentView(R.layout.alert_dialog_yes_no)
                    val body = dialog.findViewById(R.id.tvInfo) as TextView
                    body.text = getString(R.string.leave_group)
                    val yesBtn = dialog.findViewById(R.id.btn_done) as TextView
                    val noBtn = dialog.findViewById(R.id.btnDelete) as TextView
                    yesBtn.setOnClickListener {
                        ProgressDialog.showProgressBar(this@GroupDetailsActivity)
                        val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "0")
                        databaseReferenceMessage =
                            FirebaseDatabase.getInstance().getReference("conversations").child(chatId)
                        databaseReferenceMessage.child("chatMembersDetails")
                            .child(membersList[position].chatId).child("memberLeave").setValue(System.currentTimeMillis())

                        msgListMap.clear()
                        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
                        val uniqueIDMsg =
                            FirebaseDatabase.getInstance().getReference("chatMessages").push().key!!
                        msgListMap[uniqueIDMsg] = ChatModel(
                            uid,
                            "",
                            uniqueIDMsg,
                            membersList[position].chatId,
                            0,
                            System.currentTimeMillis(),
                            "left"
                        )
                        databaseReferenceMessageType =
                            FirebaseDatabase.getInstance().getReference("chatMessages")
                        databaseReferenceMessageType.child(chatId)
                            .updateChildren(msgListMap as Map<String, Any>).addOnCompleteListener { dialog.dismiss() }

                        membersList.removeAt(position)
                        adapter.notifyDataSetChanged()
                        ProgressDialog.hideProgressBar()
                    }
                    noBtn.setOnClickListener { dialog.dismiss() }
                    dialog.show()
                }
            }
        })

    }

    private fun setToolBarData(createrID: String, uid: String) {
        if (createrID == uid) {
           // binding.tvTitle.text = getString(R.string.app_name)
            binding.cameraImg.visibility = View.VISIBLE
            binding.ivAddChatGroup.visibility = View.VISIBLE
            binding.ivFilter.visibility = View.VISIBLE
        } else {
          //  binding.tvTitle.text = getString(R.string.grp_details)
            binding.cameraImg.visibility = View.INVISIBLE
            binding.ivAddChatGroup.visibility = View.INVISIBLE
            binding.ivFilter.visibility = View.INVISIBLE
        }
    }

    private fun showDialog(title: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_dialog_yes_no)
        val body = dialog.findViewById(R.id.tvInfo) as TextView
        body.text = title
        val yesBtn = dialog.findViewById(R.id.btn_done) as TextView
        val noBtn = dialog.findViewById(R.id.btnDelete) as TextView
        yesBtn.setOnClickListener {
            ProgressDialog.showProgressBar(this)
            val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "")

            val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
            databaseReferenceMessage = FirebaseDatabase.getInstance().getReference("conversations")
            databaseReferenceMessage.child(chatId).child("chatMembersDetails").child(uid)
                .child("memberLeave")
                .setValue(System.currentTimeMillis())

            msgListMap.clear()
            val uniqueIDMsg =
                FirebaseDatabase.getInstance().getReference("chatMessages").push().key!!
            msgListMap[uniqueIDMsg] =
                ChatModel(uid, "", uniqueIDMsg, uid, 0, System.currentTimeMillis(), "left")
            databaseReferenceMessageType =
                FirebaseDatabase.getInstance().getReference("chatMessages")
            databaseReferenceMessageType.child(chatId)
                .updateChildren(msgListMap as Map<String, Any>)

            ProgressDialog.hideProgressBar()
            dialog.dismiss()
            onBackPressed()
        }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        searchMemberDetails()
    }

    override fun onOptionClick(item: String) {
        when (item) {
            "open_camera" -> {
                if (this.let {
                        PermissionChecker.checkSelfPermission(
                            it, Manifest.permission.CAMERA
                        )
                    } == PackageManager.PERMISSION_DENIED) {
                    val permissions = arrayOf(Manifest.permission.CAMERA)
                    requestPermissions(permissions, EditProfileFragment.CAMERA_PERMISION)
                } else {
                    openCamera()
                }
            }
            "open_gallery" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (this.let {
                            PermissionChecker.checkSelfPermission(
                                it,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        } == PackageManager.PERMISSION_DENIED) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, EditProfileFragment.STORAGE_PERMISION)
                    } else {
                        chooseImageGallery()
                    }
                } else {
                    chooseImageGallery()

                }
            }
            "open_document" -> {
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            binding.cameraImg.id -> {
                isDocumentUpload = false
                BottomSheetOptionsForProfileImageFragment.newInstance(Bundle(), this).apply {
                    show(supportFragmentManager, tag)
                }
            }
        }
    }

    override fun onItemClickProfile(item: String) {
        when (item) {
            "open_camera" -> {
                if (this.let {
                        PermissionChecker.checkSelfPermission(
                            it,
                            Manifest.permission.CAMERA
                        )
                    } == PackageManager.PERMISSION_DENIED) {
                    val permissions = arrayOf(Manifest.permission.CAMERA)
                    requestPermissions(permissions, CAMERA_PERMISION)
                } else {
                    openCamera()
                }
            }
            "open_gallery" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (this.let {
                            PermissionChecker.checkSelfPermission(
                                it,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        } == PackageManager.PERMISSION_DENIED) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, STORAGE_PERMISION)
                    } else {
                        chooseImageGallery()
                    }
                } else {
                    chooseImageGallery()
                }
            }
            else -> {
                //Handle data
            }
        }
    }

    private fun openCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        filePhoto = getPhotoFile(Companion.FILE_NAME)
        val providerFile =
            FileProvider.getUriForFile(this, "com.kelme.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (this.let { takePhotoIntent.resolveActivity(it.packageManager) } != null) {
            startActivityForResult(takePhotoIntent, Companion.REQUEST_CODE_CAMERA)
        } else {
          //  Toast.makeText(this, "Camera could not open", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Companion.REQUEST_CODE_GALLERY)
    }

    companion object {
        private const val CAMERA_PERMISION = 1000
        private const val STORAGE_PERMISION = 1001
        private const val REQUEST_CODE_CAMERA = 101
        private const val REQUEST_CODE_GALLERY = 102
        private const val FILE_NAME = "photo.jpg"
    }

    private fun uploadImageMsg(imageFilePath: String) {

      //  ProgressDialog.showProgressBar(this)

        val imgfile = Uri.fromFile(File(imageFilePath))
        val riversRef = storageReference.child("Profile_images/${imgfile.lastPathSegment}")

        val groupName = binding.etGroupName.text.toString()
        val chatId = PrefManager.read(PrefManager.FCM_CHAT_ID, "chatId")
        databaseReferenceMessage = FirebaseDatabase.getInstance().getReference("conversations").child(chatId)
        databaseReferenceMessage.child("chatTitle").setValue(groupName)

        val uploadTask = riversRef.putFile(imgfile)
        uploadTask.addOnFailureListener {
            ProgressDialog.hideProgressBar()
            onBackPressed()
        }.addOnSuccessListener {
            ProgressDialog.hideProgressBar()
            riversRef.downloadUrl.addOnSuccessListener { uri ->
                uploadedUri = uri.toString()
                databaseReferenceMessage.child("chatPic").setValue(uploadedUri).addOnCompleteListener {
                    ProgressDialog.hideProgressBar()
                    onBackPressed()
                }
            }
        }.addOnProgressListener {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, dataa: Intent?) {
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            val takenPhoto = BitmapFactory.decodeFile(filePhoto.absolutePath)
            filePath = filePhoto.absolutePath

            Glide
                .with(this)
                .load(takenPhoto)
                .into(binding.profileImage)

        } else {
            super.onActivityResult(requestCode, resultCode, dataa)
        }
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            val imagePath = getRealPathFromURI(dataa?.data)//data?.data?.path!!
            if (imagePath != null) {
                filePath = imagePath
            }
            Glide
                .with(this)
                .load(dataa?.data)
                .into(binding.profileImage)
        }
    }

    private fun getRealPathFromURI(contentURI: Uri?): String? {
        val result: String?
        val cursor: Cursor? =
            contentURI?.let { contentResolver.query(it, null, null, null, null) }
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
}