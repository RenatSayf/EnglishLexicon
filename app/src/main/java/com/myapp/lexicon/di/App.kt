package com.myapp.lexicon.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.billing.UserPurchases
import com.myapp.lexicon.settings.adsIsEnabled
import com.myapp.lexicon.settings.cloudStorageEnabled
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(), Configuration.Provider, UserPurchases.Listener {
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

        UserPurchases(context = this, listener = this)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
    }

    override fun disableAds() {
        this.adsIsEnabled = BuildConfig.IS_PURCHASE_TEST
    }

    override fun enableAds() {
        this.adsIsEnabled = true
    }

    override fun disableCloudStorage() {
        this.cloudStorageEnabled = false
    }

    override fun enableCloudStorage() {
        this.cloudStorageEnabled = !BuildConfig.IS_PURCHASE_TEST
    }

}
