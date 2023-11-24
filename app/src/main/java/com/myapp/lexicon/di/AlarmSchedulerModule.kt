package com.myapp.lexicon.di

import android.content.Context
import com.myapp.lexicon.schedule.AlarmScheduler


object AlarmSchedulerModule
{
    fun provideAlarmScheduler(context: Context) : AlarmScheduler
    {
        return AlarmScheduler(context)
    }
}