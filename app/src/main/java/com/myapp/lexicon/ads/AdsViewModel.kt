package com.myapp.lexicon.ads

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.ext.emptyRevenue
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.screenHeight
import com.myapp.lexicon.helpers.screenWidth
import com.myapp.lexicon.models.AdsReward
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.settings.accessToken
import com.myapp.lexicon.settings.adsIsEnabled
import com.myapp.lexicon.settings.refreshToken
import com.myapp.lexicon.settings.saveAuthTokens
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt


class AdsViewModel(): ViewModel() {

    sealed class AdState {
        data object Init: AdState()
        data class Dismissed(val bonus: Double): AdState()
        data class Rewarded(val reward: AdsReward): AdState()
    }

    private var _adState = MutableLiveData<AdState>(AdState.Init)
    val adState: LiveData<AdState> = _adState

    fun setAdState(state: AdState) {
        _adState.value = state
    }

}


fun String.toAdData(
    onSuccess: (AdData) -> Unit,
    onFailed: () -> Unit
) {
    try {
        val adData = Json.decodeFromString<AdData>(this)
        onSuccess.invoke(adData)
    }
    catch (e: Exception) {
        e.printStackTraceIfDebug()
        onFailed.invoke()
    }
}

fun BannerAdView.loadBanner(
    activity: FragmentActivity,
    adId: String,
    heightRate: Double = 0.08
) {

    if (this.context.adsIsEnabled) {
        val adWidth = this.context.screenWidth
        val adHeight = (this.context.screenHeight * heightRate).roundToInt()
        val fixedSize = BannerAdSize.fixedSize(this.context, adWidth, adHeight)
        this.apply {
            val id = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                "demo-banner-yandex"
            } else {
                adId
            }
            setAdUnitId(id)
            setAdSize(fixedSize)
        }.loadAd(AdRequest.Builder().build())

        this.setBannerAdEventListener(object : BannerAdEventListener {

            private val repository: INetRepository = NetRepositoryModule().apply {
                setTokensUpdateListener(object : INetRepositoryModule.Listener {
                    override fun onUpdateTokens(tokens: Tokens) {
                        this@apply.setRefreshToken(tokens.refreshToken)
                        this@loadBanner.context.saveAuthTokens(tokens)
                    }
                    override fun onAuthorizationRequired() {}
                })
                setRefreshToken(this@loadBanner.context.refreshToken)
            }.provideNetRepository()

            override fun onAdClicked() {

            }

            override fun onAdFailedToLoad(error: AdRequestError) {

            }

            override fun onAdLoaded() {

            }

            override fun onImpression(impressionData: ImpressionData?) {
                impressionData?.rawData?.toAdData(
                    onSuccess = { data ->
                        val emptyRevenue = data.emptyRevenue()
                        val accessToken = this@loadBanner.context.accessToken
                        if (accessToken.isNotEmpty()) {
                            activity.lifecycleScope.launch(context = Dispatchers.Main) {
                                repository.updateUserBalance(accessToken, emptyRevenue)
                            }
                        }
                    },
                    onFailed = {}
                )
            }

            override fun onLeftApplication() {
                return
            }

            override fun onReturnedToApplication() {
                return
            }
        })
    }
}