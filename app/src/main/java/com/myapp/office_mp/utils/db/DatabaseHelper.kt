package com.myapp.office_mp.utils.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "notification_database"
        private const val TABLE_NOTIFICATION_TIMES = "notification_times"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE $TABLE_NOTIFICATION_TIMES ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_TIMESTAMP INTEGER)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATION_TIMES")
        onCreate(db)
    }

    fun addOrUpdateNotificationTime(timestamp: Long): Boolean {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NOTIFICATION_TIMES", null)

        return if (cursor.moveToFirst()) {
            // Если запись уже есть, обновляем её
            val values = ContentValues()
            values.put(COLUMN_TIMESTAMP, timestamp)
            val updatedRows = db.update(TABLE_NOTIFICATION_TIMES, values, "$COLUMN_ID = ?", arrayOf(cursor.getString(0)))
            cursor.close()
            db.close()
            updatedRows > 0
        } else {
            // Если запись отсутствует, добавляем новую
            val values = ContentValues()
            values.put(COLUMN_TIMESTAMP, timestamp)
            db.insert(TABLE_NOTIFICATION_TIMES, null, values)
            cursor.close()
            db.close()
            true
        }
    }

    @SuppressLint("Range")
    fun getFirstNotificationTime(): Long? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NOTIFICATION_TIMES", null)

        if (cursor.moveToFirst()) {
            val timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP))
            cursor.close()
            db.close()
            return timestamp
        }

        cursor.close()
        db.close()
        return null
    }

    fun updateNotificationTimeOneDay(context: Context) {
        val dbHelper = DatabaseHelper(context)
        val notificationTimeMillis = dbHelper.getFirstNotificationTime()

        if (notificationTimeMillis != null && notificationTimeMillis > 0) {
            val currentCalendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val notificationCalendar = Calendar.getInstance().apply {
                timeInMillis = notificationTimeMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (currentCalendar == notificationCalendar) {
                val newNotificationTimeMillis = notificationTimeMillis + TimeUnit.DAYS.toMillis(1)

                // Обновляем запись времени в базе данных
                dbHelper.addOrUpdateNotificationTime(newNotificationTimeMillis)

                // Выводим обновленное время в лог
                val notificationTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(newNotificationTimeMillis))
                Log.d("MyService", "Updated notification time: $notificationTime")
            } else {
                Log.d("MyService", "Current date does not match notification date")
            }
        } else {
            Log.d("MyService", "No notification time found in the database")
        }
    }


    fun updateNotificationCurrentTimeOneDay(context: Context) {
        val dbHelper = DatabaseHelper(context)
        val currentTimeMillis = System.currentTimeMillis()
        val notificationTimeMillis = dbHelper.getFirstNotificationTime()

        if (notificationTimeMillis != null && notificationTimeMillis > 0) {
            // Проверяем, что текущее время меньше, чем время уведомления
            if (currentTimeMillis > notificationTimeMillis) {
                val newNotificationTimeMillis = notificationTimeMillis + TimeUnit.DAYS.toMillis(1)

                // Обновляем запись времени в базе данных
                dbHelper.addOrUpdateNotificationTime(newNotificationTimeMillis)

                // Выводим обновленное время в лог
                val notificationTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(newNotificationTimeMillis))
                Log.d("MyService", "Updated notification time: $notificationTime")
            } else {
                Log.d("MyService", "Current time is after the notification time")
            }
        } else {
            Log.d("MyService", "No notification time found in the database")
        }
    }

}

