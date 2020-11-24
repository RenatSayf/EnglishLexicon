package com.myapp.lexicon.di

import android.content.Context
import com.myapp.lexicon.schedule.AlarmScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
object AlarmSchedulerModule
{
    @Provides
    fun provideAlarmScheduler(@ApplicationContext context: Context) : AlarmScheduler
    {
        return AlarmScheduler(context)
    }
}