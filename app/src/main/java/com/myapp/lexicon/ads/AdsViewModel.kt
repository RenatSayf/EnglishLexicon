package com.myapp.lexicon.ads

import android.app.Activity
import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.common.IS_REWARD_ACCESSIBLE
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.helpers.printLogIfDebug
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.screenHeight
import com.myapp.lexicon.helpers.screenWidth
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.settings.adsIsEnabled
import com.myapp.lexicon.settings.isUserRegistered
import com.yandex.mobile.ads.banner.BannerAdEventListener
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
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoader
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.roundToInt


var REQUEST_ID: String? = null

class AdsViewModel @Inject constructor(
    private val app: Application
): AndroidViewModel(app) {

    sealed class AdState {
        data object Init: AdState()
        data class Dismissed(val bonus: Double): AdState()
    }

    private var _interstitialAdState = MutableLiveData<AdState>(AdState.Init)
    val interstitialAdState: LiveData<AdState> = _interstitialAdState

    fun setInterstitialAdState(state: AdState) {
        _interstitialAdState.value = state
    }

    private var _interstitialAd = MutableLiveData<Result<InterstitialAd>>()
    val interstitialAd: LiveData<Result<InterstitialAd>> = _interstitialAd

    private var _rewardedAd = MutableLiveData<Result<RewardedAd>>()
    var rewardedAd: LiveData<Result<RewardedAd>> = _rewardedAd

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

    fun loadInterstitialAd(adId: String) {

        val id = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
            "demo-interstitial-yandex"
        } else {
            adId
        }
        val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
        InterstitialAdLoader(app).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    _interstitialAd.value = Result.success(interstitialAd)
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    printLogIfDebug("${this::class.simpleName} - ${error.description}")
                    _interstitialAd.value = Result.failure(Throwable(error.description))
                }
            })
            loadAd(adRequestConfiguration)
        }
    }

    fun loadRewardedAd(adId: String) {

        val id = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
            "demo-rewarded-yandex"
        } else {
            adId
        }
        val adRequestConfiguration = AdRequestConfiguration.Builder(id).build()
        RewardedAdLoader(app).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(rewarded: RewardedAd) {
                    _rewardedAd.value = Result.success(rewarded)
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    printLogIfDebug("${this::class.simpleName} - ${error.description}")
                    _rewardedAd.value = Result.failure(Throwable(error.description))
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
    onDismissed: (bonus: Double) -> Unit = {}
) {
    val isUserRegistered = activity.isUserRegistered(onYes = {})
    this.apply {
        setAdEventListener(object : InterstitialAdEventListener {

            private var bonus: Double = 0.0

            override fun onAdShown() {
                onShown.invoke()
            }

            override fun onAdFailedToShow(adError: AdError) {
                "${this::class.simpleName} - ${adError.description}".logIfDebug()
                onDismissed.invoke(0.0)
            }

            override fun onAdDismissed() {
                try {
                    onDismissed.invoke(bonus)
                } catch (e: Exception) {
                    e.printStackTraceIfDebug()
                }
            }

            override fun onAdClicked() {}

            override fun onAdImpression(impressionData: ImpressionData?) {
                if (IS_REWARD_ACCESSIBLE) {
                    impressionData?.let {
                        val rawData = it.rawData
                        rawData.toAdData(
                            onSuccess = {data ->
                                if (isUserRegistered) {
                                    bonus = (data.revenue * UserViewModel.USER_PERCENTAGE).to2DigitsScale()
                                    onImpression.invoke(data)
                                } else {
                                    onImpression.invoke(null)
                                }
                            },
                            onFailed = {
                                onImpression.invoke(null)
                            }
                        )
                    }?: run {
                        if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name || BuildConfig.ADS_SOURCE == AdsSource.LOCAL_HOST.name) {
                            TEST_INTERSTITIAL_DATA.toAdData(
                                onSuccess = { data ->
                                    if (isUserRegistered) {
                                        bonus = (data.revenue * UserViewModel.USER_PERCENTAGE).to2DigitsScale()
                                        onImpression.invoke(data)
                                    } else {
                                        onImpression.invoke(null)
                                    }
                                },
                                onFailed = {
                                    onImpression.invoke(null)
                                }
                            )
                        }
                        else {
                            onImpression.invoke(null)
                        }
                    }
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
    onDismissed: (bonus: Double) -> Unit = {}
) {
    val isUserRegistered = activity.isUserRegistered(onYes = {})
    this.apply {
        setAdEventListener(object : RewardedAdEventListener {
            private var bonus: Double = 0.0

            override fun onAdShown() {
                onShown.invoke()
            }

            override fun onAdFailedToShow(adError: AdError) {
                printLogIfDebug("${this::class.simpleName} - ${adError.description}")
                onDismissed.invoke(bonus)
            }

            override fun onAdDismissed() {
                onDismissed.invoke(bonus)
            }

            override fun onAdClicked() {}

            override fun onAdImpression(impressionData: ImpressionData?) {
                if (IS_REWARD_ACCESSIBLE) {
                    impressionData?.let {
                        val rawData = it.rawData
                        rawData.toAdData(
                            onSuccess = {data ->

                                if (isUserRegistered) {
                                    bonus = (data.revenue * UserViewModel.USER_PERCENTAGE).to2DigitsScale()
                                    onImpression.invoke(data)
                                } else {
                                    onImpression.invoke(null)
                                }
                            },
                            onFailed = {
                                onImpression.invoke(null)
                            }
                        )
                    }?: run {
                        if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name || BuildConfig.ADS_SOURCE == AdsSource.LOCAL_HOST.name) {
                            TEST_REWARDED_DATA.toAdData(
                                onSuccess = { data ->
                                    onImpression.invoke(data)
                                },
                                onFailed = {
                                    onImpression.invoke(null)
                                }
                            )
                        }
                        else {
                            onImpression.invoke(null)
                        }
                    }
                }
            }

            override fun onRewarded(reward: Reward) {}
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

private val TEST_BANNER_DATA: String
    get() {
        return """{
  "currency": "RUB",
  "revenueUSD": "0.04",
  "precision": "estimated",
  "revenue": "3.0",
  "requestId": "${System.currentTimeMillis()}617871108186477874100342-demo-banner-yandex",
  "blockId": "demo-banner-yandex",
  "adType": "banner",
  "ad_unit_id": "demo-banner-yandex",
  "network": {
    "name": "Yandex",
    "adapter": "Yandex",
    "ad_unit_id": "demo-banner-yandex"
  }
}"""
    }

fun FragmentActivity.loadNativeAds(
    adId: String,
    adCount: Int = 1,
    onLoaded: (List<NativeAd>) -> Unit,
    onFailed: (Throwable) -> Unit
) {
    val nativeAdLoader = NativeBulkAdLoader(this)
    val id = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) "demo-native-app-yandex"
    else adId

    nativeAdLoader.loadAds(
        nativeAdRequestConfiguration = NativeAdRequestConfiguration.Builder(id).apply {
            setShouldLoadImagesAutomatically(true)
        }.build(),
        adsCount = adCount
    )
    nativeAdLoader.setNativeBulkAdLoadListener(object : NativeBulkAdLoadListener {
        override fun onAdsFailedToLoad(error: AdRequestError) {
            onFailed.invoke(Throwable(error.description))
        }

        override fun onAdsLoaded(nativeAds: List<NativeAd>) {
            onLoaded.invoke(nativeAds)
        }
    })
}

fun BannerAdView.loadBanner(
    adId: String,
    heightRate: Double = 0.08,
    onCompleted: (error: AdRequestError?) -> Unit = {},
    onImpression: (data: AdData?) -> Unit = {},
    onAdClicked: () -> Unit = {}
) {

    if (this.context.adsIsEnabled) {
        val adWidth = this.context.screenWidth
        val adHeight = (this.context.screenHeight * heightRate).roundToInt()
        val fixedSize = BannerAdSize.fixedSize(this.context, adWidth, adHeight)
        this.apply {
            val id = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                "demo-banner-yandex"
            } else {
                adId
            }
            setAdUnitId(id)
            setAdSize(fixedSize)
        }.loadAd(AdRequest.Builder().build())

        this.setBannerAdEventListener(object : BannerAdEventListener {

            override fun onAdClicked() {
                onAdClicked.invoke()
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                onCompleted.invoke(error)
            }

            override fun onAdLoaded() {
                onCompleted.invoke(null)
            }

            override fun onImpression(impressionData: ImpressionData?) {
                impressionData?.let {
                    //println("************ onImpression(): impressionData: ${impressionData.rawData} *******************")
                    it.rawData.toAdData(
                        onSuccess = {adData: AdData ->
                            if (adData.adUnitId == "demo-banner-yandex") {
                                TEST_BANNER_DATA.toAdData(
                                    onSuccess = { data ->
                                        onImpression.invoke(data)
                                    },
                                    onFailed = {
                                        onImpression.invoke(null)
                                    }
                                )
                            }
                            else {
                                onImpression.invoke(adData)
                            }
                        },
                        onFailed = {
                            onImpression.invoke(null)
                        }
                    )
                }?: run {
                    if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name || BuildConfig.ADS_SOURCE == AdsSource.LOCAL_HOST.name) {
                        TEST_BANNER_DATA.toAdData(
                            onSuccess = { data ->
                                onImpression.invoke(data)
                            },
                            onFailed = {
                                onImpression.invoke(null)
                            }
                        )
                    }
                    else {
                        onImpression.invoke(null)
                    }
                }
            }

            override fun onLeftApplication() {
                return
            }

            override fun onReturnedToApplication() {
                return
            }
        })
    }
}