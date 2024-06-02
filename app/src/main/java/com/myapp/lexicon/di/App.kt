@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.bgwork.ResetDailyRewardWork
import com.myapp.lexicon.helpers.printLogIfDebug
import com.parse.Parse
import com.yandex.mobile.ads.common.InitializationListener
import com.yandex.mobile.ads.common.MobileAds
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig


class App : Application(), Configuration.Provider {

    companion object {
        lateinit var INSTANCE: App
            private set
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        FirebaseApp.initializeApp(this)
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) {
                60
            } else {
                3600
            }
        }
        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetchAndActivate()
        }

        val apiKey = getString(R.string.ya_metrica_api_key)
        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        AppMetrica.activate(this, config)

        MobileAds.initialize(this, object : InitializationListener {
            override fun onInitializationCompleted() {
                printLogIfDebug("*************** MobileAds initialization successful ***************")
            }
        })

        Parse.initialize(
            Parse.Configuration.Builder(this).apply {
                applicationId(BuildConfig.PARSE_APP_ID)
                clientKey(BuildConfig.PARSE_CLIENT_KEY)
                server(getString(R.string.back4app_server_url))
            }.build()
        )

        ResetDailyRewardWork.enqueueAtTime(this.applicationContext)
    }
    override val workManagerConfiguration: Configuration
        get() {
            return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
        }

}