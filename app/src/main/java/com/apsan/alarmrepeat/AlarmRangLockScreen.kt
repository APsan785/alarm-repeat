package com.apsan.alarmrepeat

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.apsan.alarmrepeat.broadcasts.AlarmReceiver
import com.apsan.alarmrepeat.databinding.ActivityAlarmRangLockScreenBinding
import com.hoc081098.viewbindingdelegate.viewBinding


class AlarmRangLockScreen : AppCompatActivity() {

    private val binding by viewBinding(ActivityAlarmRangLockScreenBinding::bind)
    val TAG = "tag"
    private lateinit var ringtone: Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_alarm_rang_lock_screen)
        supportActionBar?.hide()
        Log.d(TAG, "onCreate: Activity created")
        super.onCreate(savedInstanceState)
        showWhenLockedAndTurnScreenOn()

        var alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        ringtone = RingtoneManager.getRingtone(this, alarmUri)
        Log.d(TAG, "onStartCommand: Play Ringtone")
        ringtone.play()

        binding.stopAlarmbtn.setOnClickListener {
            NotificationManagerCompat.from(this).cancel(NotificationIDS.ALARM_RANG_ID)
            ringtone.stop()

            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(homeIntent)
            finish()
        }
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                        or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )

        }
        with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestDismissKeyguard(this@AlarmRangLockScreen, null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationManagerCompat.from(this).cancel(NotificationIDS.ALARM_RANG_ID)
        ringtone.stop()

    }
}