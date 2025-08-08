package com.myapp.lexicon.ads.ext

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.NativeAd
import com.appodeal.ads.NativeCallbacks
import com.appodeal.ads.RewardedVideoCallbacks
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.appodeal.ads.nativead.NativeAdView
import com.appodeal.ads.revenue.RevenueInfo
import com.appodeal.ads.revenue.RevenuePlatform
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.ads.RevenueViewModel
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.common.KEY_AD_DATA
import com.myapp.lexicon.common.KEY_REVENUE_PER_AD
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.models.User
import kotlin.random.Random


private const val MULTIPLIER = 1000
private val DEBUG_REWARD_USD: Double
    get() {
        return Random(System.currentTimeMillis()).nextInt(1, 12) * 0.001
    }

private var coins: Int = 0

private var adsVM: AdsViewModel? = null

private fun createTestRevenueInfo(adType: Int, adTypeString: String): RevenueInfo {
    return RevenueInfo(
        networkName = "XXX",
        demandSource = "AAA",
        adUnitName = "***",
        placement = "***",
        placementId = 0,
        segmentId = 0L,
        revenue = DEBUG_REWARD_USD,
        revenuePrecision = "***",
        adType = adType,
        adTypeString = adTypeString,
        revenuePlatform = RevenuePlatform.APPODEAL,
        payload = mapOf()
    )
}

fun FragmentActivity.setRevenueUpdateResult(revenueInfo: RevenueInfo) {

    val viewModel = ViewModelProvider(this@setRevenueUpdateResult)[RevenueViewModel::class]
    val adData = AdData(
        adType = revenueInfo.adTypeString,
        adUnitId = revenueInfo.demandSource,
        currency = revenueInfo.currency,
        requestId = System.currentTimeMillis().toString(),
        revenue = (revenueInfo.revenue * MULTIPLIER).toInt().toDouble(),
        revenueUSD = revenueInfo.revenue
    )
    coins = 0
    coins = adData.revenue.toInt()
    adsVM?.setAdReward(coins)
    viewModel.updateUserRevenueIntoCloud(adData).observe(this) { user ->
        if (user != null) {
            this.supportFragmentManager.setFragmentResult(
                KEY_AD_DATA,
                bundleOf(
                    User.KEY_USER_REWARD to user.userReward,
                    KEY_REVENUE_PER_AD to adData.revenue
                )
            )
        }
    }
}

fun FragmentActivity.initAppodealAd(
    adType: Int = Appodeal.NATIVE,
    onCompleted: () -> Unit = {}
) {
    Appodeal.initialize(
        context = this,
        appKey = BuildConfig.ADS_KEY,
        adTypes = adType,
        callback = object : ApdInitializationCallback {
            override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
                errors?.forEach { t ->
                    t.printStackTraceIfDebug()
                }
                onCompleted.invoke()
            }
        }
    )
}

fun FragmentActivity.loadAndShowRewardedAd(
    onNotLoaded: () -> Unit = {},
    onClosed: (coins: Int) -> Unit = {}
) {
    if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {

        adsVM = ViewModelProvider(this@loadAndShowRewardedAd)[AdsViewModel::class]

        Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {

            override fun onRewardedVideoClicked() {
                return
            }

            override fun onRewardedVideoClosed(finished: Boolean) {
                onClosed.invoke(coins)
            }

            override fun onRewardedVideoExpired() {
                return
            }

            override fun onRewardedVideoFailedToLoad() {
                onNotLoaded.invoke()
            }

            override fun onRewardedVideoFinished(amount: Double, currency: String) {
                return
            }

            override fun onRewardedVideoLoaded(isPrecache: Boolean) {
                return
            }

            override fun onRewardedVideoShowFailed() {
                onNotLoaded.invoke()
            }

            override fun onRewardedVideoShown() {
                if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                    val testRevenueInfo = createTestRevenueInfo(4, "Rewarded")
                    this@loadAndShowRewardedAd.setRevenueUpdateResult(testRevenueInfo)
                }
            }
        })
        if (Appodeal.canShow(Appodeal.REWARDED_VIDEO)) {
            Appodeal.show(this, Appodeal.REWARDED_VIDEO)
        }
    }
    else {
        Throwable("******** Rewarded Ad is NOT LOADED ***********").printStackTraceIfDebug()
        onNotLoaded.invoke()
    }
}

