package com.myapp.lexicon.di

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.myapp.lexicon.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application()
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
            if (BuildConfig.DEBUG) println("*************** Yandex mobile ads has been initialized *****************")
        }
    }
}
