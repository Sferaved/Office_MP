package com.myapp.office_mp.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

class MyApplication : Application() {

    private var isAppInForeground = false
    override fun onCreate() {
        super.onCreate()

        // Регистрация слушателя жизненного цикла активности
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                // Приложение активно в переднем плане
                isAppInForeground = true
            }

            override fun onActivityPaused(activity: Activity) {
                // Приложение ушло в фоновый режим
                isAppInForeground = false
            }

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    fun isAppInForeground(): Boolean {
        return isAppInForeground
    }

}
