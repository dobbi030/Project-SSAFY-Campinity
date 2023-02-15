package com.ssafy.campinity.presentation.community.campsite

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationServices
import com.ssafy.campinity.domain.entity.community.UserLocation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService : Service() {

    private lateinit var locationClient: LocationClient
    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        // 노티피케이션 추가 시
        /*val notification =
            NotificationCompat.Builder(this, "location").setContentTitle("마이풋트립")
                .setContentText("위치 정보를 수집 중").setSmallIcon(R.drawable.ic_campinity)
                .setOngoing(true)*/

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


//        val timer = Timer()
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//
//                Log.d(TAG, "run: ")
//            }
//        }, (1000L * 60L * 1L), (1000L * 60L * 1L))

        // 15초 마다
        locationClient.getLocationUpdates(1500L).catch { exception ->
            exception.printStackTrace()
        }.onEach { location ->

            val intent = Intent("intent_action")
            intent.putExtra("test", UserLocation(location.latitude, location.longitude))
            //applicationContext.sendBroadcast(intent)
            val localBroadcastManager = LocalBroadcastManager.getInstance(this)
            localBroadcastManager.sendBroadcast(intent)

            /*CoroutineScope(Dispatchers.IO).launch {
                EventBus.post(Coordinates(location.latitude, location.longitude))
            }*/
            //notificationManager.notify(1, updateNotification.build())
        }.launchIn(
            serviceScope
        )

        //startForeground(1, notification.build())
    } // End of start

    private fun stop() {
        stopForeground(true)
        stopSelf()
    } // End of stop

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}