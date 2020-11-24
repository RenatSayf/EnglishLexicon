package com.myapp.lexicon.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import javax.inject.Inject

class AlarmScheduler @Inject constructor(private val context: Context)
{
    companion object
    {
        const val REQUEST_CODE = 15875753
        const val ONE_SHOOT_ACTION = "$REQUEST_CODE.one_shoot_action"
        const val REPEAT_SHOOT_ACTION = "$REQUEST_CODE.repeat_shoot_action"
    }

    fun scheduleOne(overTime: Long)
    {
        val intent = Intent(context, TimerReceiver::class.java).apply {
            this.action = ONE_SHOOT_ACTION
        }
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.apply {
            setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + overTime, pendingIntent)
        }
    }

    fun scheduleRepeat(overTime: Long, timePeriod: Long)
    {
        val intent = Intent(context, TimerReceiver::class.java).apply {
            this.action = REPEAT_SHOOT_ACTION
        }
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.apply {
            setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + overTime, timePeriod, pendingIntent)
        }
    }

    fun cancel(requestCode: Int, action: String)
    {
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, TimerReceiver::class.java).apply {
                    this.action = action
                },
                PendingIntent.FLAG_NO_CREATE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}