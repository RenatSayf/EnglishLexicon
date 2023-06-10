package com.myapp.lexicon.ads

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.appodeal.ads.InterstitialCallbacks
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.appodeal.ads.revenue.AdRevenueCallbacks
import com.appodeal.ads.revenue.RevenueInfo
import com.appodeal.ads.utils.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R


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
        activity = this,
        appKey = this.getString(R.string.appodeal_app_key),
        adTypes = adType,
        callback = object : ApdInitializationCallback {
            override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
                errors?.let {
                    onFailed.invoke(it)
                }?: run {
                    if (!BuildConfig.DEBUG) {
                        Appodeal.setTesting(true)
                        Appodeal.setLogLevel(Log.LogLevel.verbose)
                    }
                    else {
                        Appodeal.setTesting(false)
                        Appodeal.setLogLevel(Log.LogLevel.none)
                    }
                    onSuccess.invoke()
                }
                errors?.forEach {
                    if (BuildConfig.DEBUG) {
                        it.printStackTrace()
                    }
                }
            }
        }
    )
}

fun Activity.showInterstitial(
    onShown: () -> Unit = {},
    onClosed: () -> Unit = {},
    onFailed: () -> Unit = {}
) {
    Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
        override fun onInterstitialClicked() {}

        override fun onInterstitialClosed() {
            onClosed.invoke()
        }

        override fun onInterstitialExpired() {}

        override fun onInterstitialFailedToLoad() {
            onFailed.invoke()
        }

        override fun onInterstitialLoaded(isPrecache: Boolean) {}

        override fun onInterstitialShowFailed() {
            onFailed.invoke()
        }

        override fun onInterstitialShown() {
            onShown.invoke()
        }
    })
    if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
        Appodeal.show(this, Appodeal.INTERSTITIAL)
    }
}

fun Fragment.showInterstitial(
    onShown: () -> Unit = {},
    onClosed: () -> Unit = {},
    onFailed: () -> Unit = {}
) {
    requireActivity().showInterstitial(onShown, onClosed, onFailed)
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





























