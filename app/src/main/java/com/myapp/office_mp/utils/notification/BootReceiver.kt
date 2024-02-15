package com.myapp.office_mp.utils.notification

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            if (!isServiceRunning(context)) {
                val serviceIntent = Intent(context, MyService::class.java)
                context.startService(serviceIntent)
            }
        }
    }

    private fun isServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (MyService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
