package com.myapp.lexicon.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        val apiKey = getString(R.string.ya_metrica_api_key)
        val config = YandexMetricaConfig.newConfigBuilder(apiKey).build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)

        com.yandex.mobile.ads.common.MobileAds.initialize(this) {
            if (BuildConfig.DEBUG) println("*************** ${this::class.java.simpleName}: Yandex mobile ads has been initialized *****************")
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
    }

}