fun FragmentActivity.showNativeAdsIfLoaded(
    adsList: List<NativeAdView> = listOf(),
    onNotAvailableAds: () -> Unit = {},
    onNotLoaded: () -> Unit = {},
    onShow: () -> Unit = {}
) {

    val adsCount = Appodeal.getAvailableNativeAdsCount()
    if (adsCount > 0) {

        adsVM = ViewModelProvider(this@showNativeAdsIfLoaded)[AdsViewModel::class]

        Appodeal.setNativeCallbacks(object : NativeCallbacks {
            override fun onNativeClicked(nativeAd: NativeAd?) {
                return
            }

            override fun onNativeExpired() {
                return
            }

            override fun onNativeFailedToLoad() {
                onNotLoaded.invoke()
            }

            override fun onNativeLoaded() {
                return
            }

            override fun onNativeShowFailed(nativeAd: NativeAd?) {
                onNotLoaded.invoke()
            }

            override fun onNativeShown(nativeAd: NativeAd?) {
                if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                    val testRevenueInfo = createTestRevenueInfo(2, "Native")
                    this@showNativeAdsIfLoaded.setRevenueUpdateResult(testRevenueInfo)
                }
                onShow.invoke()
            }
        })
        val nativeAds = Appodeal.getNativeAds(adsList.size)
        nativeAds.forEachIndexed { index, ad ->
            adsList[index].registerView(ad)
        }
    }
    else {
        Throwable("******** Native Ad Content Stream is NOT LOADED ***********").printStackTraceIfDebug()
        onNotAvailableAds.invoke()
    }
}

fun FragmentActivity.showInterstitialIfLoaded(
    onShow: () -> Unit = {},
    onClosed: (coins: Int) -> Unit = {},
    onNotLoaded: () -> Unit = {}
) {
    val loaded = Appodeal.isLoaded(Appodeal.INTERSTITIAL)
    if (loaded) {

        adsVM = ViewModelProvider(this@showInterstitialIfLoaded)[AdsViewModel::class]

        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
            override fun onInterstitialClicked() {
                return
            }

            override fun onInterstitialClosed() {
                onClosed.invoke(coins)
            }

            override fun onInterstitialExpired() {
                return
            }

            override fun onInterstitialFailedToLoad() {
                return
            }

            override fun onInterstitialLoaded(isPrecache: Boolean) {
                return
            }

            override fun onInterstitialShowFailed() {
                return
            }

            override fun onInterstitialShown() {
                if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                    val testRevenueInfo = createTestRevenueInfo(3, "Interstitial")
                    this@showInterstitialIfLoaded.setRevenueUpdateResult(testRevenueInfo)
                }
                onShow.invoke()
            }
        })
        Appodeal.show(this, Appodeal.INTERSTITIAL)
    }
    else {
        Throwable("******** Interstitial Ad is NOT LOADED ***********").printStackTraceIfDebug()
        onNotLoaded.invoke()
    }
}

fun FragmentActivity.showBannerViewIfLoaded(bannerId: Int) {

    Appodeal.setBannerViewId(bannerId)
    Appodeal.setBannerCallbacks(object : BannerCallbacks {
        override fun onBannerClicked() {
            return
        }

        override fun onBannerExpired() {
            return
        }

        override fun onBannerFailedToLoad() {
            Throwable("******** Banner view Ad is NOT LOADED ***********").printStackTraceIfDebug()
        }

        override fun onBannerLoaded(height: Int, isPrecache: Boolean) {
            Appodeal.show(this@showBannerViewIfLoaded, Appodeal.BANNER_VIEW)
        }

        override fun onBannerShowFailed() {
            return
        }

        override fun onBannerShown() {
            if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                val testRevenueInfo = createTestRevenueInfo(1, "Banner")
                this@showBannerViewIfLoaded.setRevenueUpdateResult(testRevenueInfo)
            }
        }

    })
}



















