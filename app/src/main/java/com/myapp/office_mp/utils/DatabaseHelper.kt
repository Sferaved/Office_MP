package com.myapp.office_mp.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = "CREATE TABLE $TABLE_NAME ($COLUMN_PASSWORD TEXT PRIMARY KEY, $COLUMN_USERNAME TEXT, $COLUMN_VERIFIED INTEGER DEFAULT 0)"
        db.execSQL(createTableSQL)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Обновление базы данных при необходимости
    }

    fun insertData(password: String, username: String): Long {
        val contentValues = ContentValues()
        contentValues.put(COLUMN_PASSWORD, password)
        contentValues.put(COLUMN_USERNAME, username)
        val db = this.writableDatabase
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun getUsername(password: String): String? {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_USERNAME FROM $TABLE_NAME WHERE $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(password))
        var username: String? = null
        if (cursor.moveToFirst()) {
            username = cursor.getString(0)
        }
        cursor.close()
        return username
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "PasswordDB"
        private const val TABLE_NAME = "Passwords"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_VERIFIED = "verified"
    }
    fun setVerified(password: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_VERIFIED, 1)
        }
        db.update(TABLE_NAME, contentValues, "$COLUMN_PASSWORD = ?", arrayOf(password))
    }

}
