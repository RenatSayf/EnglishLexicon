package com.myapp.lexicon.ads

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.myapp.lexicon.BuildConfig
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener


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
    "R-M-711878-4"
)
private const val testInterstitialAdId = "demo-interstitial-yandex"

fun InterstitialAd.showInterstitialAd(
    success: () -> Unit = {},
    error: (error: AdRequestError) -> Unit = {},
    dismiss: () -> Unit = {}
) {
    this.let { ad ->
        ad.setInterstitialAdEventListener(object : InterstitialAdEventListener {
            override fun onAdLoaded() {
                success.invoke()
            }
            override fun onAdFailedToLoad(err: AdRequestError) {
                if (BuildConfig.DEBUG) {
                    val exception = Exception("**************** ${err.description} *******************")
                    exception.printStackTrace()
                }
                error.invoke(err)
            }
            override fun onAdShown() {}
            override fun onAdDismissed() {
                dismiss.invoke()
            }
            override fun onAdClicked() {}
            override fun onLeftApplication() {}
            override fun onReturnedToApplication() {}
            override fun onImpression(p0: ImpressionData?) {}
        })
        ad.show()
    }
}

fun Context.loadInterstitialAd(
    index: Int,
    success: (ad: InterstitialAd) -> Unit = {},
    error: (error: AdRequestError) -> Unit = {},
    dismiss: () -> Unit = {}
) {
    val ad = InterstitialAd(this)
    ad.apply {
        if (BuildConfig.DEBUG) {
            setAdUnitId(testInterstitialAdId)
        }else {
            val id = try {
                interstitialAdIds[index]
            }
            catch (e: IndexOutOfBoundsException) {
                interstitialAdIds[0]
            }
            setAdUnitId(id)
        }
        setInterstitialAdEventListener(object : InterstitialAdEventListener {
            override fun onAdLoaded() {
                success.invoke(this@apply)
            }

            override fun onAdFailedToLoad(p0: AdRequestError) {
                error.invoke(p0)
            }

            override fun onAdShown() {}

            override fun onAdDismissed() {
                dismiss.invoke()
            }

            override fun onAdClicked() {}

            override fun onLeftApplication() {}

            override fun onReturnedToApplication() {}

            override fun onImpression(p0: ImpressionData?) {}
        })
    }
    val adRequest = AdRequest.Builder().build()
    ad.loadAd(adRequest)
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
            setAdUnitId(testBannerAdId)
        }else {
            val id = try {
                bannerAdIds[index]
            }
            catch (e: IndexOutOfBoundsException) {
                bannerAdIds[0]
            }
            setAdUnitId(id)
        }
        setAdSize(AdSize.stickySize(AdSize.FULL_SCREEN.width))
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

//fun DialogFragment.loadBanner(
//    index: Int,
//    adView: BannerAdView,
//    success: () -> Unit = {},
//    error: (error: AdRequestError) -> Unit = {}
//) {
//    requireContext().loadBanner(index, adView, success, error)
//}



























