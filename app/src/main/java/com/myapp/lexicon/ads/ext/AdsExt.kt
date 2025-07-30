package com.myapp.lexicon.ads.ext

import androidx.fragment.app.FragmentActivity
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
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.models.Reward
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.random.Random

private val DEBUG_REWARD_USD: Double
    get() {
        return Random(System.currentTimeMillis()).nextInt(5, 21) * 0.001
    }

fun FragmentActivity.loadAndShowRewardedAd(
    onNotLoaded: () -> Unit = {},
    onClosed: () -> Unit = {}
) {
    if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
        if (Appodeal.canShow(Appodeal.REWARDED_VIDEO)) {
            Appodeal.show(this, Appodeal.REWARDED_VIDEO)
        }

        Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {

            override fun onRewardedVideoClicked() {
                return
            }

            override fun onRewardedVideoClosed(finished: Boolean) {
                onClosed.invoke()
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
                return
            }
        })
    }
    else {
        Throwable("******** Rewarded Ad is NOT LOADED ***********").printStackTraceIfDebug()
        onNotLoaded.invoke()
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

                Appodeal.setAdRevenueCallbacks(object : AdRevenueCallbacks {
                    override fun onAdRevenueReceive(revenueInfo: RevenueInfo) {
                        revenueInfo
                    }
                })
                onCompleted.invoke()
            }
        }
    )
}

fun List<NativeAdView>.showIfLoaded(
    onNotLoaded: () -> Unit = {},
    onShow: () -> Unit = {}
) {

    val adsCount = Appodeal.getAvailableNativeAdsCount()
    if (adsCount >= 0) {

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
        val nativeAds = Appodeal.getNativeAds(this.size)
        nativeAds.forEachIndexed { index, ad ->
            this[index].registerView(ad)
        }
    }
    else {
        Throwable("******** Native Ad Content Stream is NOT LOADED ***********").printStackTraceIfDebug()
        onNotLoaded.invoke()
    }
}

fun FragmentActivity.showInterstitialIfLoaded(
    onShow: () -> Unit = {},
    onClosed: (reward: Reward?) -> Unit = {},
    onNotLoaded: () -> Unit = {}
) {
    val loaded = Appodeal.isLoaded(Appodeal.INTERSTITIAL)
    if (loaded) {

        Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
            override fun onInterstitialClicked() {
                return
            }

            override fun onInterstitialClosed() {
                onClosed.invoke(null)
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

        }

    })
}



















