package com.myapp.lexicon.ads

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.RewardedVideoCallbacks
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.appodeal.ads.revenue.AdRevenueCallbacks
import com.appodeal.ads.revenue.RevenueInfo
import com.appodeal.ads.utils.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.models.LaunchMode


fun Context.getAdvertisingID(
    onSuccess: (String) -> Unit,
    onUnavailable: () -> Unit,
    onFailure: (String) -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val client = AdvertisingIdClient(this)
    var thread: Thread? = null
    thread = Thread {
        try {
            client.start()
            val clientInfo = client.info
            val id = clientInfo.id
            id?.let {
                onSuccess.invoke(it)
            }?: run {
                onUnavailable.invoke()
            }
        } catch (e: Exception) {
            onFailure.invoke(e.message?: "Unknown error")
        }
        finally {
            thread?.let {
                if (it.isAlive) {
                    it.interrupt()
                }
            }
            onComplete.invoke()
        }
    }
    thread.start()
}

fun Activity.adsInitialize(
    adType: Int,
    onSuccess: () -> Unit = {},
    onFailed: (List<ApdInitializationError>) -> Unit = {}
) {
    Appodeal.initialize(
        context = this,
        appKey = this.getString(R.string.appodeal_app_key),
        adTypes = adType,
        callback = object : ApdInitializationCallback {
            override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
                errors?.let { err ->
                    onFailed.invoke(err)
                    err.forEach {
                        if (BuildConfig.DEBUG) {
                            it.printStackTrace()
                        }
                    }
                }?: run {
                    if (BuildConfig.DEBUG) {
                        Appodeal.setTesting(true)
                        Appodeal.setLogLevel(Log.LogLevel.debug)
                    }
                    else {
                        Appodeal.apply {
                            setTesting(false)
                            setLogLevel(Log.LogLevel.none)
                        }
                    }
                    onSuccess.invoke()
                }
            }
        }
    )
}

fun Activity.showInterstitial(
    adType: Int,
    onShown: () -> Unit = {},
    onClosed: () -> Unit = {},
    onFailed: (String) -> Unit = {}
) {
    Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
        override fun onInterstitialClicked() {}

        override fun onInterstitialClosed() {}

        override fun onInterstitialExpired() {}

        override fun onInterstitialFailedToLoad() {
            onFailed.invoke("*** Interstitial ad failed to load ****")
        }

        override fun onInterstitialLoaded(isPrecache: Boolean) {}

        override fun onInterstitialShowFailed() {
            onFailed.invoke("**** Interstitial ad show failed ****")
        }

        override fun onInterstitialShown() {
            onShown.invoke()
        }
    })

    Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {
        override fun onRewardedVideoClicked() {
            return
        }

        override fun onRewardedVideoClosed(finished: Boolean) {
            onClosed.invoke()
        }

        override fun onRewardedVideoExpired() {}

        override fun onRewardedVideoFailedToLoad() {
            onFailed.invoke("*** Rewarded video failed to load ****")
        }

        override fun onRewardedVideoFinished(amount: Double, currency: String?) {}

        override fun onRewardedVideoLoaded(isPrecache: Boolean) {}

        override fun onRewardedVideoShowFailed() {
            onFailed.invoke("***** Rewarded video show failed *****")
        }

        override fun onRewardedVideoShown() {
            onShown.invoke()
        }
    })

    val canShowInterstitial = Appodeal.canShow(Appodeal.INTERSTITIAL)
    if (adType == Appodeal.INTERSTITIAL && !canShowInterstitial) {
        Appodeal.show(this, Appodeal.REWARDED_VIDEO)
    }
    else Appodeal.show(this, adType)
}

fun Fragment.showInterstitial(
    adType: Int,
    onShown: () -> Unit = {},
    onClosed: () -> Unit = {},
    onFailed: (String) -> Unit = {}
) {
    requireActivity().showInterstitial(adType, onShown, onClosed, onFailed)
}

fun FrameLayout.showBanner(activity: Activity) {
    val bannerView = Appodeal.getBannerView(this.context)
    this.addView(bannerView)
    val initialized = Appodeal.isInitialized(Appodeal.BANNER)
    if (!initialized) {
        if (BuildConfig.DEBUG) {
            Throwable("******** Ad not yet initialized ***********").printStackTrace()
        }
        return
    }
    Appodeal.setBannerCallbacks(object : BannerCallbacks {
        override fun onBannerClicked() {}

        override fun onBannerExpired() {}

        override fun onBannerFailedToLoad() {
            if (BuildConfig.DEBUG) {
                Throwable("********* Banner failed to load *********").printStackTrace()
            }
        }

        override fun onBannerLoaded(height: Int, isPrecache: Boolean) {}

        override fun onBannerShowFailed() {
            if (BuildConfig.DEBUG) {
                Throwable("********* Banner failed to show *********").printStackTrace()
            }
        }

        override fun onBannerShown() {}
    })
    Appodeal.show(activity, Appodeal.BANNER_VIEW)
}

fun Activity.adRevenueInfo(
    onInfo: (RevenueInfo) -> Unit
) {
    Appodeal.setAdRevenueCallbacks(object : AdRevenueCallbacks {
        override fun onAdRevenueReceive(revenueInfo: RevenueInfo) {
            onInfo.invoke(revenueInfo)
        }
    })
}





























