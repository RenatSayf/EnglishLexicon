package com.myapp.lexicon.ads

import android.content.Context
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
    "R-M-711878-3"
)

fun InterstitialAd.showInterstitialAd(
    callback: () -> Unit
) {
    this.let { ad ->
        ad.setInterstitialAdEventListener(object : InterstitialAdEventListener {
            override fun onAdLoaded() {}
            override fun onAdFailedToLoad(error: AdRequestError) {
                if (BuildConfig.DEBUG) {
                    val exception = Exception("**************** ${error.description} *******************")
                    exception.printStackTrace()
                }
                callback.invoke()
            }
            override fun onAdShown() {}
            override fun onAdDismissed() {
                callback.invoke()
            }
            override fun onAdClicked() {}
            override fun onLeftApplication() {}
            override fun onReturnedToApplication() {}
            override fun onImpression(p0: ImpressionData?) {}
        })
        ad.show()
    }
}

fun Context.loadBanner(
    index: Int,
    adView: BannerAdView,
    success: () -> Unit = {},
    error: (error: AdRequestError) -> Unit = {}
) {

    adView.apply {
        setAdUnitId(bannerAdIds[index])
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



























