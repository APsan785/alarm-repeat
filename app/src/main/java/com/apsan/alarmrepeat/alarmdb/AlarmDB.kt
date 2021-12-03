package com.apsan.alarmrepeat.alarmdb

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList


class AlarmDB(
    context: Context
) : SQLiteOpenHelper(context, DB_NAME, null, 1) {

    val TAG = "tag"

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.execSQL(
                "create table $TABLE_NAME (" +
                        "_id integer primary key autoincrement," +
                        "alarm_start text," +
                        "alarm_end text," +
                        "max_trig integer," +
                        "current_trig integer," +
                        "status int);"
            )
        }
    }

    fun addEntries(time: Long, no_trigger: Int, db: SQLiteDatabase) {

        var alarm_start: String? = null
        var alarm_end: String? = null
        val calendar1 = Calendar.getInstance().apply {
            timeInMillis = time
        }

        alarm_start =
            "${addZeroes(calendar1.get(Calendar.HOUR))}:${addZeroes(calendar1.get(Calendar.MINUTE))} "

        alarm_start += if (calendar1.get(Calendar.AM_PM) == 0) "AM" else "PM"
        val calendar2 = Calendar.getInstance().apply {
            timeInMillis = time + ((no_trigger-1) * 3600 * 1000)
        }
        alarm_end =
            "${addZeroes(calendar2.get(Calendar.HOUR))}:${addZeroes(calendar2.get(Calendar.MINUTE))} "
        alarm_end += if (calendar2.get(Calendar.AM_PM) == 0) "AM" else "PM"

        val cv = ContentValues()
        cv.put("alarm_start", alarm_start)
        cv.put("alarm_end", alarm_end)
        cv.put("max_trig", no_trigger)
        cv.put("current_trig", 0)
        cv.put("status", AlarmModel.STATUS_RUNNING)
        db.insert(TABLE_NAME, null, cv)
    }

    fun increase_trig(db: SQLiteDatabase) {
        db.execSQL("update $TABLE_NAME set current_trig = current_trig+1 where _id=  (select max(_id) from Alarm_Table);")
        Log.d(TAG, "increase_trig: Increased")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    companion object {
        const val DB_NAME = "Alarm_Database"
        const val TABLE_NAME = "Alarm_Table"
    }

    fun addZeroes(input: Int): String {

        if (input in 0..9) {
            return "0$input"
        }
        return "$input"
    }

    fun changeStatus(db: SQLiteDatabase, newStatus:Int) {
        db.execSQL("update $TABLE_NAME set status = $newStatus where _id = (select max(_id) from $TABLE_NAME)")
        Log.d(TAG, "changeStatus: Status Updated to Done")
    }

    fun getAlarms(db: SQLiteDatabase): ArrayList<AlarmModel> {
        val result = ArrayList<AlarmModel>()
        val cursor = db.rawQuery("select * from $TABLE_NAME", null)
         while(cursor.moveToNext()) {
            result.add(
                AlarmModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5)
                )
            )
        }
        cursor.close()
        return result
    }
}