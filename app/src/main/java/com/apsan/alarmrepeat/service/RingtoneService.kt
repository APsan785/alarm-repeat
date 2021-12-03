package com.apsan.alarmrepeat.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apsan.alarmrepeat.App
import com.apsan.alarmrepeat.NotificationIDS
import com.apsan.alarmrepeat.R

class RingtoneService : Service() {

    val TAG = "tag"
    lateinit var ringtone: Ringtone

    companion object {
        val ACTION_DISMISS = "dismiss alarm"
        //val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null) {
            if (intent.action == ACTION_DISMISS) {
                //stopService(Intent(this, RingtoneService::class.java))
                stopSelf()
                ringtone.stop()
            }
        }

        val intent_ = Intent(this, RingtoneService::class.java)
        intent_.action = ACTION_DISMISS
        val pending = PendingIntent.getService(this, 0, intent_, 0)


        val notif = NotificationCompat.Builder(this, App.ID)
            .setSmallIcon(R.drawable.alarm_notif)
            .setContentTitle("YOUR ALARM RANG ...")
            .setContentText("Drink your glass of water")
            .addAction(R.drawable.ic_baseline_music_off_24, "TURN IT OFF", pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setColor(Color.RED)
            .setFullScreenIntent(PendingIntent.getActivity(this, 0, Intent(), 0), true)
            .build()

        Log.d(TAG, "displayNotification: Shown")

        startForeground(NotificationIDS.ALARM_RANG_ID, notif)

        var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        ringtone = RingtoneManager.getRingtone(this, alarmUri)
        Log.d(TAG, "onStartCommand: Play Ringtone")
        ringtone.play()


        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: stopping service")
        ringtone.stop()
        NotificationManagerCompat.from(this).cancel(NotificationIDS.ALARM_RANG_ID)
    }
}