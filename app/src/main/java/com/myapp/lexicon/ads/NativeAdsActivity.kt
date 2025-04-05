@file:Suppress("ObjectLiteralToLambda", "RedundantSamConstructor")

package com.myapp.lexicon.ads

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ScrollView
import android.window.OnBackInvokedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.ext.toRevenue
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.ads.models.TestAdData
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.common.IS_REWARD_ACCESSIBLE
import com.myapp.lexicon.databinding.ActivityNativeAdsBinding
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.main.ext.redirectToAuthScreen
import com.myapp.lexicon.models.AdsReward
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.settings.accessToken
import com.myapp.lexicon.settings.saveAuthTokens
import com.myapp.lexicon.settings.userPercentFromPref
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdEventListener
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoader
import kotlinx.coroutines.launch



class NativeAdsActivity : AppCompatActivity() {

    companion object {
        const val KEY_AD_ID = "KEY_AD_ID_25488714"
        private var listener: Listener? = null
        fun setAdDataListener(listener: Listener) {
            this.listener = listener
        }
    }

    private lateinit var binding: ActivityNativeAdsBinding

    private var nativeAdLoader: NativeBulkAdLoader? = null

    private var timer: CountDownTimer? = null
    interface Listener {
        fun onDismissed(data: AdData?)
        fun onClosing(reward: AdsReward)
    }

    private var adData: AdData = AdData()
    private var reward: AdsReward = AdsReward("", 0.0, 0.0, 0.0)
    private var callback: OnBackInvokedCallback? = null
    private val ratingList: MutableList<Double> = mutableListOf()

