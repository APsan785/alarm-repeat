package com.apsan.alarmrepeat.alarmdb

data class AlarmModel(
    val _id: Int,
    val start_time: String,
    val stop_time: String,
    val total: Int,
    val triggered: Int,
    val status: Int
) {
    companion object {
        val STATUS_RUNNING = 2
        val STATUS_CANCELLED = 1
        val STATUS_DONE = 0
    }
}

