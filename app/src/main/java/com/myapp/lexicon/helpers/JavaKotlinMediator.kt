package com.myapp.lexicon.helpers

import android.content.Context

class JavaKotlinMediator {

    fun loadInterstitialAd(context: Context, index: Int, listener: InterstitialAdListener?) {
        TODO("Not implemented")
    }

    fun showInterstitialAd(listener: AdDismissListener?) {
        TODO("Not implemented")
    }

    fun loadBannerAd(context: Context, listener: BannerAdListener?) {
        TODO("Not implemented")
    }

    interface InterstitialAdListener {
        fun onSuccess()
        fun onError()
    }

    interface BannerAdListener {
        fun onSuccess()
        fun onError()
    }

    interface AdDismissListener {
        fun onDismiss()
    }
}