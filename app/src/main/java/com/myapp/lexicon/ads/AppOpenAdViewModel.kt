package com.myapp.lexicon.ads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.IS_REWARD_ACCESSIBLE
import com.myapp.lexicon.settings.isUserRegistered
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData


class AppOpenAdViewModel(app: Application): AndroidViewModel(app) {

    private val appOpenAdLoader: AppOpenAdLoader = AppOpenAdLoader(app)
    private val adId = try {
        Firebase.remoteConfig.getString("AD_ON_OPEN_ID")
    } catch (e: Exception) {
        ""
    }
    private val adRequestConfiguration by lazy {
        if (adId.isNotEmpty()) AdRequestConfiguration.Builder(adId).build() else null
    }
    private val appOpenAdEventListener = AdEventListener()
    val isUserRegistered = app.isUserRegistered(onYes = {})

    private var _resultLoadOpenAd = MutableLiveData<Result<AppOpenAd>>()
    val resultLoadOpenAd: LiveData<Result<AppOpenAd>> = _resultLoadOpenAd

    private var _resultAdData: MutableLiveData<Result<AdData>?> = MutableLiveData(null)
    val resultAdData: LiveData<Result<AdData>?> = _resultAdData

    private var _bonus = MutableLiveData(Result.success(0.0))
    val bonus: LiveData<Result<Double>> = _bonus

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
            _bonus.value = Result.failure(Throwable())
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
            if (IS_REWARD_ACCESSIBLE) {
                impressionData?.rawData?.toAdData(
                    onSuccess = {data: AdData ->
                        if (isUserRegistered) {
                            adData = data
                        }
                    },
                    onFailed = {}
                )
            }
        }
    }

    init {
        appOpenAdLoader.setAdLoadListener(appOpenAdLoadListener)
        loadAppOpenAd()
    }


}