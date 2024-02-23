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
//    private val _timeState = MutableStateFlow(TimeState())
//    val timeState: StateFlow<TimeState> = _timeState.asStateFlow()
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
    private fun startServiceIfNotRunning(
        context: Context
    ) {

        // Проверяем статус сервиса
//        val currentState = getCurrentState()
        val serviceIntent = Intent(context, PushNotificationService::class.java)

        context.stopService(serviceIntent)

//        serviceIntent.putExtra("hour", currentState.first)
//        serviceIntent.putExtra("minute", currentState.second)
//        serviceIntent.putExtra("second", currentState.third)

        context.startService(serviceIntent)

        Log.d("OfficeMPAppViewModel", "startServiceIfNotRunning")
    }

//    fun updateState(newTime: Triple<Int, Int, Int>) {
//        _timeState.update { currentState ->
//            val updatedState = currentState.copy(
//                hour = newTime.first,
//                minute = newTime.second,
//                second = newTime.third
//            )
//            Log.d("TAG", "updateState: Time updated: ${newTime.first}:${newTime.second}:${newTime.third}")
//            Log.d("TAG", "updateState: Updated state: ${updatedState.hour}:${updatedState.minute}:${updatedState.second}")
//            updatedState
//        }
//    }
//
//
//
//    fun getCurrentState(): Triple<Int, Int, Int> {
//        val currentState = _timeState.value // Получаем текущее состояние
//        Log.d("TAG", "getCurrentState: " + "Time updated: ${currentState.hour}:${currentState.minute}:${ currentState.second}")
//        return Triple(currentState.hour, currentState.minute, currentState.second)
//    }

}

