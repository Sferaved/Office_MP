package com.myapp.office_mp.model

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.myapp.office_mp.utils.db.DatabaseHelper
import com.myapp.office_mp.utils.notification.PushNotificationService


class OfficeMPAppViewModel : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    fun setTimeToPush(context: Context) {

        val dbHelper = DatabaseHelper(context)
        val notificationTime = dbHelper.getNotificationTime()

        if (notificationTime == null) {
            dbHelper.updateNotificationTime(8, 30, 0)
            Log.d("OfficeMPAppViewModel", "First notification time: 08:30:00")
        } else {
            val (hour, minute, second) = notificationTime
            Log.d("OfficeMPAppViewModel", "Notification time found in the database: $hour:$minute:$second")
        }

        startServiceIfNotRunning(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startServiceIfNotRunning(context: Context) {
        // Проверяем статус сервиса
        val serviceIntent = Intent(context, PushNotificationService::class.java)

        context.stopService(serviceIntent)
        context.startService(serviceIntent)

        Log.d("OfficeMPAppViewModel", "startServiceIfNotRunning")
    }
}

