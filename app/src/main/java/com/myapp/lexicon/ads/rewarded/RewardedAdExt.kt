package com.myapp.lexicon.ads.rewarded

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
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private val TEST_REWARDED_DATA: String
    get() = """{
      "currency": "RUB",
      "revenueUSD": "0.50051",
      "precision": "estimated",
      "revenue": "50.0",
      "requestId": "${System.currentTimeMillis()}617871108186477874100342-demo-rewarded-yandex",
      "blockId": "demo-rewarded-yandex",
      "adType": "interstitial",
      "ad_unit_id": "demo-rewarded-yandex",
      "network": {
        "name": "Yandex",
        "adapter": "Yandex",
        "ad_unit_id": "demo-rewarded-yandex"
      }
    }"""

private var ad: RewardedAd? = null

fun FragmentActivity.loadRewardedAd(
    adId: String,
    onLoaded: (ad: RewardedAd) -> Unit = {}
) {

    this.orientationLock()

    val id = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
        "demo-rewarded-yandex"
    } else {
        adId
    }

    val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
    RewardedAdLoader(this).apply {
        setAdLoadListener(object : RewardedAdLoadListener {
            override fun onAdLoaded(rewarded: RewardedAd) {
                ad = rewarded
                onLoaded.invoke(rewarded)
                this@loadRewardedAd.orientationUnLock()
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                "${this::class.simpleName} - ${error.description}".logIfDebug()
                this@loadRewardedAd.orientationUnLock()
            }
        })
        loadAd(adRequestConfiguration)
    }
}

fun FragmentActivity.showRewardedAd(
    onDismissed: (reward: AdsReward) -> Unit = {},
    onAuthorizationRequired: () -> Unit
) {
    ad?.setAdEventListener(object : RewardedAdEventListener {

        private var reward: AdsReward? = null

        private val repository: INetRepository = NetRepositoryModule().apply {
            setTokensUpdateListener(object : INetRepositoryModule.Listener {
                override fun onUpdateTokens(tokens: Tokens) {
                    this@apply.setRefreshToken(tokens.refreshToken)
                    this@showRewardedAd.saveAuthTokens(tokens)
                }
                override fun onAuthorizationRequired() {
                    onAuthorizationRequired.invoke()
                }
            })
            setRefreshToken(this@showRewardedAd.refreshToken)
        }.provideNetRepository()

        override fun onAdShown() {
            this@showRewardedAd.orientationLock()
        }

        override fun onAdFailedToShow(adError: AdError) {
            "${this::class.simpleName} - ${adError.description}".logIfDebug()
            this@showRewardedAd.orientationUnLock()
        }

        override fun onAdDismissed() {
            this@showRewardedAd.orientationUnLock()
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
                    TestAdData(TEST_REWARDED_DATA)
                }
                else {
                    impressionData
                }

                impressData?.let {
                    val rawData = it.rawData
                    val revenue = rawData.toRevenue()
                    val accessToken = this@showRewardedAd.accessToken
                    if (accessToken.isNotEmpty() && revenue != null) {
                        this@showRewardedAd.lifecycleScope.launch(context = Dispatchers.Main) {
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

        override fun onRewarded(reward: Reward) {}
    })
    ad?.show(this)?: run {
        Exception("Ad is not loaded yet").printStackTraceIfDebug()
    }
}




