@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.banner.BannerAdIds
import com.myapp.lexicon.ads.feed_ad.FeedAdIds
import com.myapp.lexicon.ads.interstitial.InterstitialAdIds
import com.myapp.lexicon.ads.native_ad.NativeAdIds
import com.myapp.lexicon.ads.rewarded.RewardedAdIds
import com.myapp.lexicon.helpers.printLogIfDebug
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.settings.RemoteConfigViewModel
import com.parse.Parse
import com.yandex.mobile.ads.common.InitializationListener
import com.yandex.mobile.ads.common.MobileAds
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig


class App : Application(), Configuration.Provider {

    var defaultConfig: RemoteConfigViewModel.Config = RemoteConfigViewModel.Config(
        adTypePerScreen = RemoteConfigViewModel.Config.AdType(
            main = 1,
            service = 1,
            test = 3,
            translate = 1,
            video = 3
        ),
        bannerIds = RemoteConfigViewModel.Config.BannerIds(
            main = BannerAdIds.BANNER_1.id,
            service = BannerAdIds.BANNER_1.id,
            editor = BannerAdIds.BANNER_2.id,
            translate = BannerAdIds.BANNER_3.id
        ),
        nativeIds = RemoteConfigViewModel.Config.NativeIds(
            main = NativeAdIds.NATIVE_1.id,
            service = NativeAdIds.NATIVE_1.id,
            translate = NativeAdIds.NATIVE_2.id,
            test = NativeAdIds.NATIVE_2.id,
            video = NativeAdIds.NATIVE_1.id
        ),
        interstitialAdIds = RemoteConfigViewModel.Config.InterstitialIds(
            main = InterstitialAdIds.INTERSTITIAL_1.id,
            service = InterstitialAdIds.INTERSTITIAL_1.id,
            translate = InterstitialAdIds.INTERSTITIAL_2.id,
            test = InterstitialAdIds.INTERSTITIAL_3.id,
            video = InterstitialAdIds.INTERSTITIAL_3.id
        ),
        rewardedIds = RemoteConfigViewModel.Config.RewardedIds(
            main = RewardedAdIds.REWARDED_1.id,
            service = RewardedAdIds.REWARDED_1.id,
            translate = RewardedAdIds.REWARDED_2.id,
            test = RewardedAdIds.REWARDED_3.id,
            video = RewardedAdIds.REWARDED_3.id
        ),
        feedIds = RemoteConfigViewModel.Config.FeedAdIds(
            main = FeedAdIds.FEED_1.id,
            service = FeedAdIds.FEED_1.id,
            translate = FeedAdIds.FEED_1.id,
            test = FeedAdIds.FEED_1.id,
            video = FeedAdIds.FEED_1.id
        )
    )

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

            addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    fetchAndActivate()
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    error.printStackTraceIfDebug()
                }
            })
        }

        val apiKey = getString(R.string.ya_metrica_api_key)
        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        AppMetrica.activate(this, config)

        MobileAds.initialize(this, object : InitializationListener {
            override fun onInitializationCompleted() {
                printLogIfDebug("*************** MobileAds initialization successful ***************")
            }
        })
        MobileAds.enableDebugErrorIndicator(BuildConfig.DEBUG)

        Parse.initialize(
            Parse.Configuration.Builder(this).apply {
                applicationId(BuildConfig.PARSE_APP_ID)
                clientKey(BuildConfig.PARSE_CLIENT_KEY)
                server(getString(R.string.back4app_server_url))
            }.build()
        )

        val configVM = RemoteConfigViewModel(netModule = NetRepositoryModule())
        configVM.fetchRemoteConfig(
            onSuccess = { config ->
                this.defaultConfig = config
            },
            onFailure = {

            }
        )

    }
    override val workManagerConfiguration: Configuration
        get() {
            return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
        }

}