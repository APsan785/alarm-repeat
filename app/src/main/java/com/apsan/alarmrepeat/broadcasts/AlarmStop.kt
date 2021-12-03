package com.apsan.alarmrepeat.broadcasts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apsan.alarmrepeat.alarmdb.AlarmDB
import com.apsan.alarmrepeat.alarmdb.AlarmModel


class AlarmStop:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val TAG="TAG"
        Log.d(TAG, "onReceive: Cancelling alarm")
        val pendingIntent: PendingIntent? = intent!!.getParcelableExtra("key")
        val am = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)
        val helper = AlarmDB(context)
        helper.changeStatus(helper.writableDatabase, AlarmModel.STATUS_DONE)
    }
}