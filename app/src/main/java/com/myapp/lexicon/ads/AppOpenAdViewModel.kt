package com.myapp.lexicon.ads

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.IS_REWARD_ACCESSIBLE
import com.myapp.lexicon.di.App
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.settings.isUserRegistered
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

class AppOpenAdViewModel(app: App): AndroidViewModel(app) {

    private val appOpenAdLoader: AppOpenAdLoader = AppOpenAdLoader(app)
    private val adId = "demo-appopenad-yandex"
    private val adRequestConfiguration = AdRequestConfiguration.Builder(adId).build()
    private val appOpenAdEventListener = AdEventListener()
    val isUserRegistered = app.isUserRegistered(onYes = {})

    private var _resultOpenAd = MutableLiveData<Result<AppOpenAd>>()
    val resultOpenAd: LiveData<Result<AppOpenAd>> = _resultOpenAd

    private var _resultAdData = MutableLiveData<Result<AdData>>()
    val resultAdData: LiveData<Result<AdData>> = _resultAdData

    private var _bonus = MutableLiveData(Result.success(0.0))
    val bonus: LiveData<Result<Double>> = _bonus

    private val appOpenAdLoadListener = object : AppOpenAdLoadListener {
        override fun onAdLoaded(appOpenAd: AppOpenAd) {
            appOpenAd.setAdEventListener(appOpenAdEventListener)
            _resultOpenAd.value = Result.success(appOpenAd)
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            _resultOpenAd.value = Result.failure(Throwable(error.description))
        }
    }

    private fun loadAppOpenAd() {
        appOpenAdLoader.loadAd(adRequestConfiguration)
    }

    private inner class AdEventListener : AppOpenAdEventListener {
        override fun onAdShown() {

        }

        override fun onAdFailedToShow(adError: AdError) {
            _resultAdData.value = Result.failure(Throwable())
            _bonus.value = Result.failure(Throwable())
        }

        override fun onAdDismissed() {
            _resultAdData.value?.onSuccess { data: AdData ->
                _bonus.value = Result.success((data.revenue * UserViewModel.USER_PERCENTAGE).to2DigitsScale())
            }
        }

        override fun onAdClicked() {

        }

        override fun onAdImpression(impressionData: ImpressionData?) {
            if (IS_REWARD_ACCESSIBLE) {
                impressionData?.rawData?.toAdData(
                    onSuccess = {data: AdData ->
                        if (isUserRegistered) {
                            _resultAdData.value = Result.success(data)
                        }
                        else {
                            _resultAdData.value = Result.failure(Throwable())
                        }
                    },
                    onFailed = {
                        _resultAdData.value = Result.failure(Throwable())
                    }
                )
            }
        }
    }





    init {
        appOpenAdLoader.setAdLoadListener(appOpenAdLoadListener)
        loadAppOpenAd()
    }


}