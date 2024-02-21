package com.myapp.office_mp.utils.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.myapp.office_mp.MainActivity
import com.myapp.office_mp.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Received notification broadcast")

        // Создаем канал уведомлений (если он еще не был создан)
        createNotificationChannel(context)

        // Создание уведомления
        val notification = createNotification(context)

        // Отображение уведомления
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(context: Context): Notification {
        val channelId = "my_channel_id" // Используем тот же идентификатор канала
        val intent = Intent(context, MainActivity::class.java) // Замените MainActivity на вашу активность
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.bullet_2157465)
            .setContentTitle("Мобильный офис")
            .setContentText("Нажми для запуска программы")
            .setContentIntent(pendingIntent) // Добавляем намерение для запуска приложения при нажатии на уведомление
            .setAutoCancel(true) // Автоматическое закрытие уведомления при клике на него

        return builder.build()
    }


    companion object {
        private const val NOTIFICATION_ID = 123456 // Уникальный идентификатор уведомления
    }
}

