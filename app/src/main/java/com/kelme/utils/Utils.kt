package com.kelme.utils

import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.ParseException
import android.os.Build
import android.text.format.DateFormat
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.kelme.R
import com.kelme.model.VoipNotificationResponse
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Amit on 28,June,2021
 */
object Utils {

    fun hideKeyboard(activity: Activity) {
        val view = (activity.findViewById<View>(R.id.content) as ViewGroup).getChildAt(0)
        hideKeyboard(activity, view)
    }

    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun enableBackButton(activity: Activity, backButton: View) {
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener { activity.onBackPressed() }
    }


    fun showSnackBar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

//    fun showToast(context: Context?, message: String?) {
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//    }
//
//    fun showToast(context: Context?, @StringRes message: Int) {
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//    }

    fun encodeString(string: String): String? {
        val data = string.toByteArray(StandardCharsets.UTF_8)
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    fun decodeString(string: String?): String? {
        return try {
            val data = Base64.decode(string, Base64.DEFAULT)
            String(data, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            string
        }
    }

    fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
//         else {
//            connectivityManager.activeNetworkInfo?.run {
//                return when(type) {
//                    ConnectivityManager.TYPE_WIFI -> true
//                    ConnectivityManager.TYPE_MOBILE -> true
//                    ConnectivityManager.TYPE_ETHERNET -> true
//                    else -> false
//                }
//            }
//        }
        return false
    }
   /* fun loadImage(context: Context, imageView: ImageView?, imageLink: String) {
        Glide.with(context)
            .asBitmap()
            .placeholder(R.drawable.ic_loading)
            .error(R.drawable.alerts)
            .load(imageLink)
            .into(imageView!!)
    }*/

    fun loadImage(context: Context, imageView: ImageView?, imageLink: String)
    {
        Glide.with(context)
            .asBitmap()
            .error(R.drawable.alerts)
            .load(imageLink)
            .into(imageView!!)
    }
    fun loadImageWithDrawable(context: Context, imageView: ImageView?, drawable: Int) {
        Glide.with(context)
            .asBitmap()
            .placeholder(R.drawable.ic_loading)
            .error(R.drawable.alerts)
            .load(drawable)
            .into(imageView!!)
    }

    fun convertTimeStampToDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format("dd-MM-yyyy", calendar).toString()
    }

    fun convertTimeStampToDateNotification(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString()
    }

    class TimeAgo2 {
        fun covertTimeToText(dataDate: String?): String? {
            var convTime: String? = null
            val prefix = ""
            val suffix = "Ago"
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val pasTime: Date = dateFormat.parse(dataDate)
                val nowTime = Date()
                val dateDiff = nowTime.time - pasTime.time
                val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
                val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
                val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
                val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
                if (second < 60) {
                    convTime = "$second Seconds $suffix"
                } else if (minute < 60) {
                    convTime = "$minute Minutes $suffix"
                } else if (hour < 24) {
                    convTime = "$hour Hours $suffix"
                } else if (day >= 7) {
                    convTime = if (day > 360) {
                        (day / 360).toString() + " Years " + suffix
                    } else if (day > 30) {
                        (day / 30).toString() + " Months " + suffix
                    } else {
                        (day / 7).toString() + " Week " + suffix
                    }
                } else if (day < 7) {
                    convTime = "$day Days $suffix"
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                Log.e("ConvTimeE", e.response)
            }
            return convTime
        }
    }

    fun convertTimeStampToDate(timestamp: Long, format: String): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format(format, calendar).toString()
    }

    fun getAddressFromLatLng(activity: Activity, latLng: LatLng):String {
        try {
            val geoCoder: Geocoder = Geocoder(activity)
            val addresses: List<Address> =
                geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                val address = addresses[0].getAddressLine(0)
                Log.e("gecoderAddress->", address)
                return address
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getAddressFromLatLngDashBoard(activity: Activity, latLng: LatLng):String {
        try {
            val geoCoder: Geocoder = Geocoder(activity)
            val addresses: List<Address> =
                geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                val address = addresses[0].locality
                Log.e("gecoderAddress->", address)
                return address
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getAddressFromLatLngAsAddress(activity: Activity, latLng: LatLng):Address? {
        try {
            val geoCoder: Geocoder = Geocoder(activity)
            val addresses: List<Address> =
                geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                //Log.e("gecoderAddress->", address.toString())
                return addresses[0]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getReceiverId(firebaseModel: VoipNotificationResponse?): String {
        val uid = PrefManager.read(PrefManager.FCM_USER_ID, "")
        return if (uid == firebaseModel!!.receiver_id) {
            firebaseModel.sender_id
        } else {
            firebaseModel.receiver_id
        }
    }
}