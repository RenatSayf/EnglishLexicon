package com.myapp.lexicon.ads.ext

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
import com.appodeal.ads.revenue.AdRevenueCallbacks
import com.appodeal.ads.revenue.RevenueInfo
import com.appodeal.ads.revenue.RevenuePlatform
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.RevenueViewModel
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.models.User
import kotlin.random.Random


private const val MULTIPLIER = 1000
private val DEBUG_REWARD_USD: Double
    get() {
        return Random(System.currentTimeMillis()).nextInt(1, 12) * 0.001
    }

fun createTestRevenueInfo(adType: Int, adTypeString: String): RevenueInfo {
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

fun FragmentActivity.revenueUpdateListener(onUpdate: (coins: Int, user: User) -> Unit) {

    Appodeal.setAdRevenueCallbacks(object : AdRevenueCallbacks {
        override fun onAdRevenueReceive(revenueInfo: RevenueInfo) {

            this@revenueUpdateListener.updateRevenueOnCloud(
                revenueInfo,
                onUpdate = { coins, user ->
                    onUpdate.invoke(coins, user)
                }
            )
        }

    })
}

fun FragmentActivity.updateRevenueOnCloud(
    revenueInfo: RevenueInfo,
    onUpdate: (coins: Int, user: User) -> Unit
) {
    val viewModel = ViewModelProvider(this@updateRevenueOnCloud)[RevenueViewModel::class]
    val adData = AdData(
        adType = revenueInfo.adTypeString,
        adUnitId = revenueInfo.demandSource,
        currency = revenueInfo.currency,
        requestId = System.currentTimeMillis().toString(),
        revenue = (revenueInfo.revenue * MULTIPLIER).toInt().toDouble(),
        revenueUSD = revenueInfo.revenue
    )
    val coins = adData.revenue.toInt()
    viewModel.updateUserRevenueIntoCloud(adData, onUpdated = { user ->
        onUpdate.invoke(coins, user)
    })
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
    onClosed: (coins: Int, user: User) -> Unit
) {
    if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {

        var thisCoins = 0
        var thisUser: User? = null

        this.revenueUpdateListener { coins, user ->
            thisCoins = coins
            thisUser = user
        }

        Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {

            override fun onRewardedVideoClicked() {
                return
            }

            override fun onRewardedVideoClosed(finished: Boolean) {
                thisUser?.let { user ->
                    if (thisCoins > 0) {
                        onClosed.invoke(thisCoins, user)
                    }
                }
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
                    this@loadAndShowRewardedAd.updateRevenueOnCloud(testRevenueInfo) { coins, user ->
                        thisCoins = coins
                        thisUser = user
                    }
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
    onNotLoaded: () -> Unit = {},
    onClosed: (coins: Int, user: User) -> Unit
) {
    val loaded = Appodeal.isLoaded(Appodeal.INTERSTITIAL)
    if (loaded) {

        var thisCoins = 0
        var thisUser: User? = null

        this.revenueUpdateListener { coins, user ->
            thisCoins = coins
            thisUser = user
        }

        Appodeal.setAdRevenueCallbacks(object : AdRevenueCallbacks {
            override fun onAdRevenueReceive(revenueInfo: RevenueInfo) {
                revenueInfo
            }
        })

        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
            override fun onInterstitialClicked() {
                return
            }

            override fun onInterstitialClosed() {
                thisUser?.let { user ->
                    if (thisCoins > 0) {
                        onClosed.invoke(thisCoins, user)
                    }
                }
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
                    this@showInterstitialIfLoaded.updateRevenueOnCloud(testRevenueInfo) { coins, user ->
                        thisCoins = coins
                        thisUser = user
                    }
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
                this@showBannerViewIfLoaded.updateRevenueOnCloud(testRevenueInfo) { coins, user ->

                }
            }
        }

    })
}



















