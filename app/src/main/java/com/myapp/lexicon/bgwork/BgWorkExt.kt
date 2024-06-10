package com.myapp.lexicon.bgwork

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Build
import com.myapp.lexicon.helpers.throwIfDebug
import java.util.Locale


private const val REQUEST_CODE = 524872359
const val DAY_END_ACTION = "$REQUEST_CODE.one_shoot_action"

@SuppressLint("UnspecifiedImmutableFlag")
fun<T: BroadcastReceiver> Context.scheduleOneAlarm(alarmTime: Long, receiverClazz: Class<out T>) {

    val intent = Intent(this, receiverClazz).apply {
        this.action = DAY_END_ACTION
    }
    val broadcast = PendingIntent.getBroadcast(this, REQUEST_CODE, intent,
            PendingIntent.FLAG_NO_CREATE
    )

    if (broadcast == null) {
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val isCanSchedule = canScheduleExactAlarms()
                if (isCanSchedule) {
                    setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                }
                else {
                    set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                }
            }
            else {
                setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
            }
        }
    }
    else {
        Exception("********** scheduleOneAlarm(): broadcast: PendingIntent already exists ***********").throwIfDebug()
    }
}

fun getCalendarMoscowTimeZone(): Calendar {
    return Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
}

val LOCALE_RU: Locale
    get() {
        return Locale("ru", "RU")
    }
