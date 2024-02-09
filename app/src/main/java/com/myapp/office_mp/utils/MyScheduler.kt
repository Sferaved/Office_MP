package com.myapp.office_mp.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class MyScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleTask(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MyBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        // Получение текущего времени
        val currentTime = Calendar.getInstance()

        // Создание объекта Calendar для 8 утра
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)

            // Если текущее время позже 8 утра, установите на следующий день
            if (currentTime.after(this)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Установка будильника на время из calendar
        val triggerAtMillis = calendar.timeInMillis

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}
