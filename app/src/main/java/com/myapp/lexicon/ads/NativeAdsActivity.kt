package com.myapp.lexicon.ads

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.window.OnBackInvokedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.databinding.ActivityNativeAdsBinding
import com.myapp.lexicon.helpers.logIfDebug
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdEventListener
import com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoadListener
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoader

class NativeAdsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNativeAdsBinding

    private var nativeAdLoader: NativeBulkAdLoader? = null

    private var timer: CountDownTimer? = null
    interface Listener {
        fun onDismissed(data: AdData?)
    }

    private var adData: AdData? = null
    private var callback: OnBackInvokedCallback? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNativeAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            nativeAdLoader = NativeBulkAdLoader(this@NativeAdsActivity)
            nativeAdLoader?.loadAds(
                nativeAdRequestConfiguration = NativeAdRequestConfiguration.Builder("demo-native-app-yandex").apply {
                    setShouldLoadImagesAutomatically(true)
                }.build(),
                2
            )
            nativeAdLoader?.setNativeBulkAdLoadListener(object : NativeBulkAdLoadListener {
                override fun onAdsFailedToLoad(error: AdRequestError) {
                    error.description.logIfDebug()
                }

                override fun onAdsLoaded(nativeAds: List<NativeAd>) {
                    pbLoadAds.visibility = View.GONE
                    nativeBannerTop.setAd(nativeAds[0].apply {
                        setNativeAdEventListener(topBannerListener)
                    })
                    nativeBannerBottom.setAd(nativeAds[1].apply {
                        setNativeAdEventListener(bottomBannerListener)
                    })

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

    private val topBannerListener = object : NativeAdEventListener {
        override fun onAdClicked() {

        }

        override fun onImpression(impressionData: ImpressionData?) {

        }

        override fun onLeftApplication() {

        }

        override fun onReturnedToApplication() {

        }
    }

    private val bottomBannerListener = object : NativeAdEventListener {
        override fun onAdClicked() {

        }

        override fun onImpression(impressionData: ImpressionData?) {

        }

        override fun onLeftApplication() {

        }

        override fun onReturnedToApplication() {

        }

    }

    override fun onDestroy() {

        timer
        super.onDestroy()
    }
}