package com.myapp.lexicon.di

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.google.android.gms.ads.MobileAds
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
        MobileAds.initialize(this)
    }
}
