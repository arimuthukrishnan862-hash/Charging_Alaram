package com.muthukrishnann.chargeguard.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AlarmController.stopAlarm(context)
    }
}
