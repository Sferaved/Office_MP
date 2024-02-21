package com.myapp.office_mp.utils.notification

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("NotificationWorker", "Work executed")

        // Здесь можно выполнять необходимые действия, например, отправлять уведомление
        val context = applicationContext

        val intent = Intent(context, NotificationReceiver::class.java)

        // Отправляем широковещательное намерение
        context.sendBroadcast(intent)

        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "notification_work"

        fun enqueueWork(context: Context, notificationTime: Triple<Int, Int, Int>) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                1, // Периодичность (дни)
                TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(notificationTime), TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }

        private fun calculateInitialDelay(notificationTime: Triple<Int, Int, Int>): Long {
            val currentTime = Calendar.getInstance()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, notificationTime.first)
                set(Calendar.MINUTE, notificationTime.second)
                set(Calendar.SECOND, notificationTime.third)
            }

            var initialDelay = calendar.timeInMillis - currentTime.timeInMillis
            if (initialDelay < 0) {
                // Если установленное время уже прошло, добавляем один день
                initialDelay += TimeUnit.DAYS.toMillis(1)
            }
            return initialDelay
        }
    }
}
