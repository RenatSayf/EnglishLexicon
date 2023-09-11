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
import com.appodeal.ads.utils.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.helpers.isNetworkAvailable
import com.myapp.lexicon.helpers.printLogIfDebug


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
    if (BuildConfig.DEBUG) {
        Appodeal.apply {
            setTesting(true)
            setLogLevel(Log.LogLevel.debug)
        }
    }
    else {
        Appodeal.apply {
            setTesting(false)
            setLogLevel(Log.LogLevel.verbose)
        }
    }
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
                    onSuccess.invoke()
                }
            }
        }
    )
}

fun Activity.showInterstitial(
    onShown: () -> Unit = {},
    onClosed: () -> Unit = {},
    onFailed: (String) -> Unit = {}
) {
    Appodeal.setInterstitialCallbacks(object : InterstitialCallbacks {
        override fun onInterstitialClicked() {}

        override fun onInterstitialClosed() {}

        override fun onInterstitialExpired() {}

        override fun onInterstitialFailedToLoad() {
            val message = "*** Interstitial ad failed to load ****"
            onFailed.invoke(message)
            printLogIfDebug(message)
        }

        override fun onInterstitialLoaded(isPrecache: Boolean) {}

        override fun onInterstitialShowFailed() {
            val message = "**** Interstitial ad show failed ****"
            onFailed.invoke(message)
            printLogIfDebug(message)
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
            val message = "*** Rewarded video failed to load ****"
            onFailed.invoke(message)
            printLogIfDebug(message)
        }

        override fun onRewardedVideoFinished(amount: Double, currency: String?) {}

        override fun onRewardedVideoLoaded(isPrecache: Boolean) {}

        override fun onRewardedVideoShowFailed() {
            val message = "***** Rewarded video show failed *****"
            onFailed.invoke(message)
            printLogIfDebug(message)
        }

        override fun onRewardedVideoShown() {
            onShown.invoke()
        }
    })

    val canShowInterstitial = Appodeal.canShow(Appodeal.INTERSTITIAL)
    if (canShowInterstitial) {
        Appodeal.show(this, Appodeal.INTERSTITIAL)
        return
    }
    else {
        val canShowVideo = Appodeal.canShow(Appodeal.REWARDED_VIDEO)
        if (canShowVideo) {
            Appodeal.show(this, Appodeal.REWARDED_VIDEO)
            return
        }
        else {
            val networkAvailable = this.isNetworkAvailable()
            if (networkAvailable) {
                //this.clearAdsData()
                this.adsInitialize(
                    Appodeal.REWARDED_VIDEO or Appodeal.INTERSTITIAL,
                    onSuccess = {
                        Appodeal.show(this, Appodeal.REWARDED_VIDEO or Appodeal.INTERSTITIAL)
                    }
                )
                return
            }
            else return
        }
    }
}

fun Fragment.showInterstitial(
    onShown: () -> Unit = {},
    onClosed: () -> Unit = {},
    onFailed: (String) -> Unit = {}
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
fun Context.clearAdsData() {
    val files = this.applicationContext.dataDir.listFiles()

    val dirFiles = files?.find {
        it.name == "files"
    }
    val fileList = dirFiles?.listFiles()
    fileList?.forEach {
        try {
            if (it.delete()) {
                printLogIfDebug("*********** ${it.name} has been deleted *****************")
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        }
    }

    val dirSharedPrefs = files?.find {
        it.name == "shared_prefs"
    }
    val prefsList = dirSharedPrefs?.listFiles()
    prefsList?.forEach {
        try {
            if (it.delete()) {
                printLogIfDebug("*********** ${it.name} has been deleted *****************")
            }
        }
        catch (e: Exception) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        }
    }
}





























