package com.myapp.office_mp.utils.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.myapp.office_mp.utils.db.DatabaseHelper
import java.util.Calendar

class PushNotificationService : Service() {

    private val TAG = "PushNotificationService"
    private lateinit var context: Context

    private val intervalMillis: Long = AlarmManager.INTERVAL_DAY // Раз в день
    private val dbHelper by lazy { DatabaseHelper(context) }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
        context = applicationContext
        createNotificationChannel()
        scheduleNotificationUpdate()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")

        super.onDestroy()
        stopForeground(true)
    }

    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel: ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Office MP"
            val descriptionText = "Описание канала уведомлений"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("channelId", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun scheduleNotificationUpdate() {
        Log.d(TAG, "scheduleNotificationUpdate: ")
        val notificationTime = dbHelper.getNotificationTime() ?: Triple(8, 0, 0) // Время по умолчанию 8:00
        Log.d(TAG, "Notification time: ${notificationTime.first}:${notificationTime.second}:${notificationTime.third}")

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Получение текущего времени и установка времени срабатывания
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, notificationTime.first)
        calendar.set(Calendar.MINUTE, notificationTime.second)
        calendar.set(Calendar.SECOND, notificationTime.third)

        // Если время уведомления уже прошло, устанавливаем его на следующий день
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }



        val intent = Intent(context, NotificationReceiver::class.java)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
////            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
////            alarmManager.setRepeating(
////                AlarmManager.RTC_WAKEUP,
////                calendar.timeInMillis,
////                intervalMillis,
////                pendingIntent
////            )
//            NotificationWorker.enqueueWork(context, notificationTime)
//        }
//        else {
//            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT )
//            alarmManager.setRepeating(
//                AlarmManager.RTC_WAKEUP,
//                calendar.timeInMillis,
//                intervalMillis,
//                pendingIntent
//            )
//        }
        NotificationWorker.enqueueWork(context, notificationTime)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind: ")
        return null
    }
}