    private val repository: INetRepository = NetRepositoryModule().apply {
        setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {
                this@apply.setRefreshToken(tokens.refreshToken)
                this@NativeAdsActivity.saveAuthTokens(tokens)
            }
            override fun onAuthorizationRequired() {
                this@NativeAdsActivity.redirectToAuthScreen()
            }
        })
    }.provideNetRepository()

    private val testAdData: String
        get() = """{
      "currency": "RUB",
      "revenueUSD": "0.04321",
      "precision": "estimated",
      "revenue": "4.0",
      "requestId": "${System.currentTimeMillis()}-demo-native-app-yandex",
      "blockId": "demo-rewarded-yandex",
      "adType": "interstitial",
      "ad_unit_id": "demo-native-app-yandex",
      "network": {
        "name": "Yandex",
        "adapter": "Yandex",
        "ad_unit_id": "demo-native-app-yandex"
      }
    }"""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNativeAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG && listener == null) throw NullPointerException("${this::class.java.simpleName}.setAdDataListener must be installed")

        this.orientationLock()

        with(binding) {

            pbLoadAds.visibility = View.VISIBLE

            nativeAdLoader = NativeBulkAdLoader(this@NativeAdsActivity)
            val adId = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) "demo-native-app-yandex"
            else {
                intent.extras?.getString(KEY_AD_ID)?: NATIVE_AD_MAIN
            }

            nativeAdLoader?.setNativeBulkAdLoadListener(object : NativeBulkAdLoadListener {
                override fun onAdsFailedToLoad(error: AdRequestError) {
                    error.description.logIfDebug()
                    finish()
                }

                override fun onAdsLoaded(nativeAds: List<NativeAd>) {
                    pbLoadAds.visibility = View.GONE
                    layoutAds.visibility = View.VISIBLE

                    if (nativeAds.size >= 3) {
                        nativeBannerTop.setAd(nativeAds[0].apply {
                            setNativeAdEventListener(nativeAdListener)
                        })
                        nativeBannerCenter.setAd(nativeAds[1].apply {
                            setNativeAdEventListener(nativeAdListener)
                        })
                        nativeBannerBottom.setAd(nativeAds[2].apply {
                            setNativeAdEventListener(nativeAdListener)
                        })
                    }
                    scrollAds.postDelayed(Runnable {
                        scrollAds.fullScroll(ScrollView.FOCUS_DOWN)
                    }, 2000)
                }
            })

            nativeAdLoader?.loadAds(
                nativeAdRequestConfiguration = NativeAdRequestConfiguration.Builder(adId).apply {
                    setShouldLoadImagesAutomatically(true)
                }.build(),
                adsCount = 3
            )

            btnClose.setOnClickListener {

                finish()
            }

            timer = object : CountDownTimer(20000, 20000) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    btnClose.visibility = View.VISIBLE
                }
            }
            timer?.start()
        }

    }

    private val nativeAdListener = object : NativeAdEventListener {

        override fun onAdClicked() {
            this@NativeAdsActivity.finish()
        }

        override fun onImpression(impressionData: ImpressionData?) {

            if (IS_REWARD_ACCESSIBLE) {

                val impressData: ImpressionData? = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                    TestAdData(testAdData)
                }
                else {
                    impressionData
                }
                impressData?.rawData?.toAdData(
                    onSuccess = { data: AdData ->
                        adData.let {
                            it.adType = data.adType
                            it.adUnitId = data.adUnitId
                            it.blockId = data.blockId
                            it.currency = data.currency
                            it.network = data.network
                            it.precision = data.precision
                            it.requestId = data.requestId
                            it.revenue += data.revenue
                            it.revenueUSD += data.revenueUSD
                        }
                        ratingList.add(data.revenue)
                        if (ratingList.size >= 3) {
                            val maxValue = ratingList.maxOrNull()
                            if (maxValue != null) {
                                with(binding){
                                    rbTop.apply {
                                        max = (maxValue * 100).toInt()
                                        progress = (ratingList[0] * 100).toInt()
                                    }
                                    rbCenter.apply {
                                        max = (maxValue * 100).toInt()
                                        progress = (ratingList[1] * 100).toInt()
                                    }
                                    rbBottom.apply {
                                        max = (maxValue * 100).toInt()
                                        progress = (ratingList[2] * 100).toInt()
                                    }
                                }
                            }
                            lifecycleScope.launch {
                                repository.updateUserBalance(
                                    accessToken = this@NativeAdsActivity.accessToken,
                                    revenue = adData.toRevenue()
                                ).collect(collector = { result ->
                                    result.onSuccess { reward ->
                                        this@NativeAdsActivity.reward = reward
                                    }
                                    binding.btnClose.visibility = View.VISIBLE
                                })
                                //delay(5000)
                            }
                        }
                    },
                    onFailed = {
                        Exception("********* ImpressionData.rawData.toAdData() - parsing error ***********").printStackTraceIfDebug()
                    }
                )
            }
        }

        override fun onLeftApplication() {}

        override fun onReturnedToApplication() {}
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.callback = object : OnBackInvokedCallback {
                override fun onBackInvoked() {
                    finish()
                }
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(0, this.callback as OnBackInvokedCallback)
        }
    }

    override fun onDestroy() {

        listener?.onDismissed(adData)
        listener?.onClosing(this.reward)
        timer?.cancel()
        timer = null
        listener = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.callback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
        }
        this.orientationUnLock()
        super.onDestroy()
    }

}

fun Activity.startNativeAdsActivity(
    adId: String = NativeAdIds.NATIVE_1.id,
    onImpression: (data: AdData?) -> Unit,
    onDismissed: (bonus: Double) -> Unit,
    onClosing: (reward: AdsReward) -> Unit
) {
    NativeAdsActivity.setAdDataListener(object : NativeAdsActivity.Listener {
        override fun onDismissed(data: AdData?) {
            if (data != null) {
                try {
                    onImpression.invoke(data)
                    val bonus = (data.revenue * this@startNativeAdsActivity.userPercentFromPref).to2DigitsScale()
                    onDismissed.invoke(bonus)
                } catch (e: Exception) {
                    e.printStackTraceIfDebug()
                }
            }
        }
        override fun onClosing(reward: AdsReward) {
            onClosing.invoke(reward)
        }
    })
    this.startActivity(Intent(this, NativeAdsActivity::class.java).apply {
        putExtra(NativeAdsActivity.KEY_AD_ID, adId)
    })
}