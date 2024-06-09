package com.myapp.lexicon.bgwork

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import com.myapp.lexicon.helpers.logIfDebug
import java.util.concurrent.TimeUnit

class DayEndReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == DAY_END_ACTION) {

            Thread.sleep(TimeUnit.SECONDS.toMillis(1))
            val calendar = context.getCalendarMoscowTimeZone().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val newAlarmTime = calendar.timeInMillis

            context.scheduleOneAlarm(newAlarmTime, this::class.java)

            "*********** ${this::class.java.simpleName}.onReceive() has been triggered ********************".logIfDebug()

            ResetDailyRewardWork.enqueueAtTime(context)
        }
    }
}