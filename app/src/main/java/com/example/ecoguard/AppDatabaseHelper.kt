package com.example.ecoguard

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "AppDatabase.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS app_flags (id INTEGER PRIMARY KEY, onboarding_shown INTEGER)")
        val values = ContentValues().apply {
            put("id", 1)
            put("onboarding_shown", 0)
        }
        db.insert("app_flags", null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS app_flags")
        onCreate(db)
    }

    fun isOnboardingShown(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT onboarding_shown FROM app_flags WHERE id = 1", null)
        var result = false
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0) == 1
        }
        cursor.close()
        return result
    }

    fun setOnboardingShown() {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("onboarding_shown", 1)
        }
        db.update("app_flags", values, "id = 1", null)
    }
}
