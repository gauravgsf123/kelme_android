package com.kelme.fragment.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.activity.login.LoginActivity
import com.kelme.adapter.DocumentListAdapter
import com.kelme.app.BaseFragment
import com.kelme.databinding.FragmentEditProfileBinding
import com.kelme.event.UpdateUserProfileEvent
import com.kelme.interfaces.CallUpdateProfile
import com.kelme.model.request.DeleteDocumentRequest
import com.kelme.model.response.DocumentData
import com.kelme.model.response.MyProfileData
import com.kelme.utils.*
import id.zelory.compressor.constraint.size
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.Locale
import java.util.Locale.getDefault

private const val REQUEST_CODE_CAMERA = 101
private const val REQUEST_CODE_GALLERY = 102
private const val REQUEST_CODE_CAMERA_PROFILE = 103
private const val REQUEST_CODE_GALLERY_PROFILE = 104
private const val REQUEST_CODE_DOC = 105
private lateinit var filePhoto: File
private const val FILE_NAME_PHOTO = "photo"
private lateinit var fileDocument: File
private const val FILE_NAME_DOCUMENT = "photo"

class EditProfileFragment : BaseFragment(), CallUpdateProfile, View.OnClickListener,
    BottomSheetOptionsForProfileImageFragment.ItemClickListener,
    BottomSheetOptionForProfileDocumentUploadFragment.ItemClickListener,
    DocumentListAdapter.onClickListener {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var myProfileData: MyProfileData
    private var gender = 0
    private lateinit var myProfileViewModel: MyProfileViewModel
    private var compressedImage: File? = null
    private var filePath: String = ""
    private var filePathProfile: String = ""
    private var isDocumentUpload = false

    private lateinit var adapterDocument: DocumentListAdapter
    private var pos = -1
    private var documentList = ArrayList<DocumentData>()
    private var isRegistered = true

    override fun callUpdateUserProfile() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            myProfileData = it.getParcelable(Constants.MY_PROFILE_DATA_MODEL)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)
        myProfileViewModel = ViewModelProvider(this, ViewModalFactory(activity?.application!!)).get(MyProfileViewModel::class.java)
        init()
        return binding.root
    }

    private fun init() {
        binding.cameraImg.setOnClickListener(this)
        binding.tvUploadMore.setOnClickListener(this)
        binding.name.setText(myProfileData.name)
        binding.email.setText(myProfileData.email)
        binding.phone.setText(myProfileData.phone_number)
        Glide.with(this).load(myProfileData.image).into(binding.profileImage)
        when (myProfileData.gender) {
            "Male" -> binding.gender.text = "Male"
            "Female" -> binding.gender.text = "Female"
            "Other" -> binding.gender.text = "Other"
        }

        adapterDocument = DocumentListAdapter(this)
        binding.rvDocument.adapter = adapterDocument

        if (myProfileData.document?.size!! > 0) {
            documentList.addAll(myProfileData.document!!)
            binding.tvNoDocument.visibility = View.GONE
            binding.rvDocument.visibility = View.VISIBLE

            (binding.rvDocument.adapter as DocumentListAdapter).setItems(documentList)
        } else {
            binding.tvNoDocument.visibility = View.VISIBLE
            binding.rvDocument.visibility = View.GONE
        }

        val listPopupWindowButton = binding.gender
        val listPopupWindow = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)

        listPopupWindow.anchorView = listPopupWindowButton

        val items = listOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_popup_window_item, items)
        listPopupWindow.setAdapter(adapter)

        listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            binding.gender.text = items[position]
            listPopupWindow.dismiss()
        }

        listPopupWindowButton.setOnClickListener { v: View? -> listPopupWindow.show() }
        checkGender()
        setObserver()
        setObserverForUploadDocument()
    }

    private fun setObserverForUploadDocument() {
        myProfileViewModel.uploadDocumentData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        myProfileViewModel.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()

                        val documentData = DocumentData(
                            response.data?.document_id.toString(),
                            response.data?.userId.toString(),
                            response.data?.title.toString(),
                            response.data?.document.toString(),
                            "",
                            response.data?.createdAt.toString(),
                            ""
                        )
                        documentList.add(documentData)
                        adapterDocument.notifyDataSetChanged()
                        showHideList()
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

        myProfileViewModel.updaeUserProfieData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        myProfileViewModel.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
//                    Toast.makeText(
//                        requireContext(),
//                        response.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                        (activity as DashboardActivity?)?.run {
                            onBackPressed()
                        }
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

        myProfileViewModel.deleteDocumentResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.message == "240") {
                        myProfileViewModel.logout()
                    } else {
                        ProgressDialog.hideProgressBar()
                        if (response.message == "Deleted Successfully.") {
//                        Toast.makeText(
//                            requireContext(),
//                            "Document delete successfully.",
//                            Toast.LENGTH_SHORT
//                        ).show()

                            if (pos >= 0) {
                                documentList.removeAt(pos)
                                adapterDocument.notifyItemRemoved(pos)
                                showHideList()
                            }
                        }
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

    private fun showHideList() {
        if (documentList.size > 0) {
            binding.tvNoDocument.visibility = View.GONE
            binding.rvDocument.visibility = View.VISIBLE
        } else {
            binding.tvNoDocument.visibility = View.VISIBLE
            binding.rvDocument.visibility = View.GONE
        }
    }

    private fun checkGender() {
        when (binding.gender.text.toString()) {
            "Male" -> gender = 1
            "Female" -> gender = 2
            "Other" -> gender = 3
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
            binding.tvUploadMore.id -> {
                isDocumentUpload = true
                childFragmentManager.let {
                    BottomSheetOptionForProfileDocumentUploadFragment.newInstance(Bundle(), this)
                        .apply {
                            show(it, tag)
                        }
                }
            }
        }
    }

//    fun onOptionClick(item: String) {
//        when (item) {
//            "open_camera" -> {
//                if (context?.let {
//                        checkSelfPermission(
//                            it,
//                            Manifest.permission.CAMERA
//                        )
//                    } == PackageManager.PERMISSION_DENIED) {
//                    val permissions = arrayOf(Manifest.permission.CAMERA)
//                    requestPermissions(permissions, CAMERA_PERMISION)
//                } else {
//                    openCamera()
//                }
//            }
//            "open_gallery" -> {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (context?.let {
//                            checkSelfPermission(
//                                it,
//                                Manifest.permission.READ_EXTERNAL_STORAGE
//                            )
//                        } == PackageManager.PERMISSION_DENIED) {
//                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
//                        requestPermissions(permissions, STORAGE_PERMISION)
//                    } else {
//                        chooseImageGallery();
//                    }
//                } else {
//                    chooseImageGallery();
//                }
//            }
//            "open_document" -> {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (context?.let {
//                            checkSelfPermission(
//                                it,
//                                Manifest.permission.READ_EXTERNAL_STORAGE
//                            )
//                        } == PackageManager.PERMISSION_DENIED) {
//                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
//                        requestPermissions(permissions, STORAGE_PERMISION)
//                    } else {
//                        chooseDocument()
//                    }
//                } else {
//                    chooseDocument()
//                }
//            }
//        }
//    }

//    override fun onCameraClick() {
//        capturePhoto()
//    }
//
//    override fun onGalleryClick() {
//        openGalleryForImage()
//    }

    override fun onItemClick(item: String) {
        when (item) {
            "open_camera" -> {
                if (context?.let {
                        checkSelfPermission(
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
                chooseImageGallery()
            }
            "open_document" -> {
                chooseDocument()
            }
            else -> {
                //Handle data
            }
        }
    }

    private fun openCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        fileDocument = getPhotoFile(FILE_NAME_DOCUMENT)
        val providerFile =
            FileProvider.getUriForFile(requireContext(), "com.kelme.fileprovider", fileDocument)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (context?.let { takePhotoIntent.resolveActivity(it.packageManager) } != null) {
            startActivityForResult(takePhotoIntent, REQUEST_CODE_CAMERA)
        } else {
            //Toast.makeText(context, "Camera could not open", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCameraProfile() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        filePhoto = getPhotoFile(FILE_NAME_PHOTO)
        val providerFile =
            FileProvider.getUriForFile(requireContext(), "com.kelme.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (context?.let { takePhotoIntent.resolveActivity(it.packageManager) } != null) {
            startActivityForResult(takePhotoIntent, REQUEST_CODE_CAMERA_PROFILE)
        } else {
            //Toast.makeText(context, "Camera could not open", Toast.LENGTH_SHORT).show()
        }
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    private fun chooseImageGalleryProfile() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY_PROFILE)
    }

    private fun chooseDocument() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
        //var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        //chooseFile.type = "*/*"
        /*chooseFile.type = "application/pdf"
        chooseFile = Intent.createChooser(chooseFile, "Choose a file")
        startActivityForResult(chooseFile, REQUEST_CODE_DOC)*/
    }

    companion object {
        val CAMERA_PERMISION = 1000;
        val STORAGE_PERMISION = 1001;
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_PERMISION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //  chooseImageGallery()
                } else {
                  //  Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //  openCamera()
                } else {
                    //Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CAMERA_PROFILE && resultCode == Activity.RESULT_OK) {
            //viewImage.setImageBitmap(takenPhoto)
            customProfileCompressImage(filePhoto)


        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            //viewImage.setImageBitmap(takenPhoto)
            customDocumentCompressImage(fileDocument)
            //val takenPhoto = BitmapFactory.decodeFile(compressedImage!!.absolutePath)
            //filePath = compressedImage!!.absolutePath
            //uploadDocument()

        } else if (requestCode == REQUEST_CODE_GALLERY_PROFILE && resultCode == Activity.RESULT_OK) {
            //val takenPhoto = BitmapFactory.decodeFile(filePhoto.absolutePath)
            //viewImage.setImageBitmap(takenPhoto)
            val imagePath = getRealPathFromURI(data?.data)//filePhoto.absolutePath

            if (imagePath != null) {
                filePathProfile = imagePath
            }
            Glide
                .with(this)
                .load(data?.data)
                .into(binding.profileImage)

        } else if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            //viewImage.setImageURI(data?.data)
            val imagePath = getRealPathFromURI(data?.data)//data?.data?.path!!
            if (imagePath != null) {
                filePath = imagePath
                uploadDocument()
            }



        }else if (requestCode == REQUEST_CODE_DOC && resultCode == Activity.RESULT_OK) {
            //viewImage.setImageURI(data?.data)
            Log.d("uploadDocument", data?.data.toString())
            val documentPath = FilePath.getPath(requireContext(),data?.data)//getRealPathOfDocumentFromUri(requireContext(),data?.data)//data?.data?.path!!
            if (documentPath != null) {
                filePath = documentPath
                uploadDocument()
            }else{
                Log.d("uploadDocument","file not found")
            }



        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun customProfileCompressImage(actualImage: File) {
        actualImage.let { imageFile ->
            lifecycleScope.launch {
                val compressedImage = Compressor.compress(requireContext(), imageFile) {
                    resolution(640, 480)
                    val destination = File(imageFile.parent, imageFile.name.lowercase(getDefault()))
                    destination(destination)
                    quality(50)
                    format(Bitmap.CompressFormat.JPEG)
                    size(180_152) // 1 MB
                }

                val takenPhoto = BitmapFactory.decodeFile(compressedImage.absolutePath)
                filePathProfile = compressedImage.absolutePath
                Glide
                    .with(requireActivity())
                    .load(takenPhoto)
                    .into(binding.profileImage)
            }

        }
    }
    private fun customDocumentCompressImage(actualImage: File) {
        actualImage.let { imageFile ->
            lifecycleScope.launch {
                 compressedImage = Compressor.compress(requireContext(), imageFile) {
                    resolution(640, 480)
                    val destination = File(imageFile.parent, imageFile.name.lowercase(getDefault()))
                    destination(destination)
                    quality(50)
                    format(Bitmap.CompressFormat.JPEG)
                    size(180_152) // 1 MB
                }

                filePath = compressedImage!!.absolutePath
                uploadDocument()
            }

        }
    }

    private fun getRealPathOfDocumentFromUri(context: Context, contentUri: Uri?): String? {
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

    override fun onResume() {
        super.onResume()
        (activity as DashboardActivity?)?.run {
            setTitle(getString(R.string.edit_profile))
            showSaveBtn()
            hideSearchBar()
            changeProfileColor()
            showBackArrow()
            hideUnreadCount()
            setAppTopBackgroundColor(R.color.foregroundColor)
        }
        if (isRegistered) {
            EventBus.getDefault().register(this)
            isRegistered = false
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as DashboardActivity?)?.run {
            resetColorChange()
            removeAppTopBackgroundColor()
        }
        EventBus.getDefault().unregister(this)
        isRegistered = true
    }

    private fun uploadDocument() {
        Log.d("uploadDocument", filePath)
        var file: File? = null
        var filePart: MultipartBody.Part? = null
        if (filePath != "") {
            file = File(filePath)
            filePart = MultipartBody.Part.createFormData(
                "my_doc",
                file.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), file)
            )
        }
        if (filePart != null) {
            myProfileViewModel.uploadDocument(
                filePart,
                file?.name?.let { getPart(it) }
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UpdateUserProfileEvent?) {
        updateUserProfile(binding.root)
    }

    private fun updateUserProfile(root: View) {
        checkGender()
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        if (filePathProfile != "") {
            val file = File(filePathProfile)
            builder.addFormDataPart(
                "profile_pic",
                file.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), file)
            )
        }
        builder.addFormDataPart("name", binding.name.text.toString())
        builder.addFormDataPart("email", binding.email.text.toString())
        builder.addFormDataPart("phone_no", binding.phone.text.toString())
        builder.addFormDataPart("gender", gender.toString())
        val body = builder.build()
        myProfileViewModel.updateUserProfile(body)

    }

    override fun onDeleteItem(position: Int) {
        pos = position
        myProfileViewModel.deleteDocument(DeleteDocumentRequest(documentList[position].documentId))
    }

    override fun onItemClickProfile(item: String) {
        when (item) {
            "open_camera" -> {
                if (context?.let {
                        checkSelfPermission(
                            it,
                            Manifest.permission.CAMERA
                        )
                    } == PackageManager.PERMISSION_DENIED) {
                    val permissions = arrayOf(Manifest.permission.CAMERA)
                    requestPermissions(permissions, CAMERA_PERMISION)
                } else {
                    openCameraProfile()
                }
            }
            "open_gallery" -> {
                chooseImageGalleryProfile();
            }
        }
    }
}
