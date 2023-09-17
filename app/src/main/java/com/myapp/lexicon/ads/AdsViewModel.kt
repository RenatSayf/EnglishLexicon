package com.myapp.lexicon.ads

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.helpers.printLogIfDebug
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AdsViewModel @Inject constructor(
    private val app: Application
): AndroidViewModel(app) {

    private var _interstitialAd = MutableLiveData<Result<InterstitialAd>>()
    val interstitialAd: LiveData<Result<InterstitialAd>> = _interstitialAd

    private var _rewardedAd = MutableLiveData<Result<RewardedAd>>()
    val rewardedAd: LiveData<Result<RewardedAd>> = _rewardedAd

    fun loadInterstitialAd(adId: InterstitialAdIds) {
        val id = if (BuildConfig.DEBUG) {
            "demo-interstitial-yandex"
        } else {
            adId.id
        }
        val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
        InterstitialAdLoader(app).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(p0: InterstitialAd) {
                    _interstitialAd.value = Result.success(p0)
                }

                override fun onAdFailedToLoad(p0: AdRequestError) {
                    printLogIfDebug("${this::class.simpleName} - ${p0.description}")
                    _interstitialAd.value = Result.failure(Throwable(p0.description))
                }
            })
            loadAd(adRequestConfiguration)
        }
    }

    fun loadRewardedAd(adId: RewardedAdIds) {
        val id = if (BuildConfig.DEBUG) {
            "demo-rewarded-yandex"
        } else {
            adId.id
        }
        val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
        RewardedAdLoader(app).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(p0: RewardedAd) {
                    _rewardedAd.value = Result.success(p0)
                }

                override fun onAdFailedToLoad(p0: AdRequestError) {
                    printLogIfDebug("${this::class.simpleName} - ${p0.description}")
                    _rewardedAd.value = Result.failure(Throwable(p0.description))
                }
            })
            loadAd(adRequestConfiguration)
        }
    }
}

fun InterstitialAd.showAd(
    activity: Activity,
    onShown: () -> Unit = {},
    onImpression: () -> Unit = {},
    onDismissed: () -> Unit = {}
) {
    this.apply {
        setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() {
                onShown.invoke()
            }

            override fun onAdFailedToShow(p0: AdError) {
                printLogIfDebug("${this::class.simpleName} - ${p0.description}")
            }

            override fun onAdDismissed() {
                onDismissed.invoke()
            }

            override fun onAdClicked() {}

            override fun onAdImpression(p0: ImpressionData?) {
                p0?.let {
                    val rawData = it.rawData
                    rawData
                    onImpression.invoke()
                }
            }
        })
    }
}