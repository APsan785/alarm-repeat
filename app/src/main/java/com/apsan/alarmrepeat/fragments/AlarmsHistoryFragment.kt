package com.apsan.alarmrepeat.fragments

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apsan.alarmrepeat.adapter.RecyclerAlarmsAdapter
import com.apsan.alarmrepeat.alarmdb.AlarmDB
import com.apsan.alarmrepeat.alarmdb.AlarmModel
import com.apsan.alarmrepeat.databinding.FragmentAlarmshistoryBinding

class AlarmsHistoryFragment : Fragment() {

    lateinit var binding: FragmentAlarmshistoryBinding
    lateinit var helper: AlarmDB
    lateinit var db_r: SQLiteDatabase
    lateinit var db_w: SQLiteDatabase
    lateinit var alarmsList: ArrayList<AlarmModel>
    val TAG = "TAG"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmshistoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        helper = AlarmDB(requireContext())
        db_r = helper.readableDatabase
        db_w = helper.writableDatabase

        updateList()
        val adapter = RecyclerAlarmsAdapter(alarmsList, binding.noAlarmsTv)
        binding.recyclerAlarms.layoutManager = LinearLayoutManager(context)
        binding.recyclerAlarms.adapter = adapter

        binding.deleteAllbtn.setOnClickListener {

            SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Do you want to delete all alarms ?")
                .setConfirmText("Yes..")
                .setCancelText("No!")
                .setConfirmClickListener {
                    db_w.execSQL("delete from ${AlarmDB.TABLE_NAME} where not status = ${AlarmModel.STATUS_RUNNING}")
                    updateList()
                    adapter.notifyDataSetChanged()
                    it.cancel()
                }
                .setCancelClickListener {
                    it.cancel()
                }
                .show()

        }
    }

    private fun updateList() {

        if (this::alarmsList.isInitialized) {
            alarmsList.clear()
        }else{
            alarmsList = ArrayList()
        }
        alarmsList.addAll(helper.getAlarms(db_r).reversed())
    }

}