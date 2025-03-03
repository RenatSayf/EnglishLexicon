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
import androidx.lifecycle.lifecycleScope
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.common.IS_REWARD_ACCESSIBLE
import com.myapp.lexicon.databinding.ActivityBannersBinding
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.to2DigitsScale
import com.yandex.mobile.ads.common.AdRequestError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BannersActivity : AppCompatActivity() {

    companion object {
        private var listener: Listener? = null
        fun setAdDataListener(listener: Listener) {
            this.listener = listener
        }
    }

    private var binding: ActivityBannersBinding? = null

    interface Listener {
        fun onDismissed(data: AdData?)
    }

    private var adData: AdData? = null
    private var callback: OnBackInvokedCallback? = null
    private var timer: CountDownTimer? = null
    private val ratingList: MutableMap<Int, Double> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.orientationLock()

        binding = ActivityBannersBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        with(binding!!) {

            pbLoadAds.visibility = View.VISIBLE


            loadAndShowBanners(
                this,
                onLoaded = {
                    pbLoadAds.visibility = View.GONE
                    rbTop.visibility = View.VISIBLE
                    rbBottom.visibility = View.VISIBLE
                },
                onImpression = {data: AdData? ->
                    this@BannersActivity.adData = data

                    val maxRatingValue = ratingList.values.maxOrNull()
                    if (ratingList.size >= 2 && maxRatingValue != null) {
                        rbTop.apply {
                            max = (maxRatingValue * 100).toInt()
                            progress = (ratingList[0]!! * 100).toInt()
                        }
                        rbBottom.apply {
                            max = (maxRatingValue * 100).toInt()
                            progress = (ratingList[1]!! * 100).toInt()
                        }
                    }

                    lifecycleScope.launch {
                        delay(5000)
                        layoutCloseAds.visibility = View.VISIBLE
                    }
                },
                onFailed = {
                    layoutCloseAds.visibility = View.VISIBLE
                },
                onAdClicked = {
                    finish()
                }
            )

            btnClose.setOnClickListener {
                finish()
            }

            timer = object : CountDownTimer(20000, 20000) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    layoutCloseAds.visibility = View.VISIBLE
                }
            }
            timer?.start()
        }

    }

    private fun loadAndShowBanners(
        binding: ActivityBannersBinding,
        onLoaded: () -> Unit,
        onImpression: (data: AdData?) -> Unit,
        onFailed: () -> Unit,
        onAdClicked: () -> Unit = {}
    ) {
        with(binding) {

            var loadCount = 0
            var errorCount = 0
            var impressionCount = 0
            val adData = AdData()

            try {
                bannerViewTop.loadBanner(
                    adId = BANNER_ACTIVITY_1,
                    heightRate = 0.5,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            errorCount++
                            Throwable(it.description).printStackTraceIfDebug()
                            if (errorCount >= 2) {
                                onFailed.invoke()
                            }
                        }
                        if (loadCount >= 2) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = { data: AdData? ->
                        impressionCount++
                        if (IS_REWARD_ACCESSIBLE) {
                            if (data != null) {
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
                                ratingList[0] = data.revenue
                                if (impressionCount >= 2) {
                                    onImpression.invoke(adData)
                                }
                            } else onImpression.invoke(null)
                        }
                    },
                    onAdClicked = {
                        onAdClicked.invoke()
                    }
                )
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTraceIfDebug()
                onFailed.invoke()
            }

            try {
                bannerViewBottom.loadBanner(
                    adId = BANNER_ACTIVITY_2,
                    heightRate = 0.5,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            errorCount++
                            Throwable(it.description).printStackTraceIfDebug()
                            if (errorCount >= 2) {
                                onFailed.invoke()
                            }
                        }
                        if (loadCount >= 2) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = { data: AdData? ->
                        impressionCount++
                        if (IS_REWARD_ACCESSIBLE) {
                            if (data != null) {
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
                                ratingList[1] = data.revenue
                                if (impressionCount >= 2) {
                                    onImpression.invoke(adData)
                                }
                            } else onImpression.invoke(null)
                        }
                    },
                    onAdClicked = {
                        onAdClicked.invoke()
                    }
                )
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTraceIfDebug()
                onFailed.invoke()
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {

        finish()
        super.onBackPressed()
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

        listener?.onDismissed(this@BannersActivity.adData)
        timer?.cancel()
        timer = null
        binding = null
        listener = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.callback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
        }
        this.orientationUnLock()
        super.onDestroy()
    }
}

fun Activity.startBannersActivity(
    onImpression: (data: AdData?) -> Unit,
    onDismissed: (bonus: Double) -> Unit
) {
    BannersActivity.setAdDataListener(object : BannersActivity.Listener {
        override fun onDismissed(data: AdData?) {
            if (data != null) {
                onImpression.invoke(data)
                val bonus = (data.revenue * UserViewModel.USER_PERCENTAGE).to2DigitsScale()
                onDismissed.invoke(bonus)
            }
        }
    })
    this.startActivity(Intent(this, BannersActivity::class.java))
}