package com.myapp.lexicon.ads

import com.myapp.lexicon.BuildConfig
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener


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



























