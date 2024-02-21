package com.myapp.office_mp.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

class MyApplication : Application() {

    private var _isAppInForeground = false

    var isAppInForeground: Boolean
        get() = _isAppInForeground
        set(value) {
            _isAppInForeground = value
            // Additional logic here if needed
        }

    override fun onCreate() {
        super.onCreate()

        // Registering activity lifecycle callback
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                // App is in foreground
                isAppInForeground = true
            }

            override fun onActivityPaused(activity: Activity) {
                // App goes to background
                isAppInForeground = false
            }

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
