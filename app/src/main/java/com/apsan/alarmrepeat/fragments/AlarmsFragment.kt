package com.apsan.alarmrepeat.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apsan.alarmrepeat.App
import com.apsan.alarmrepeat.NotificationIDS
import com.apsan.alarmrepeat.R
import com.apsan.alarmrepeat.alarmdb.AlarmDB
import com.apsan.alarmrepeat.alarmdb.AlarmModel
import com.apsan.alarmrepeat.broadcasts.AlarmReceiver
import com.apsan.alarmrepeat.broadcasts.AlarmStop
import com.apsan.alarmrepeat.databinding.FragmentAlarmBinding
import java.util.*


class AlarmsFragment : Fragment() {

    private val TAG = "TAG"
    private var time: Long? = null
    private lateinit var db: SQLiteDatabase
    private lateinit var binding: FragmentAlarmBinding
    private lateinit var helper:AlarmDB


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initAlarmBtn(){
        db = helper.writableDatabase

        val cursor = db.rawQuery(
            "select status from ${AlarmDB.TABLE_NAME} where _id = ( select max(_id) from ${AlarmDB.TABLE_NAME})",
            null
        )
        if (cursor.moveToFirst()) {
            binding.alarmBtn.isChecked = cursor.getInt(0) == AlarmModel.STATUS_RUNNING
        }
        cursor.close()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirstTimeDialog()

        helper = context?.let { AlarmDB(it) }!!
        initAlarmBtn()

        val alarmManag = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(activity, AlarmReceiver::class.java)
        val alarmIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                0
            )

        val cancelIntent = Intent(activity, AlarmStop::class.java).let {
            it.putExtra("key", alarmIntent)
            PendingIntent.getBroadcast(
                context,
                0,
                it,
                0
            )
        }

        binding.alarmBtn.setOnClickListener {
            if (binding.alarmBtn.isChecked) {

                scheduleAlarm().let {

                    val delay = binding.repeatTimePicker.value * 1000 * 60 * 60

                    time = it.timeInMillis - it.timeInMillis % 60000

                    if (System.currentTimeMillis() > time!!) {
                        time = time!! + 1000 * 60 * 60 * 24
                    }

                    alarmManag.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        time!!,
                        delay.toLong(),
                        alarmIntent
                    )
                    Log.d("TAG", "onCreate: Alarm scheduled")
                }
                if (time != null) {
                    helper.addEntries(time!!, binding.repeatNumberPicker.value, db)
                }
                val margin_for_stopping = 2 * 60 * 1000
                val stop_time =
                    (binding.repeatNumberPicker.value-1) * 60 * 60 * 1000 + margin_for_stopping
                stop_time.plus(time!!).let { it1 ->
                    alarmManag.set(
                        AlarmManager.RTC_WAKEUP,
                        it1, cancelIntent
                    )
                }
                val notif = NotificationCompat.Builder(requireContext(), App.ID)
                    .setSmallIcon(R.drawable.alarm_notif)
                    .setContentTitle("Alarm Scheduled")
                    .setContentText("For drinking water")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setColor(Color.RED)
                    .build()
                NotificationManagerCompat.from(requireContext()).notify(NotificationIDS.ALARM_SCHEDULED_ID, notif)

            } else {
                val alertdialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Do you want to cancel previous alarm ?")
                    .setConfirmText("Yes..")
                    .setCancelText("No!")
                    .setConfirmClickListener {
                        alarmManag.cancel(alarmIntent)
                        helper.changeStatus(db, AlarmModel.STATUS_CANCELLED)
                        NotificationManagerCompat.from(requireContext()).cancel(NotificationIDS.ALARM_SCHEDULED_ID)
                        Log.d(TAG, "onReceive: Cancelled")
                        it.cancel()
                    }
                    .setCancelClickListener {
                        it.cancel()
                        binding.alarmBtn.isChecked = true
                    }
                alertdialog.setOnKeyListener { dialog, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        binding.alarmBtn.isChecked = true
                        dialog.dismiss();
                    }
                    true;
                }
                alertdialog.show()


            }
        }
        binding.showalarmsbtn.setOnClickListener {
            showAlarms()
        }
        binding.infoBtn.setOnClickListener {
            showInfoDialog()
        }


    }

    override fun onResume() {
        super.onResume()
        initAlarmBtn()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun scheduleAlarm(): Calendar {
        val calendar = Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, binding.timePicker.hour)
            set(Calendar.MINUTE, binding.timePicker.minute)
            //Calendar.AM_PM - AM = 0, PM = 1

        }
        return calendar
    }

    fun showAlarms() {
        val transac = activity?.supportFragmentManager?.beginTransaction()
        transac?.setCustomAnimations(
            R.anim.slide_in_left,
            R.anim.slide_out_right,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        transac?.replace(R.id.frame, AlarmsHistoryFragment())?.addToBackStack(null)?.commit()
    }

    fun FirstTimeDialog(){

        val FIRST_TIME_KEY = "com.example.app.MainActivity.firstTimeKey"
        val PREFERENCES = "AlarmRepeatPreferences"
        val sp = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val isFirstTime = sp.getBoolean(FIRST_TIME_KEY, true)
        if (isFirstTime) {
            val edit = sp.edit()
            edit.putBoolean(FIRST_TIME_KEY, false)
            edit.apply()

        //show the dialog
            showInfoDialog()
        }
    }

    private fun showInfoDialog(){
        val InfoDialog = SweetAlertDialog(context)
        InfoDialog.apply {
            titleText = "Why this app?"
            contentText = context.getString(R.string.use_of_app)
            confirmText = "Got it !"
            show()
        }
    }

}