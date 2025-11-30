package com.kelme.activity.login

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.kelme.R
import com.kelme.activity.dashboard.DashboardActivity
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityLoginBinding
import com.kelme.model.ContactModel
import com.kelme.model.request.LoginRequest
import com.kelme.utils.*
import com.kelme.utils.Constants.userList

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModal: LoginViewModal
    private lateinit var auth: FirebaseAuth

    private val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val PERMISSION_PUSH = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModal = ViewModelProvider(this, ViewModalFactory(application)).get(
            LoginViewModal::class.java)

        auth = Firebase.auth
//        auth = FirebaseAuth.getInstance()
//        val currentUser = auth.currentUser
//        currentUser?.let {
//            Log.e(TAG, ">>>>> user is logged in ")
//            //auth.signOut()
//        }
        if (!PermissionUtil.hasPermissions(this, *PERMISSIONS)) requestPermission(this,PERMISSIONS,Constants.PERMISSIONS_REQUEST_CODE)
        setUi()
        setObserver()
    }

    override fun initializerControl() {
        //TODO("Not yet implemented")
    }

    private fun setUi() {
        if (PrefManager.read(PrefManager.REMEMBER_LOGIN_DETAILS, false)) {
            binding.etEmail.setText(PrefManager.read(PrefManager.EMAIL, ""))
            binding.etPassword.setText(PrefManager.read(PrefManager.PASSWORD, ""))
            binding.cbRememberMe.isChecked = true
        }

        binding.ivPassword.setOnClickListener {
            val message=binding.etPassword.text.toString()
            if(message.trim().isNotEmpty()) {
                if (binding.etPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                    binding.ivPassword.setImageResource(R.drawable.show_password)
                    binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    binding.ivPassword.setImageResource(R.drawable.hide_password)
                    binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            //startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
            if (TextUtils.isEmpty(binding.etEmail.text)) {
                Utils.showSnackBar(binding.root, "Enter Email")
            } else if (!(Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString().trim()).matches())
            ) {
                Utils.showSnackBar(binding.root, "Enter Valid Email")
            } else if (TextUtils.isEmpty(binding.etPassword.text)) {
                Utils.showSnackBar(binding.root, "Enter Password")
            } else {
                login()
            }
        }

        binding.cbRememberMe.setOnCheckedChangeListener { _, b ->
            PrefManager.write(PrefManager.REMEMBER_LOGIN_DETAILS, b)
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }
    }

    private fun setObserver() {
        viewModal.user.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                        signInFirebaseAuth()
                        response.data?.auth_token?.let {
                            PrefManager.write(
                                PrefManager.AUTH_TOKEN,
                                it
                            )
                        }
                        response.data?.user_id?.let { PrefManager.write(PrefManager.USER_ID, it) }
                        response.data?.name?.let { PrefManager.write(PrefManager.NAME, it) }
                        response.data?.email?.let { PrefManager.write(PrefManager.EMAIL, it) }
                        response.data?.role?.let { PrefManager.write(PrefManager.USERROLE, it) }
                        response.data?.company_id?.let { PrefManager.write(PrefManager.COMPANYID, it) }
                        response.data?.phone_number?.let { PrefManager.write(PrefManager.PHONE, it) }
                        response.data?.image?.let { PrefManager.write(PrefManager.IMAGE, it) }
                        response.data?.country_id?.let {
                            PrefManager.write(
                                PrefManager.COUNTRY_ID,
                                it
                            )
                        }
                        response.data?.country_name?.let {
                            PrefManager.write(PrefManager.COUNTRY_NAME, it) }
                        PrefManager.write(
                            PrefManager.PASSWORD,
                            binding.etPassword.text.toString().trim()
                        )
                }
                is Resource.Loading -> {
                    ProgressDialog.showProgressBar(this)
                }
                is Resource.Error -> {
                    ProgressDialog.hideProgressBar()
                    if (response.message == "Your session has been expired, Please login again.") {
                        viewModal.logout()
                    }
                    Toast.makeText(
                        applicationContext,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun login() {
        val request = LoginRequest(
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString(),
            Constants.DEVICE_TYPE_ID,
            PrefManager.read(PrefManager.DEVICE_ID, ""),
            PrefManager.read(PrefManager.FCM_TOKEN, "")
        )
        viewModal.login(request)
    }

    private fun signInFirebaseAuth() {
        auth.signInWithEmailAndPassword(
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString()
        ).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                   // Log.d(TAG, "signInFirebaseAuth:success")
                    val user = auth.currentUser
                    user?.let {
                        val uid = user.uid
                       // Log.d(TAG, "signInFirebaseAuth uid: $uid")
                        PrefManager.write(PrefManager.FCM_USER_ID, uid)
                        ProgressDialog.hideProgressBar()
                        if (PrefManager.read(PrefManager.FCM_USER_ID, "").isNotBlank()) {
                            startActivity(
                                Intent(this@LoginActivity, DashboardActivity::class.java)
                                    .putExtra("mobile", binding.etEmail.text.toString())
                            )
                            PrefManager.write(PrefManager.IS_LOGIN, true)
                            finish()
                        } else {
                          //  Toast.makeText(this, "firebase login failed", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "signInFirebaseAuth: " + task.exception)
                        }
                    }
                    getTotalUserList()
//                    Toast.makeText(
//                        baseContext,
//                        "Firebase Authentication Success.",
//                        Toast.LENGTH_SHORT
//                    ).show()
                } else {
//                    Toast.makeText(
//                        baseContext,
//                        "Firebase Authentication failed." + task.exception,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    Log.d(TAG, "signInFirebaseAuth: " + task.exception)
                }
            }
    }

    private fun getTotalUserList() {
        val query = FirebaseDatabase.getInstance().getReference("users")

        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                if (dataSnapshot.exists()) {
                    val user = ContactModel()
                    for (userSnapshot in dataSnapshot.children) {
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
                        userList.add(user)
//                        Log.d(TAG, "onDataChange: " + userList.count())
//                        Log.d(TAG, "onDataChange: " + user.name)
//                        Log.d(TAG, "onDataChange: " + name)
                    }
                    Log.d(TAG, "onDataChange: $userList")
                    val gson = Gson()
                    val json = gson.toJson(userList)
                    val sharedPreferences = getSharedPreferences("USER",MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putString("Set", json)
                    editor.apply()
                } else {
                    Log.d(TAG, "onDataChange: no data found")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun loginFirebaseAuth() {

        auth.createUserWithEmailAndPassword(
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        val user = FirebaseAuth.getInstance().currentUser
                        Log.d(TAG, "loginFirebaseAuth: $user")
                        if (user == null) {
                            //User is not Logged in
                            signInFirebaseAuth()
                            Log.d(TAG, "loginFirebaseAuth: user is not logged in")
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "loginFirebaseAuth:failure", task.exception)
//                    Toast.makeText(baseContext, "Firebase Authentication failed." + task.exception,
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }
    }

    private fun requestPermission(activity: Activity, permissions:  Array<String>,requestCode:Int) {
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            requestCode
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
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            if (!PermissionUtil.hasPermissions(this, *PERMISSION_PUSH))
                requestPermission(this,PERMISSION_PUSH,Constants.REQUEST_CODE_PUSH_NOTIFICATION)
        } /*else if(requestCode == Constants.REQUEST_CODE_PUSH_NOTIFICATION && grantResults.isNotEmpty()){
            @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            if (!PermissionUtil.hasPermissions(this, *PERMISION_LOCATION))
                requestPermission(this,PERMISION_LOCATION,Constants.REQUEST_CODE_LOCATION_PERMISSION)
        }*/
    }
}

