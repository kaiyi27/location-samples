package com.android.example.sleepsamplekotlin.services

import android.app.Notification
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

    override fun onCreate() {

        super.onCreate()

        sleepPendingIntent =
            SleepReceiver.createSleepReceiverPendingIntent(context = applicationContext)
        // Do one-time setup here if needed

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Build the ongoing notification

        val notification = buildNotification()

        // Start in foreground ASAP

        startForeground(NOTIFICATION_ID, notification, foregroundServiceType=ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        subscribeToSleepSegmentUpdates(applicationContext, sleepPendingIntent)

        // If system kills the service, recreate it with a null intent

        return START_STICKY

    }

    private fun buildNotification(): Notification {

        // Tap action -> open your main activity

        val pendingIntent = PendingIntent.getActivity(

            this,

            0,

            Intent(this, MainActivity::class.java),

            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        )

        return NotificationCompat.Builder(this, CHANNEL_ID)

            .setContentTitle("Intruder protection active")

            .setContentText("Monitoring intruder events. Tap for details.")

            .setSmallIcon(R.drawable.ic_launcher_foreground)

            .setContentIntent(pendingIntent)

            .setOngoing(true)                       // 🔴 Mark as ongoing

            .setCategory(Notification.CATEGORY_SERVICE)

            .setPriority(NotificationCompat.PRIORITY_LOW)

            .build()

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
