package com.android.example.sleepsamplekotlin.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.example.sleepsamplekotlin.MainActivity
import com.android.example.sleepsamplekotlin.R
import com.android.example.sleepsamplekotlin.receiver.SleepReceiver
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest

class IntruderService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    val CHANNEL_ID = "1000"
    val CHANNEL_NAME = "Intruder Protection Service"
    val TAG = "DEBUG"

    private lateinit var sleepPendingIntent: PendingIntent

    private fun subscribeToSleepSegmentUpdates(context: Context, pendingIntent: PendingIntent) {
        Log.d(TAG, "requestSleepSegmentUpdates()")
        val task = ActivityRecognition.getClient(context).requestSleepSegmentUpdates(
            pendingIntent,
            // Registers for both [SleepSegmentEvent] and [SleepClassifyEvent] data.
            SleepSegmentRequest.getDefaultSleepSegmentRequest()
        )

        task.addOnSuccessListener {
//            mainViewModel.updateSubscribedToSleepData(true)
            Log.d(TAG, "Successfully subscribed to sleep data.")
        }
        task.addOnFailureListener { exception ->
            Log.d(TAG, "Exception when subscribing to sleep data: $exception")
        }
    }

    private fun unsubscribeToSleepSegmentUpdates(context: Context, pendingIntent: PendingIntent) {
        Log.d(TAG, "unsubscribeToSleepSegmentUpdates()")
        val task = ActivityRecognition.getClient(context).removeSleepSegmentUpdates(pendingIntent)

        task.addOnSuccessListener {
//            mainViewModel.updateSubscribedToSleepData(false)
            Log.d(TAG, "Successfully unsubscribed to sleep data.")
        }
        task.addOnFailureListener { exception ->
            Log.d(TAG, "Exception when unsubscribing to sleep data: $exception")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for intruder protection service"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        // Create notification channel before building notification
        createNotificationChannel()

        sleepPendingIntent =
            SleepReceiver.createSleepReceiverPendingIntent(context = applicationContext)
        // Do one-time setup here if needed
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure notification channel exists (safety check)
        createNotificationChannel()
        
        // Start in foreground IMMEDIATELY - must be called within 5 seconds of startForegroundService()
        // Build a simple notification first to ensure we meet the deadline
        val simpleNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Intruder protection active")
            .setContentText("Service Montoring Sleep Segment Events")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // For Android 12+ (API 31+), we need to specify the foreground service type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startForeground(
                NOTIFICATION_ID,
                simpleNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, simpleNotification)
        }
        Log.d(TAG, "Foreground service started successfully")

        subscribeToSleepSegmentUpdates(applicationContext, sleepPendingIntent)

        // If system kills the service, recreate it with a null intent
        return START_STICKY
    }

    override fun onDestroy() {

        super.onDestroy()

        unsubscribeToSleepSegmentUpdates(applicationContext, sleepPendingIntent)

    }

    companion object {

        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {

            val intent = Intent(context, IntruderService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                context.startForegroundService(intent)

            } else {

                context.startService(intent)

            }

        }

        fun stop(context: Context) {

            val intent = Intent(context, IntruderService::class.java)

            context.stopService(intent)

        }

    }

}
