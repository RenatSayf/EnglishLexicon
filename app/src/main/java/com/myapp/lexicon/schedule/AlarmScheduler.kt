@file:Suppress("unused")

package com.myapp.lexicon.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build


class AlarmScheduler(private val context: Context)
{
    companion object
    {
        const val REQUEST_CODE = 15875753
        const val ONE_SHOOT_ACTION = "$REQUEST_CODE.one_shoot_action"
        const val REPEAT_SHOOT_ACTION = "$REQUEST_CODE.repeat_shoot_action"
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(context, TimerReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun scheduleOne(overTime: Long)
    {
        val pendingIntent = createPendingIntent(ONE_SHOOT_ACTION)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val isCanSchedule = canScheduleExactAlarms()
                if (isCanSchedule) {
                    setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + overTime, pendingIntent)
                }
                else {
                    set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + overTime, pendingIntent)
                }
            }
            else {
                setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + overTime, pendingIntent)
            }
        }
    }

    fun scheduleRepeat(overTime: Long, timePeriod: Long)
    {
        val pendingIntent = createPendingIntent(REPEAT_SHOOT_ACTION)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.apply {
            setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + overTime, timePeriod, pendingIntent)
        }
    }

    fun cancel(action: String)
    {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(action)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}