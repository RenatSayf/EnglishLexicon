package com.myapp.lexicon.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.myapp.lexicon.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(), Configuration.Provider
{
    override fun attachBaseContext(base: Context?)
    {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate()
    {
        super.onCreate()

        com.yandex.mobile.ads.common.MobileAds.initialize(this) {
            if (BuildConfig.DEBUG) println("*************** ${this::class.java.simpleName}: Yandex mobile ads has been initialized *****************")
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
    }


}
