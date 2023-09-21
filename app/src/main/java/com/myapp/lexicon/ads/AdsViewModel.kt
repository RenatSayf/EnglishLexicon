package com.myapp.lexicon.ads

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.helpers.printLogIfDebug
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class AdsViewModel @Inject constructor(
    private val app: Application
): AndroidViewModel(app) {

    private var _interstitialAd = MutableLiveData<Result<InterstitialAd>>()
    val interstitialAd: LiveData<Result<InterstitialAd>> = _interstitialAd

    private var _rewardedAd = MutableLiveData<Result<RewardedAd>>()
    val rewardedAd: LiveData<Result<RewardedAd>> = _rewardedAd

    fun getInterstitialAdOrNull(): InterstitialAd? {
        _interstitialAd.value?.onSuccess { ad ->
            return ad
        }
        return null
    }

    fun getRewardedAdOrNull(): RewardedAd? {
        _rewardedAd.value?.onSuccess { ad ->
            return ad
        }
        return null
    }

    fun loadInterstitialAd(adId: InterstitialAdIds? = null) {

        val id = if (BuildConfig.DEBUG) {
            "demo-interstitial-yandex"
        } else {
            adId?.id ?: InterstitialAdIds.values().random().id
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

    fun loadRewardedAd(adId: RewardedAdIds? = null) {
        val id = if (BuildConfig.DEBUG) {
            "demo-rewarded-yandex"
        } else {
            adId?.id ?: RewardedAdIds.values().random().id
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
    onImpression: (data: AdData?) -> Unit = {},
    onFailed: () -> Unit = {},
    onDismissed: () -> Unit = {}
) {
    this.apply {
        setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() {
                onShown.invoke()
            }

            override fun onAdFailedToShow(p0: AdError) {
                printLogIfDebug("${this::class.simpleName} - ${p0.description}")
                onFailed.invoke()
            }

            override fun onAdDismissed() {
                onDismissed.invoke()
            }

            override fun onAdClicked() {}

            override fun onAdImpression(p0: ImpressionData?) {
                p0?.let {
                    val rawData = it.rawData
                    rawData.toAdData(
                        onSuccess = {data ->
                            onImpression.invoke(data)
                        },
                        onFailed = {
                            onImpression.invoke(null)
                        }
                    )
                }?: run {
                    onImpression.invoke(null)
                }
            }
        })
        show(activity)
    }
}

fun RewardedAd.showAd(
    activity: Activity,
    onShown: () -> Unit = {},
    onImpression: (data: AdData?) -> Unit = {},
    onFailed: () -> Unit = {},
    onDismissed: () -> Unit = {}
) {
    this.apply {
        setAdEventListener(object : RewardedAdEventListener {
            override fun onAdShown() {
                onShown.invoke()
            }

            override fun onAdFailedToShow(p0: AdError) {
                printLogIfDebug("${this::class.simpleName} - ${p0.description}")
                onFailed.invoke()
            }

            override fun onAdDismissed() {
                onDismissed.invoke()
            }

            override fun onAdClicked() {}

            override fun onAdImpression(p0: ImpressionData?) {
                p0?.let {
                    val rawData = it.rawData
                    rawData.toAdData(
                        onSuccess = {data ->
                            onImpression.invoke(data)
                        },
                        onFailed = {
                            onImpression.invoke(null)
                        }
                    )
                }?: run {
                    onImpression.invoke(null)
                }
            }

            override fun onRewarded(p0: Reward) {}
        })
        show(activity)
    }
}

fun String.toAdData(
    onSuccess: (AdData) -> Unit,
    onFailed: () -> Unit
) {
    try {
        val adData = Json.decodeFromString<AdData>(this)
        onSuccess.invoke(adData)
    }
    catch (e: Exception) {
        if (BuildConfig.DEBUG) e.printStackTrace()
        onFailed.invoke()
    }
}

private const val JSON_STRING = """{
  "currency": "RUB",
  "revenueUSD": "0.001200671",
  "precision": "estimated",
  "revenue": "0.116025434",
  "requestId": "1694954665976270-617871108186477874100342-production-app-host-vla-326",
  "blockId": "R-M-711877-3",
  "adType": "interstitial",
  "ad_unit_id": "R-M-711877-3",
  "network": {
    "name": "Yandex",
    "adapter": "Yandex",
    "ad_unit_id": "R-M-711877-3"
  }
}"""

fun BannerAdView.loadBanner(adId: BannerAdIds? = null) {
    val width = (this.context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.density).roundToInt()
    val stickySize = BannerAdSize.stickySize(this.context, width)
    this.apply {
        val id = if (BuildConfig.DEBUG) {
            "demo-banner-yandex"
        } else {
            adId?.id ?: BannerAdIds.values().random().id
        }
        setAdUnitId(id)
        setAdSize(stickySize)
    }.loadAd(AdRequest.Builder().build())
}