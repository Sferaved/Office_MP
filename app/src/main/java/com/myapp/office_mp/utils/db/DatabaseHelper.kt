package com.myapp.office_mp.utils.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "PushNotificationDB"
        private const val TABLE_NOTIFICATION_SCHEDULE = "notification_schedule"
        private const val KEY_ID = "id"
        private const val KEY_HOUR = "hour"
        private const val KEY_MINUTE = "minute"
        private const val KEY_SECOND = "second"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE $TABLE_NOTIFICATION_SCHEDULE($KEY_ID INTEGER PRIMARY KEY, $KEY_HOUR INTEGER, $KEY_MINUTE INTEGER, $KEY_SECOND INTEGER)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATION_SCHEDULE")
        onCreate(db)
    }

    fun updateNotificationTime(hour: Int, minute: Int, second: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_HOUR, hour)
            put(KEY_MINUTE, minute)
            put(KEY_SECOND, second)
        }

        if (getRowCount(db) == 0) {
            db.insert(TABLE_NOTIFICATION_SCHEDULE, null, values)
        } else {
            db.update(TABLE_NOTIFICATION_SCHEDULE, values, null, null)
        }
        db.close()
    }

    @SuppressLint("Range")
    fun getNotificationTime(): Triple<Int, Int, Int>? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NOTIFICATION_SCHEDULE"
        val cursor = db.rawQuery(query, null)
        var notificationTime: Triple<Int, Int, Int>? = null

        if (cursor.moveToFirst()) {
            val hour = cursor.getInt(cursor.getColumnIndex(KEY_HOUR))
            val minute = cursor.getInt(cursor.getColumnIndex(KEY_MINUTE))
            val second = cursor.getInt(cursor.getColumnIndex(KEY_SECOND))
            notificationTime = Triple(hour, minute, second)
        }

        cursor.close()
        db.close()
        return notificationTime
    }

    private fun getRowCount(db: SQLiteDatabase): Int {
        val query = "SELECT * FROM $TABLE_NOTIFICATION_SCHEDULE"
        val cursor = db.rawQuery(query, null)
        val count = cursor.count
        cursor.close()
        return count
    }
}


