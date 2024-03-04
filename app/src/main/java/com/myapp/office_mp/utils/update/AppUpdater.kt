package com.myapp.office_mp.utils.update

import android.app.Activity
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.OnSuccessListener

class AppUpdater(private val activity: Activity) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var onUpdateListener: OnUpdateListener? = null

    fun setOnUpdateListener(onUpdateListener: OnUpdateListener) {
        this.onUpdateListener = onUpdateListener
    }

    fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener(OnSuccessListener<AppUpdateInfo> { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    // Гибкое обновление
                    startAppUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE)
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    // Немедленное обновление
                    startAppUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                }
            }else {
                // Если новая версия не доступна, показываем Toast
                Toast.makeText(activity, "Нет новой версии приложения для обновления", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startAppUpdate(appUpdateInfo: AppUpdateInfo, updateType: Int) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, updateType, activity, MY_REQUEST_CODE)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Слушатель для обновления состояния установки
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // Обновление загружено
            // Вы можете предложить пользователю перезапустить приложение
            appUpdateManager.completeUpdate()
            onUpdateListener?.onUpdateCompleted()
        }
    }

    fun registerListener() {
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    fun unregisterListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    interface OnUpdateListener {
        fun onUpdateCompleted()
    }

    companion object {
        private const val MY_REQUEST_CODE = 100
    }
}
