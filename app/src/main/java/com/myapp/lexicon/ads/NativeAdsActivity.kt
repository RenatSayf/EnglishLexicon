@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.ads

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.window.OnBackInvokedCallback
import androidx.appcompat.app.AppCompatActivity
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.databinding.ActivityNativeAdsBinding
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.to2DigitsScale
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdEventListener
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoader

class NativeAdsActivity : AppCompatActivity() {

    companion object {
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
    }

    private var adData: AdData = AdData()
    private var callback: OnBackInvokedCallback? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNativeAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.orientationLock()

        with(binding) {

            nativeAdLoader = NativeBulkAdLoader(this@NativeAdsActivity)
            nativeAdLoader?.loadAds(
                nativeAdRequestConfiguration = NativeAdRequestConfiguration.Builder(NativeAdIds.NATIVE_1.id).apply {
                    setShouldLoadImagesAutomatically(true)
                }.build(),
                2
            )
            nativeAdLoader?.setNativeBulkAdLoadListener(object : NativeBulkAdLoadListener {
                override fun onAdsFailedToLoad(error: AdRequestError) {
                    error.description.logIfDebug()
                    finish()
                }

                override fun onAdsLoaded(nativeAds: List<NativeAd>) {
                    pbLoadAds.visibility = View.GONE
                    nativeBannerTop.visibility = View.VISIBLE
                    nativeBannerBottom.visibility = View.VISIBLE
                    val nativeAd1 = nativeAds.firstOrNull()
                    if (nativeAd1 != null) {
                        nativeBannerTop.setAd(nativeAd1.apply {
                            setNativeAdEventListener(nativeAdListener)
                        })
                    }
                    val nativeAd2 = nativeAds.lastOrNull()
                    if (nativeAd2 != null) {
                        nativeBannerBottom.setAd(nativeAd2.apply {
                            setNativeAdEventListener(nativeAdListener)
                        })
                    }
                }
            })

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
        override fun onAdClicked() {}

        override fun onImpression(impressionData: ImpressionData?) {
            impressionData?.rawData?.toAdData(
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
                },
                onFailed = {}
            )
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
            if (this.callback != null) {
                onBackInvokedDispatcher.registerOnBackInvokedCallback(0,
                    this.callback as OnBackInvokedCallback
                )
            }
        }
    }

    override fun onDestroy() {

        listener?.onDismissed(adData)?: throw NullPointerException("${this::class.java.simpleName}.setAdDataListener must be installed")
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
    onImpression: (data: AdData?) -> Unit,
    onDismissed: (bonus: Double) -> Unit
) {
    NativeAdsActivity.setAdDataListener(object : NativeAdsActivity.Listener {
        override fun onDismissed(data: AdData?) {
            if (data != null) {
                onImpression.invoke(data)
                val bonus = (data.revenue * UserViewModel.USER_PERCENTAGE).to2DigitsScale()
                onDismissed.invoke(bonus)
            }
        }
    })
    this.startActivity(Intent(this, NativeAdsActivity::class.java))
}