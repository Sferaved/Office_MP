package com.myapp.office_mp.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.myapp.office_mp.MainActivity
import com.myapp.office_mp.R
import com.myapp.office_mp.utils.db.DatabaseHelper

class MyPeriodicWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Ваши действия, выполняемые при каждом запуске задачи

        // Получение контекста
        val context = applicationContext

        // Ваши действия для установки уведомления
        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE)

        val CHANNEL_ID = "my_channel_id" // Исправление 1: Присваивание CHANNEL_ID строковому значению
        val NOTIFICATION_ID = 12345 // Исправление 2: Присваивание числового значения NOTIFICATION_ID

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.bullet_2157465)
            .setContentTitle("Напоминание")
            .setContentText("Нажми на это сообщение чтобы открыть программу")
            .setContentIntent(pendingIntent) // Использование PendingIntent.getActivity()
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

        Log.d("MyPeriodicWorker", "doWork ")
        val dbHelper = DatabaseHelper(context)
        dbHelper.updateNotificationTimeOneDay(context)
        // Возвращаем Result.success() при успешном выполнении работы
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val channelId = "my_channel_id"
        val channelName = "My Channel"
        val channelDescription = "Description of My Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }



}
