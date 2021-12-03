package com.apsan.alarmrepeat.broadcasts

import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.apsan.alarmrepeat.AlarmRangLockScreen
import com.apsan.alarmrepeat.App
import com.apsan.alarmrepeat.NotificationIDS
import com.apsan.alarmrepeat.R
import com.apsan.alarmrepeat.alarmdb.AlarmDB
import com.apsan.alarmrepeat.service.RingtoneService
import kotlin.properties.Delegates


class AlarmReceiver : BroadcastReceiver() {
    val TAG = "TAG"
    var curr by Delegates.notNull<Int>()

    override fun onReceive(p0: Context?, p1: Intent?) {


        val helper = p0?.let { AlarmDB(it) }
        val read_db = helper?.readableDatabase
        val cursor = read_db!!.rawQuery(
            "select max_trig, current_trig from ${AlarmDB.TABLE_NAME} where _id =  (select max(_id) from Alarm_Table);",
            null
        )
        cursor.moveToFirst()
        val current = cursor.getInt(cursor.getColumnIndexOrThrow("current_trig"))
        val max = cursor.getInt(cursor.getColumnIndexOrThrow("max_trig"))
        if (current < max) {

            Log.d(TAG, "onReceive: Alarm Triggered")

            val vibrator = p0.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(4000)

            Toast.makeText(p0, "ALARM RANG", Toast.LENGTH_SHORT).show()

            if ((p0.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked) {
                // it is locked
                lockScreenIntent(p0)
                Log.d(TAG, "onReceive: LockScreenIntent")
            } else {
                //it is not locked
                startForegroundService(p0, Intent(p0, RingtoneService::class.java))

            }
            val write_db = helper.writableDatabase
            helper.increase_trig(write_db)
            Log.d(TAG, "onReceive: between increase and cancel")
            curr = cursor.getInt(
                cursor.getColumnIndexOrThrow("current_trig")
            )
            Log.d(TAG, "onReceive: curr = $curr")
            Log.d(TAG, "onReceive: current = $current")
            if(current==max-1){
                NotificationManagerCompat.from(p0).cancel(NotificationIDS.ALARM_SCHEDULED_ID)
                Log.d(TAG, "onReceive: Cancelled")
            }
        }

        cursor.close()
    }

    fun lockScreenIntent(context: Context) {
        val pending = PendingIntent.getActivity(
            context,
            0,
            Intent(context, AlarmRangLockScreen::class.java),
            0
        )

        val notif = NotificationCompat.Builder(context, App.ID)
            .setSmallIcon(R.drawable.alarm_notif)
            .setContentTitle("YOUR ALARM RANG ...")
            .setContentText("Drink your glass of water")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(pending, true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
        NotificationManagerCompat.from(context).notify(NotificationIDS.ALARM_RANG_ID, notif)
    }

}
