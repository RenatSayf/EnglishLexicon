package com.myapp.lexicon.helpers

import android.content.Context
import com.myapp.lexicon.ads.loadBanner
import com.myapp.lexicon.ads.loadInterstitialAd
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.interstitial.InterstitialAd

class JavaKotlinMediator {

    fun loadInterstitialAd(context: Context, index: Int, listener: Listener?) {

        context.loadInterstitialAd(
            index,
            success = {
                listener?.onSuccess(it)
            },
            error = {
                listener?.onError(it)
            }
        )
    }

    fun loadBannerAd(context: Context, index: Int, adView: BannerAdView, listener: BannerAdListener?) {
        context.loadBanner(
            index = index,
            adView = adView,
            success = {
                listener?.onSuccess()
            },
            error = {
                listener?.onError(it)
            }
        )
    }

    interface Listener {
        fun onSuccess(ad: InterstitialAd)
        fun onError(error: AdRequestError)
    }

    interface BannerAdListener {
        fun onSuccess()
        fun onError(error: AdRequestError)
    }
}