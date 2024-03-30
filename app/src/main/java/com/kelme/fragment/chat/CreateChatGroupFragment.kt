package com.kelme.fragment.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.adapter.UserListAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentCreateChatGroupBinding
import com.kelme.event.SaveChatGroupEvent
import com.kelme.fragment.profile.BottomSheetOptionForDocumentUploadFragment
import com.kelme.fragment.profile.BottomSheetOptionsForProfileImageFragment
import com.kelme.fragment.profile.EditProfileFragment
import com.kelme.interfaces.QuantityListner
import com.kelme.model.ChatMembersDetails
import com.kelme.model.ChatModelNewGroupUser
import com.kelme.model.ContactModel
import com.kelme.model.response.ChatModel
import com.kelme.utils.PrefManager
import com.kelme.utils.ProgressDialog
import com.kelme.utils.ViewModalFactory
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class CreateChatGroupFragment : BaseFragment(), QuantityListner,
    BottomSheetOptionForDocumentUploadFragment.OptionClickListener, View.OnClickListener,
    BottomSheetOptionsForProfileImageFragment.ItemClickListener {
    private lateinit var binding: FragmentCreateChatGroupBinding
    private lateinit var viewModal: ChatViewModal
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReferenceMessages: DatabaseReference
    private lateinit var userListAdapter: UserListAdapter
    var userList: ArrayList<ContactModel?> = ArrayList()
    var selectedUserList: ArrayList<ContactModel?> = ArrayList()
    var loginUser: ContactModel? = ContactModel()
    private var filePath: String = ""
    private lateinit var filePhoto: File
    var uniqueIDMsg: String=""
    private var isDocumentUpload = false
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference
    var uploadedUri : Uri = Uri.parse("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_create_chat_group, container, false)
        viewModal = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(
            ChatViewModal::class.java
        )
        //  viewModal.contactList()
        // setContactListObserver()

        setUi()
        retrieveChatList()

        EventBus.getDefault().register(this)
//        binding.tvAddMember.setOnClickListener {
//          //  Log.d("list_item",userListAdapter.checkedItems.toString())
//            Log.d("list_item",userListAdapter.checkedItems.toString())
//        }
        return binding.root
    }

    private fun setUi() {
        binding.cameraImg.setOnClickListener(this)
        binding.rvContact.setHasFixedSize(true)
        binding.rvContact.layoutManager = GridLayoutManager(
            requireContext(),
            1,
            RecyclerView.VERTICAL,
            false
        )

        userListAdapter = UserListAdapter(requireContext(), userList, this)

        binding.rvContact.adapter = userListAdapter

//        userListAdapter.onItemClick(object : ItemClickListener {
//            override fun onClick(position: Int, view: View?) {
//                // val bundle = Bundle()
//                //   bundle.putParcelable(Constants.COUNTRY_MODEL, userList[position])
//                //(activity as DashboardActivity).replaceFragment(CountryDetailFragment(), bundle)
//            }
//        })
    }

    private fun retrieveChatList() {

        ProgressDialog.showProgressBar(requireContext())

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
                            if(PrefManager.read(PrefManager.COMPANYID, "")=="" ||
                                PrefManager.read(PrefManager.COMPANYID, "") == companyId) {
                                userList.add(user)
                            }
                        }else loginUser = user
                        /*if (user.userId != uid) {
                            userList.add(user)
                        } else {
                            loginUser = user
                            //Log.d(TAG, "onDataChange: LoginUser" + loginUser.toString())
                        }*/
                       // Log.d(TAG, "onDataChange: " + userList.count())
                        userListAdapter.updateItems(userList)
                        ProgressDialog.hideProgressBar()
                    }
                } else {
                   // Log.d(TAG, "onDataChange: no data found")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle("Create Group")
            hideNotificationIcon()
            showBackArrow()
            hideSearchBar()
            showSaveBtn()
            hideBottom()
            hideMapControl()
            hideUnreadCount()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SaveChatGroupEvent?) {

        if (binding.etGroupName.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Please Enter Group Name", Toast.LENGTH_SHORT).show()
        } else {
            ProgressDialog.showProgressBar(requireActivity())
            ProgressDialog.setCancelable()
            uploadImageMsg(filePath)
        }
    }

    private fun createGroup(root: View) {
        //Log.d("create_group", "create group")
        if (selectedUserList.size > 0) {
            if (selectedUserList.size < 14) {
                val uniqueID = FirebaseDatabase.getInstance().getReference("conversations").push().key
                createNewNodeAndAddMsgData(uniqueID)
            } else {
//                Toast.makeText(
//                    requireContext(),
//                    "You can add maximum 15 members",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        } else {
           // Toast.makeText(requireContext(), "please select user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNewNodeAndAddMsgData(uniqueID: String?) {
        val addMsg = collectDataOfNewGroupUser(uniqueID)
        val msgListMap: HashMap<String, ChatModel> = HashMap()
        databaseReference = FirebaseDatabase.getInstance().getReference("conversations")
        uniqueID?.let {
            databaseReference.child(uniqueID).setValue(addMsg).addOnCompleteListener {
                if (it.isSuccessful) {
                   // Log.d("inelse", "createNewNodeAndAddMsgData: In IF")
                    databaseReferenceMessages = FirebaseDatabase.getInstance().getReference("chatMessages")
                    val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
                    msgListMap.clear()
                    for (s in selectedUserList) {
                        uniqueIDMsg = FirebaseDatabase.getInstance().getReference("chatMessages").push().key!!
                        msgListMap[uniqueIDMsg] = ChatModel(
                            uid, "", uniqueIDMsg, s?.userId, 0, System.currentTimeMillis(), "join"
                        )
                    }
                    msgListMap[uniqueIDMsg] = ChatModel(
                        uid, "", uniqueIDMsg, uid, 0, System.currentTimeMillis(), "create"
                    )
                    databaseReferenceMessages.child(uniqueID).updateChildren(msgListMap as Map<String, Any>)

                    //Log.d(TAG, "createNewNodeAndAddMsgData: " + "success")
                    ProgressDialog.hideProgressBar()
                    activity?.onBackPressed()
                } else {
                   // Log.d("inelse", "createNewNodeAndAddMsgData: In Else")
                }
            }
        }
    }

    private fun collectDataOfNewGroupUser(uniqueID: String?): ChatModelNewGroupUser {
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        val chatID = uniqueID
        val lastmsg = ""
        val chatPic = uploadedUri.toString()
        val chatType = "group"
        val createrId = uid
        val lastUpdate: Long = System.currentTimeMillis()
        val created = System.currentTimeMillis()
        val chatTitle = binding.etGroupName.text.toString()
        val chatMembers: HashMap<String?, Boolean> = HashMap()
        chatMembers[loginUser!!.userId] = true
        val lastUpdates: HashMap<String?, Long> = HashMap()
        lastUpdates[loginUser!!.userId] = 0
        val lastMessages: HashMap<String, String> = HashMap()
        //Log.d("inelse", "createNewNodeAndAddMsgData: In chatpic$chatPic")
        val memberDetails: HashMap<String?, ChatMembersDetails> = HashMap()
        memberDetails[loginUser!!.userId] = ChatMembersDetails(0, loginUser!!.userId, 0, 0)
        for (s in selectedUserList) {
            s?.userId?.let { lastUpdates.put(it, 0) }
            s?.userId?.let { chatMembers.put(it, true) }
            s?.userId?.let { lastMessages.put(it, " ") }
            s?.userId?.let { memberDetails.put(it, ChatMembersDetails(0, it, 0, 0)) }
        }
        val addMsg = ChatModelNewGroupUser(
            chatID, lastmsg, chatPic, chatMembers, chatTitle,
            chatType, created, createrId, lastUpdate, lastUpdates, lastMessages, memberDetails
        )
        return addMsg
    }

    override fun onQuantitychanged(userlist: ArrayList<ContactModel>) {
        selectedUserList.clear()
        selectedUserList.addAll(userlist.toList())
    }

    override fun onOptionClick(item: String) {
        when (item) {
            "open_camera" -> {
                if (context?.let {
                        PermissionChecker.checkSelfPermission(
                            it, Manifest.permission.CAMERA
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
                    if (context?.let {
                            PermissionChecker.checkSelfPermission(
                                it,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        } == PackageManager.PERMISSION_DENIED) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, STORAGE_PERMISION)
                    } else {
                        chooseImageGallery();
                    }
                } else {
                    chooseImageGallery();
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
                childFragmentManager.let {
                    BottomSheetOptionsForProfileImageFragment.newInstance(Bundle(), this).apply {
                        show(it, tag)
                    }
                }
            }
        }
    }

    fun onItemClick(item: String) {
        when (item) {
            "open_camera" -> {
                if (context?.let {
                        PermissionChecker.checkSelfPermission(
                            it,
                            Manifest.permission.CAMERA
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
                    if (context?.let {
                            PermissionChecker.checkSelfPermission(
                                it,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        } == PackageManager.PERMISSION_DENIED) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, EditProfileFragment.STORAGE_PERMISION)
                    } else {
                        chooseImageGallery();
                    }
                } else {
                    chooseImageGallery();
                }
            }
            else -> {
                //Handle data
            }
        }
    }

    override fun onItemClickProfile(item: String) {
        when (item) {
            "open_camera" -> {
                if (context?.let {
                        PermissionChecker.checkSelfPermission(
                            it,
                            Manifest.permission.CAMERA
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
                    if (context?.let {
                            PermissionChecker.checkSelfPermission(
                                it,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        } == PackageManager.PERMISSION_DENIED) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, EditProfileFragment.STORAGE_PERMISION)
                    } else {
                        chooseImageGallery();
                    }
                } else {
                    chooseImageGallery();
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
            FileProvider.getUriForFile(requireContext(), "com.kelme.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (context?.let { takePhotoIntent.resolveActivity(it.packageManager) } != null) {
            startActivityForResult(takePhotoIntent, Companion.REQUEST_CODE_CAMERA)
        } else {
            //Toast.makeText(context, "Camera could not open", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Companion.REQUEST_CODE_GALLERY)
    }

    companion object {
        private val CAMERA_PERMISION = 1000
        private val STORAGE_PERMISION = 1001
        private const val REQUEST_CODE_CAMERA = 101
        private const val REQUEST_CODE_GALLERY = 102
        private const val FILE_NAME = "photo.jpg"
    }

    private fun uploadImageMsg(imageFilePath: String) {
        Log.d(TAG, "uploadImageMsg: $imageFilePath")
        if (imageFilePath != "") {
            val imgfile = Uri.fromFile(File(imageFilePath))
            val riversRef = storageReference.child("Profile_images/${imgfile.lastPathSegment}")
            val uploadTask = riversRef.putFile(imgfile)
            uploadTask.addOnFailureListener {
                // ProgressDialog.hideProgressBar()
            }.addOnSuccessListener {
                riversRef.downloadUrl.addOnSuccessListener { uri ->
                    uploadedUri = uri
                    createGroup(binding.root)
                }
            }
        } else {
            createGroup(binding.root)
        }
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
            contentURI?.let { requireActivity().contentResolver.query(it, null, null, null, null) }
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