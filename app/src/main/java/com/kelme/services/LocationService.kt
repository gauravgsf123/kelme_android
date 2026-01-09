package com.kelme.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.kelme.R
import com.kelme.model.request.CurrentLocationRequest
import com.kelme.network.RetrofitInstance
import com.kelme.utils.PrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class LocationService : Service() {
    private val NOTIFICATION_CHANNEL_ID = "my_notification_location"
    private var mPrevLocation = Location("provider")
    val locationHelper = LocationHelper()

    private val mBinder: IBinder = MyBinder()

    inner class MyBinder : Binder() {
        // Return this instance of MyService so clients can call public methods
        val service: LocationService
            get() =// Return this instance of MyService so clients can call public methods
                this@LocationService
    }

    companion object {
        var mLocation: Location? = null
    }

    override fun onCreate() {
        super.onCreate()
        mPrevLocation.latitude = 0.0
        mPrevLocation.longitude = 0.0

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(false)
                .setSmallIcon(R.drawable.kelme_app_logo)
                .setContentTitle("Location Service")
                .setContentText("Location service is running in background")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = NOTIFICATION_CHANNEL_ID
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            startForeground(
                1,
                builder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29-33
            startForeground(
                1,
                builder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            );
        } else { // API < 29
            startForeground(1, builder.build());
        }
        /*if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ){
            startForeground(1, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, builder.build())
        }*/


        Log.d("isRunning","in OnCreate")
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("isRunning","in onStartCommand")
        locationHelper.startListeningUserLocation(this, object : MyLocationListener {
            override fun onLocationChanged(location: Location?) {
                if (location == null) {
                    Log.e("LocationError", "Received null location")
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    mLocation = location
                    Log.d("LocationUpdate", "Lat: ${location.latitude}, Lng: ${location.longitude}")

                    PrefManager.write(PrefManager.LATITUDE, location.latitude.toString())
                    PrefManager.write(PrefManager.LONGITUDE, location.longitude.toString())

                    if (PrefManager.read(PrefManager.IS_LOGIN, false)) {
                        val request = CurrentLocationRequest(
                            location.latitude.toString(),
                            location.longitude.toString(),
                            System.currentTimeMillis().toString()
                        )
                        trackUser(request)
                    } else {
                        locationHelper.stopLocationUpdates()
                        stopSelf()
                    }
                }
            }
        })
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Called when app is swiped from recents or process killed
        stopForeground(true) // remove notification
        stopSelf() // stop the service
        super.onTaskRemoved(rootIntent)
    }

    suspend fun trackUser(request: CurrentLocationRequest) =
        RetrofitInstance.apiService?.trackUser(request)

    /*override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }*/

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopLocationUpdates()
    }
}