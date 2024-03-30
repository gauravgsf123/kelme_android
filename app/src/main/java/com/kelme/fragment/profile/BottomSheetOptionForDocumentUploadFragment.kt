package com.kelme.fragment.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kelme.R
import com.kelme.activity.chat.ChatConversationActivity
import com.kelme.databinding.FragmentBottomSheetOptionForDocumentUploadBinding
import com.kelme.interfaces.MediaInterface
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class BottomSheetOptionForDocumentUploadFragment : BottomSheetDialogFragment() {
    var activity: ChatConversationActivity? = null
    var mediaInterface: MediaInterface? = null
    private lateinit var binding: FragmentBottomSheetOptionForDocumentUploadBinding
    val REQUEST_CODE_CAMERA = 200
    val REQUEST_CODE_Gallery = 100
    val REQUEST_CODE_DOCUMENT = 300
    private lateinit var filePhoto: File
    private val FILE_NAME = "photo.jpg"
    private var filePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is MediaInterface) {
            mediaInterface = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_bottom_sheet_option_for_document_upload, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        // We can have cross button on the top right corner for providing element to dismiss the bottom sheet
        binding.tvCancel.setOnClickListener { dismissAllowingStateLoss() }

        binding.tvOpenCamera.setOnClickListener {
            dismissAllowingStateLoss()
            // mListener?.onOptionClick("open_camera")
            if (askForPermissionsCamera()) {
                // Permissions are already granted, do your stuff
                // capturePhoto()
                mediaInterface?.onCameraClick()
            }
        }

        binding.tvOpenGallery.setOnClickListener {
            dismissAllowingStateLoss()

            // mListener?.onOptionClick("open_gallery")
            if (askForPermissions_media()) {
                // Permissions are already granted, do your stuff
                //openGalleryForImage()
                mediaInterface?.onGalleryClick()
            }
        }


        binding.tvOpenDocument.setOnClickListener {
            mediaInterface?.onDocumentClick()
            dismissAllowingStateLoss()

            // mListener?.onOptionClick("open_document")
        }
    }


    fun isPermissionsAllowedMedia(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun askForPermissions_media(): Boolean {
        if (!isPermissionsAllowedMedia()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_Gallery
                )
            }
            return false
        }
        return true
    }

    fun askForPermissionsDoc(): Boolean {
        if (!isPermissionsAllowedMedia()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_DOCUMENT
                )
            }
            return false
        }
        return true
    }

    private fun isPermissionsAllowedCamera(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askForPermissionsCamera(): Boolean {
        if (!isPermissionsAllowedCamera()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.CAMERA
                )
            ) {
                showPermissionDeniedDialog()
            }
            else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CODE_CAMERA
                )
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here
                    //capturePhoto()
                    mediaInterface?.onCameraClick()
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    askForPermissionsCamera()
                }
            }
            REQUEST_CODE_Gallery -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here
                    //openGalleryForImage()
                    mediaInterface?.onGalleryClick()
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    askForPermissions_media()
                }
                return
            }
            REQUEST_CODE_DOCUMENT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here
                    //openGalleryForImage()
                    mediaInterface?.onDocumentClick()
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    askForPermissionsDoc()
                }
                return
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(context)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton(
                "App Settings"
            ) { dialogInterface, i ->
                // send to app settings if permission is denied permanently
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    interface OptionClickListener {
        fun onOptionClick(item: String)
    }

    companion object {
        private lateinit var mListener: OptionClickListener

        @JvmStatic
        fun newInstance(
            bundle: Bundle,
            mListener: OptionClickListener
        ): BottomSheetOptionForDocumentUploadFragment {
            val fragment = BottomSheetOptionForDocumentUploadFragment()
            fragment.arguments = bundle
            Companion.mListener = mListener
            return fragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CAMERA && data != null) {
            // imageView.setImageBitmap(data.extras.get("data") as Bitmap)
            Log.d("Image captured", "onActivityResult: " + data.extras!!.get("data").toString())
            val takenPhoto = BitmapFactory.decodeFile(filePhoto.absolutePath)
            filePath = filePhoto.absolutePath
            uploadDocument()
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_Gallery && data != null) {
            // imageView.setImageBitmap(data.extras.get("data") as Bitmap)
            Log.d(
                "Image captured gallary",
                "onActivityResult: " + data.extras!!.get("data").toString()
            )
            val imagePath = getRealPathFromURI(data.data)//data?.data?.path!!
            if (imagePath != null) {
                filePath = imagePath
                uploadDocument()
            }
        }
    }

    private fun uploadDocument() {
        Log.d("uploadDocument", filePath)
        val file: File?
        var filePart: MultipartBody.Part? = null
        if (filePath != "") {
            file = File(filePath)
            filePart = MultipartBody.Part.createFormData(
                "my_doc",
                file.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), file)
            )
        }
    }

    private fun getRealPathFromURI(contentURI: Uri?): String? {
        val result: String?
        val cursor: Cursor? =
            contentURI?.let { activity?.contentResolver?.query(it, null, null, null, null) }
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