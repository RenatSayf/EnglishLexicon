package com.myapp.lexicon.ads

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.myapp.lexicon.BuildConfig
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
import kotlin.math.roundToInt


private val bannerAdIds = listOf(
    "R-M-711878-1",
    "R-M-711878-2",
    "R-M-711878-3",
    "R-M-711878-7",
    "R-M-711878-8"
)
private const val testBannerAdId = "demo-banner-yandex"

private val interstitialAdIds = listOf(
    "R-M-711878-6",
    "R-M-711878-5",
    "R-M-711878-4",
    "R-M-711878-9"
)
private const val testInterstitialAdId = "demo-interstitial-yandex"

fun InterstitialAd.showInterstitialAd(
    activity: Activity,
    success: () -> Unit = {},
    error: (error: AdError) -> Unit = {},
    dismiss: () -> Unit = {}
) {
    this.let { ad ->
        ad.setAdEventListener(object : InterstitialAdEventListener {

            override fun onAdShown() {
                success.invoke()
            }
            override fun onAdFailedToShow(p0: AdError) {
                if (BuildConfig.DEBUG) {
                    val exception = Exception("**************** ${p0.description} *******************")
                    exception.printStackTrace()
                }
                error.invoke(p0)
            }

            override fun onAdDismissed() {
                dismiss.invoke()
            }
            override fun onAdClicked() {}
            override fun onAdImpression(p0: ImpressionData?) {

            }
        })
        ad.show(activity)
    }
}

fun Context.loadInterstitialAd(
    index: Int,
    success: (ad: InterstitialAd) -> Unit = {},
    error: (error: AdRequestError) -> Unit = {},
    dismiss: () -> Unit = {}
) {
    val interstitialAdLoader = InterstitialAdLoader(this)
    interstitialAdLoader.apply {

        setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(p0: InterstitialAd) {
                success.invoke(p0)
            }

            override fun onAdFailedToLoad(p0: AdRequestError) {
                error.invoke(p0)
            }
        })
    }
    val adId = if (BuildConfig.DEBUG) {
        testInterstitialAdId
    }else {
        try {
            interstitialAdIds[index]
        }
        catch (e: IndexOutOfBoundsException) {
            interstitialAdIds[0]
        }
    }
    val adRequest = AdRequestConfiguration.Builder(adId).build()
    interstitialAdLoader.loadAd(adRequest)
}

fun Fragment.loadInterstitialAd(
    index: Int,
    success: (ad: InterstitialAd) -> Unit = {},
    error: (error: AdRequestError) -> Unit = {},
    dismiss: () -> Unit = {}
) {
    requireContext().loadInterstitialAd(index, success, error, dismiss)
}

fun Context.loadBanner(
    index: Int,
    adView: BannerAdView,
    success: () -> Unit = {},
    error: (error: AdRequestError) -> Unit = {}
) {

    adView.apply {
        if (BuildConfig.DEBUG) {
            try {
                setAdUnitId(testBannerAdId)
            } catch (e: IllegalArgumentException) {
                if (BuildConfig.DEBUG) e.printStackTrace()
            }
        }else {
            val id = try {
                bannerAdIds[index]
            }
            catch (e: IndexOutOfBoundsException) {
                bannerAdIds[0]
            }
            setAdUnitId(id)
        }
        try {
            val screenHeight = this@loadBanner.resources.displayMetrics.run { heightPixels / density }.roundToInt()
            val viewWidth = adView.width
            val bannerAdSize = BannerAdSize.inlineSize(this@loadBanner, viewWidth, screenHeight)
            setAdSize(bannerAdSize)
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {
                    success.invoke()
                }
                override fun onAdFailedToLoad(p0: AdRequestError) {
                    error.invoke(p0)
                }
                override fun onAdClicked() {}

                override fun onLeftApplication() {}

                override fun onReturnedToApplication() {}

                override fun onImpression(p0: ImpressionData?) {}
            })
            loadAd(AdRequest.Builder().build())
        } catch (e: IllegalArgumentException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        }
    }
}

fun Fragment.loadBanner(
    index: Int,
    adView: BannerAdView,
    success: () -> Unit = {},
    error: (error: AdRequestError) -> Unit = {}
) {
    requireContext().loadBanner(index, adView, success, error)
}
































