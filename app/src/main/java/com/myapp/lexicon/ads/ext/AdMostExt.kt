package com.myapp.lexicon.ads.ext

import admost.sdk.AdMostView
import admost.sdk.listener.AdMostBannerCallBack
import admost.sdk.listener.AdMostFullScreenCallBack
import android.view.View
import androidx.fragment.app.FragmentActivity


fun FragmentActivity.showBannerIfLoaded(zoneID: String) {

    val banner = AdMostView(this, zoneID, object : AdMostBannerCallBack() {
        override fun onReady(
            network: String?,
            ecpm: Int,
            adView: View?
        ) {
            super.onReady(network, ecpm, adView)
        }

        override fun onFail(errorCode: Int) {
            super.onFail(errorCode)
        }

        override fun onClick(network: String?) {
            super.onClick(network)
        }

        override fun onAdRevenuePaid(impressionData: AdMostFullScreenCallBack.AdMostImpressionData?) {
            super.onAdRevenuePaid(impressionData)
        }

        override fun onAdRefreshed(
            network: String?,
            ecpm: Int,
            adView: View?
        ) {
            super.onAdRefreshed(network, ecpm, adView)
        }
    }, null)
    banner.load()
}