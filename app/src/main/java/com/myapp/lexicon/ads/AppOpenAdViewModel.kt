package com.myapp.lexicon.ads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.settings.accessToken
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData


class AppOpenAdViewModel(private val app: Application): AndroidViewModel(app) {

    private val appOpenAdLoader: AppOpenAdLoader = AppOpenAdLoader(app)
    private val adId = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) "demo-appopenad-yandex"
    else "R-M-711878-16"

    private val adRequestConfiguration by lazy {
        if (adId.isNotEmpty()) AdRequestConfiguration.Builder(adId).build() else null
    }
    private val appOpenAdEventListener = AdEventListener()

    private var _resultLoadOpenAd = MutableLiveData<Result<AppOpenAd>>()
    val resultLoadOpenAd: LiveData<Result<AppOpenAd>> = _resultLoadOpenAd

    private var _resultAdData: MutableLiveData<Result<AdData>?> = MutableLiveData(null)
    val resultAdData: LiveData<Result<AdData>?> = _resultAdData

    private val appOpenAdLoadListener = object : AppOpenAdLoadListener {
        override fun onAdLoaded(appOpenAd: AppOpenAd) {
            appOpenAd.setAdEventListener(appOpenAdEventListener)
            _resultLoadOpenAd.value = Result.success(appOpenAd)
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            _resultLoadOpenAd.value = Result.failure(Throwable(error.description))
        }
    }

    private fun loadAppOpenAd() {
        adRequestConfiguration?.let {
            appOpenAdLoader.loadAd(it)
        }?: run {
            _resultLoadOpenAd.value = Result.failure(Throwable())
        }
    }

    private inner class AdEventListener : AppOpenAdEventListener {
        private var adData: AdData? = null

        override fun onAdShown() {

        }

        override fun onAdFailedToShow(adError: AdError) {
            _resultAdData.value = null
        }

        override fun onAdDismissed() {
            if (adData != null) {
                _resultAdData.value = Result.success(adData!!)
                _resultAdData.value = null
            }
            else {
                _resultAdData.value = Result.failure(Throwable())
            }
        }

        override fun onAdClicked() {

        }

        override fun onAdImpression(impressionData: ImpressionData?) {
            impressionData?.rawData?.toAdData(
                onSuccess = {data: AdData ->
                    if (app.accessToken.isNotEmpty()) {
                        adData = data
                    }
                },
                onFailed = {}
            )
        }
    }

    init {
        appOpenAdLoader.setAdLoadListener(appOpenAdLoadListener)
        loadAppOpenAd()
    }


}