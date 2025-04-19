package com.myapp.lexicon.ads.interstitial

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.ext.toRevenue
import com.myapp.lexicon.ads.models.TestAdData
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.common.IS_REWARD_ACCESSIBLE
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.models.AdsReward
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.settings.accessToken
import com.myapp.lexicon.settings.refreshToken
import com.myapp.lexicon.settings.saveAuthTokens
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private var ad: InterstitialAd? = null

private val TEST_INTERSTITIAL_DATA: String
    get() = """{
      "currency": "RUB",
      "revenueUSD": "0.03332",
      "precision": "estimated",
      "revenue": "2.9999",
      "requestId": "${System.currentTimeMillis()}617871108186477874100342-demo-interstitial-yandex",
      "blockId": "demo-interstitial-yandex",
      "adType": "interstitial",
      "ad_unit_id": "demo-interstitial-yandex",
      "network": {
        "name": "Yandex",
        "adapter": "Yandex",
        "ad_unit_id": "demo-interstitial-yandex"
      }
    }"""

fun FragmentActivity.loadInterstitialAd(
    adId: String,
    onLoaded: (ad: InterstitialAd) -> Unit = {}
) {

    this.orientationLock()

    val id = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
        "demo-interstitial-yandex"
    } else {
        adId
    }
    val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
    InterstitialAdLoader(this).apply {

        setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {

                this@loadInterstitialAd.orientationUnLock()
                ad = interstitialAd
                onLoaded.invoke(interstitialAd)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {

                this@loadInterstitialAd.orientationUnLock()
                "${this::class.simpleName} - ${error.description}".logIfDebug()
            }
        })
        loadAd(adRequestConfiguration)
    }
}

fun FragmentActivity.showInterstitialAd(
    onDismissed: (reward: AdsReward) -> Unit = {},
    onAuthorizationRequired: () -> Unit
) {
    ad?.setAdEventListener(object : InterstitialAdEventListener {

        private var reward: AdsReward? = null

        private val repository: INetRepository = NetRepositoryModule().apply {
            setTokensUpdateListener(object : INetRepositoryModule.Listener {
                override fun onUpdateTokens(tokens: Tokens) {
                    this@apply.setRefreshToken(tokens.refreshToken)
                    this@showInterstitialAd.saveAuthTokens(tokens)
                }
                override fun onAuthorizationRequired() {
                    onAuthorizationRequired.invoke()
                }
            })
            setRefreshToken(this@showInterstitialAd.refreshToken)
        }.provideNetRepository()

        override fun onAdShown() {
            this@showInterstitialAd.orientationLock()
        }

        override fun onAdFailedToShow(adError: AdError) {
            "${this::class.simpleName} - ${adError.description}".logIfDebug()
            this@showInterstitialAd.orientationUnLock()
        }

        override fun onAdDismissed() {
            this@showInterstitialAd.orientationUnLock()
            try {
                if (reward != null) {
                    onDismissed.invoke(reward!!)
                }
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
            }
        }

        override fun onAdClicked() {}

        override fun onAdImpression(impressionData: ImpressionData?) {
            if (IS_REWARD_ACCESSIBLE) {

                val impressData: ImpressionData? = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                    TestAdData(TEST_INTERSTITIAL_DATA)
                }
                else {
                    impressionData
                }

                impressData?.let {
                    val rawData = it.rawData
                    val revenue = rawData.toRevenue()
                    val accessToken = this@showInterstitialAd.accessToken
                    if (accessToken.isNotEmpty() && revenue != null) {
                        this@showInterstitialAd.lifecycleScope.launch(context = Dispatchers.Main) {
                            repository.updateUserBalance(accessToken, revenue).collect(collector = { result ->
                                result.onSuccess { r ->
                                    reward = r
                                }
                            })
                        }
                    }
                }
            }
        }
    })
    ad?.show(this)?: run {
        Exception("Ad is not loaded yet").printStackTraceIfDebug()
    }
}














