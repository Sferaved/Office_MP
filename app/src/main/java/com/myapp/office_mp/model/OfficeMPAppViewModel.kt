package com.myapp.office_mp.model

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.myapp.office_mp.utils.db.DatabaseHelper
import com.myapp.office_mp.utils.notification.MyService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class OfficeMPAppViewModel : ViewModel() {
    fun setTimeToPush(context: Context) {

        val dbHelper = DatabaseHelper(context)
        val notificationTimeMillis = dbHelper.getFirstNotificationTime()

        if (notificationTimeMillis != null && notificationTimeMillis > 0) {
            val notificationTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(notificationTimeMillis))
            Log.d("MyService", "First notification time: $notificationTime")
        } else {
            Log.d("MyService", "No notification time found in the database")
        }


        if(notificationTimeMillis == null) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
            }

            val triggerTimeMillis = calendar.timeInMillis

            dbHelper.addOrUpdateNotificationTime(triggerTimeMillis)
        }

        dbHelper.updateNotificationCurrentTimeOneDay(context)

        startServiceIfNotRunning(context)
    }

    private fun startServiceIfNotRunning(context: Context) {
        // Проверяем статус сервиса
        val serviceIntent = Intent(context, MyService::class.java)
        if (!isServiceRunning(context, MyService::class.java)) {
            // Если сервис не запущен, запускаем его
            context.startService(serviceIntent)
        }
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

}

