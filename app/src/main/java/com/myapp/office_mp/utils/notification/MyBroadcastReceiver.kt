package com.myapp.office_mp.utils.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, PushNotificationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}

