@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.ads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.myapp.lexicon.BuildConfig
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.InitializationListener
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
open class AdViewModel2 @Inject constructor(val app: Application): AndroidViewModel(app) {

    private var yandexInitStatus: Boolean = false
    private val isDisabled = false
    private val yandexAdRequest = com.yandex.mobile.ads.common.AdRequest.Builder().build()

    private val interstitialAdIds = listOf(
        "R-M-711878-6",
        "R-M-711878-5",
        "R-M-711878-4"
    )
    private val interstitialTestAdId = "demo-interstitial-yandex"

    private val bannerAdIds = listOf(
        "R-M-711878-1",
        "R-M-711878-2",
        "R-M-711878-3"
    )
    private val testBannerAdId = "demo-banner-yandex"

    init {
        com.yandex.mobile.ads.common.MobileAds.initialize(app, object : InitializationListener {
            override fun onInitializationCompleted() {
                this@AdViewModel2.yandexInitStatus = true
                if (BuildConfig.DEBUG) println("*************** Yandex mobile ads has been initialized *****************")
            }
        })
    }

    fun loadInterstitialAd(indexId: Int = 0, listener: YandexAdListener) {
        if (!isDisabled) {
            val yandexAdId = getInterstitialAdId(indexId)
            com.yandex.mobile.ads.interstitial.InterstitialAd(app).apply {
                setAdUnitId(yandexAdId)
                loadAd(yandexAdRequest)
                setInterstitialAdEventListener(object : InterstitialAdEventListener {
                    override fun onAdLoaded() {
                        listener.onYandexAdLoaded(this@apply)
                    }

                    override fun onAdFailedToLoad(error: AdRequestError) {
                        listener.onYandexAdFailed(error)
                    }

                    override fun onAdShown() {}

                    override fun onAdDismissed() {}

                    override fun onAdClicked() {}

                    override fun onLeftApplication() {}

                    override fun onReturnedToApplication() {}

                    override fun onImpression(p0: ImpressionData?) {}
                })
            }
        }
        else {
            listener.onYandexAdFailed(AdRequestError(-1, "Ads are disabled"))
        }
    }

    private fun getInterstitialAdId(index: Int = 0): String {
        return if (BuildConfig.DEBUG) {
            interstitialTestAdId
        } else {
            try {
                interstitialAdIds[index]
            } catch (e: IndexOutOfBoundsException) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                interstitialAdIds[0]
            }
        }
    }

    fun getBannerAdId(index: Int = 0): String {
        return if (BuildConfig.DEBUG) {
            testBannerAdId
        }else {
            try {
                bannerAdIds[index]
            } catch (e: IndexOutOfBoundsException) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                bannerAdIds[0]
            }
        }
    }

    interface YandexAdListener {
        fun onYandexAdLoaded(ad: com.yandex.mobile.ads.interstitial.InterstitialAd)
        fun onYandexAdFailed(error: AdRequestError)
    }

}