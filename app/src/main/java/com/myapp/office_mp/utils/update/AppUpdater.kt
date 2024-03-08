package com.myapp.office_mp.utils.update

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.OnSuccessListener

class AppUpdater(private val activity: Activity, private val context: Context) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var onUpdateListener: OnUpdateListener? = null

    fun setOnUpdateListener(onUpdateListener: OnUpdateListener) {
        this.onUpdateListener = onUpdateListener
    }

    fun checkForUpdate() {
        Log.d("TAG", "checkForUpdate: ")
        appUpdateManager.appUpdateInfo.addOnSuccessListener(OnSuccessListener<AppUpdateInfo> { appUpdateInfo ->
            try {
                val updateAvailability = appUpdateInfo.updateAvailability()
                Log.d("TAG", "checkForUpdate3: $updateAvailability")
                if (updateAvailability != null && updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        // Гибкое обновление
                        startAppUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE)
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        // Немедленное обновление
                        startAppUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
                    }
                } else {
                    // Если новая версия не доступна, показываем Toast
                    val message = "Нет новой версии приложения для обновления"
                    context?.let {
                        Handler(it.mainLooper).post {
                            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                val message = "Нет новой версии приложения для обновления"
                context?.let {
                    Handler(it.mainLooper).post {
                        Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                    }
                }
                // Обработка исключения
                Log.e("TAG", "Exception while checking for update: $e")
                e.printStackTrace()
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
