package com.myapp.office_mp.utils.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.myapp.office_mp.utils.db.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MyService : Service() {

    private val dbHelper by lazy { DatabaseHelper(applicationContext) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyPeriodicWorker", "Work started")
        schedulePeriodicWork()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun schedulePeriodicWork() {
        val notificationTime = dbHelper.getFirstNotificationTime()
        val notificationTimeMillis =
            notificationTime?.let { Date(it) }?.let {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(it)
            }
        Log.d("MyPeriodicWorker", "First notification time: $notificationTimeMillis")

        if (notificationTime != null && notificationTime > 0) {
            val currentTime = System.currentTimeMillis()

            val timeToStartService = notificationTime - currentTime
            val not =
                currentTime?.let { Date(it) }?.let {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(it)
                }
            Log.d("MyPeriodicWorker", "not time: $not")
            Log.d("MyPeriodicWorker", "timeToStartService time: $timeToStartService")

            if (timeToStartService > 0) {
                val delay = timeToStartService
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()

                val periodicWorkRequest = PeriodicWorkRequest.Builder(
                    MyPeriodicWorker::class.java,
                    1, TimeUnit.DAYS
                )
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)
            } else {
                val dbHelper = DatabaseHelper(applicationContext)
                dbHelper.updateNotificationTimeOneDay(applicationContext)
            }
        }
    }
}

