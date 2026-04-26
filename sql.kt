package com.example.lab

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyHelper(c: Context) : SQLiteOpenHelper(c, "FinalDB", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) { db?.execSQL("CREATE TABLE Info(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, dept TEXT)") }
    override fun onUpgrade(db: SQLiteDatabase?, o: Int, n: Int) {}
    fun insert(n: String, d: String) = writableDatabase.insert("Info", null, ContentValues().apply { put("name", n); put("dept", d) })
    fun read() = readableDatabase.rawQuery("SELECT * FROM Info", null)
    fun update(id: Int, name: String, dept: String): Int {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("name", name)
        cv.put("dept", dept)
        return db.update("student", cv, "id=?", arrayOf(id.toString()))
    }

    fun delete(id: Int): Int {
        val db = writableDatabase
        return db.delete("student", "id=?", arrayOf(id.toString()))
    }
}
