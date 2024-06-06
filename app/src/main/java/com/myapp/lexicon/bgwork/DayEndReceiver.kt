package com.myapp.lexicon.bgwork

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import com.myapp.lexicon.helpers.logIfDebug

class DayEndReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DAY_END_ACTION) {
            val newAlarmTime = context.getCalendarMoscowTimeZone().apply {
                timeInMillis += 120_000L
            }.timeInMillis

            context.scheduleOneAlarm(newAlarmTime, this::class.java)

            "*********** ${this::class.java.simpleName}.onReceive() has been triggered ********************".logIfDebug()
        }
    }
}