package com.apsan.alarmrepeat.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apsan.alarmrepeat.alarmdb.AlarmModel
import com.apsan.alarmrepeat.databinding.AlarmCardBinding

class RecyclerAlarmsAdapter(
    val AlarmsList : ArrayList<AlarmModel>,
    val tv:TextView
) : RecyclerView.Adapter<RecyclerAlarmsAdapter.AlarmCard>() {

    val TAG = "TAG"

    class AlarmCard(val cardBinding: AlarmCardBinding) : RecyclerView.ViewHolder(cardBinding.root) {
        fun bindit(alarm: AlarmModel) {
            cardBinding.apply {
                timeText.text = "${alarm.start_time} to ${alarm.stop_time}"
                triggers.text = alarm.triggered.toString()
                totals.text = alarm.total.toString()
                if (alarm.status == AlarmModel.STATUS_CANCELLED) {
                    status.text = "CANCELLED"
                    status.setTextColor(Color.parseColor("#FF0000"))
                } else if (alarm.status == AlarmModel.STATUS_DONE) {
                    status.apply {
                        text = "DONE"
                        setTextColor(Color.parseColor("#3D2FFB"))
                    }
                } else if (alarm.status == AlarmModel.STATUS_RUNNING) {
                    status.apply {
                        text = "RUNNING"
                        setTextColor(Color.parseColor("#24813A"))
                    }
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmCard {
        return AlarmCard(AlarmCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: AlarmCard, position: Int) {
        holder.bindit(AlarmsList[position])
    }

    override fun getItemCount(): Int {
        if(AlarmsList.size == 0) {
            tv.setVisibility(View.VISIBLE)
        }
        return AlarmsList.size
    }
}