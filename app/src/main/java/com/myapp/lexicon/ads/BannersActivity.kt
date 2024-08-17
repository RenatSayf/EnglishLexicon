@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.ads

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.window.OnBackInvokedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.databinding.ActivityBannersBinding
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.to2DigitsScale
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequestError
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class BannersActivity : AppCompatActivity() {

    companion object {
        const val ARG_ID_LIST = "BANNERS_IDS"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.orientationLock()

        binding = ActivityBannersBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        with(binding!!) {

            pbLoadAds.visibility = View.VISIBLE


            val listBannerIds = intent?.getStringArrayListExtra(ARG_ID_LIST)?.toList()

            if (!listBannerIds.isNullOrEmpty()) {

                val bannerAdIds = BannerAdIds.entries.filter {
                    listBannerIds.contains(it.id)
                }
                repeat(listBannerIds.size) {
                    val bannerView = BannerAdView(this@BannersActivity).apply {
                        layoutParams = LinearLayoutCompat.LayoutParams(
                            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        )
                    }
                    layoutBanners.addView(bannerView)
                }

                loadAndShowBanners(
                    this,
                    bannerAdIds,
                    onLoaded = {
                        pbLoadAds.visibility = View.GONE
                    },
                    onImpression = {data: AdData? ->
                        this@BannersActivity.adData = data

                        runBlocking {
                            delay(5000)
                            btnClose.visibility = View.VISIBLE
                        }
                    },
                    onFailed = {
                        btnClose.visibility = View.VISIBLE
                    }
                )
            }

            btnClose.setOnClickListener {
                listener?.onDismissed(this@BannersActivity.adData)
                finish()
            }
        }

    }

    private fun loadAndShowBanners(
        binding: ActivityBannersBinding,
        idList: List<BannerAdIds>,
        onLoaded: () -> Unit,
        onImpression: (data: AdData?) -> Unit,
        onFailed: () -> Unit
    ) {
        with(binding) {

            val adIds = idList.take(3)
            var loadCount = 0
            var errorCount = 0
            var impressionCount = 0
            val adData = AdData()
            val banners = layoutBanners.children.toList()
            try {
                (banners[0] as BannerAdView).loadBanner(
                    adId = adIds[0],
                    heightRate = 1.0 / adIds.size,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            errorCount++
                            Throwable(it.description).printStackTraceIfDebug()
                            if (errorCount >= adIds.size) {
                                onFailed.invoke()
                            }
                        }
                        if (loadCount >= adIds.size) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = { data: AdData? ->
                        impressionCount++
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
                            if (impressionCount >= adIds.size) {
                                onImpression.invoke(adData)
                            }
                        } else onImpression.invoke(null)
                    }
                )
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTraceIfDebug()
                onFailed.invoke()
            }

            try {
                (banners[1] as BannerAdView).loadBanner(
                    adId = adIds[1],
                    heightRate = 1.0 / adIds.size,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            errorCount++
                            Throwable(it.description).printStackTraceIfDebug()
                            if (errorCount >= adIds.size) {
                                onFailed.invoke()
                            }
                        }
                        if (loadCount >= adIds.size) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = { data: AdData? ->
                        impressionCount++
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
                            if (impressionCount >= adIds.size) {
                                onImpression.invoke(adData)
                            }
                        } else onImpression.invoke(null)
                    }
                )
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTraceIfDebug()
                onFailed.invoke()
            }

            try {
                (banners[2] as BannerAdView).loadBanner(
                    adId = adIds[2],
                    heightRate = 1.0 / adIds.size,
                    onCompleted = { error: AdRequestError? ->
                        loadCount++
                        error?.let {
                            errorCount++
                            Throwable(it.description).printStackTraceIfDebug()
                            if (errorCount >= adIds.size) {
                                onFailed.invoke()
                            }
                        }
                        if (loadCount >= adIds.size) {
                            onLoaded.invoke()
                        }
                    },
                    onImpression = { data: AdData? ->
                        impressionCount++
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
                            if (impressionCount >= adIds.size) {
                                onImpression.invoke(adData)
                            }
                        } else onImpression.invoke(null)
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

        listener?.onDismissed(this@BannersActivity.adData)
        finish()
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.callback = object : OnBackInvokedCallback {
                override fun onBackInvoked() {
                    listener?.onDismissed(this@BannersActivity.adData)
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
    this.startActivity(Intent(this, BannersActivity::class.java).apply {
        putExtras(Bundle().apply {
            putStringArrayList(
                BannersActivity.ARG_ID_LIST,
                arrayListOf(
                    BannerAdIds.BANNER_4.id,
                    BannerAdIds.BANNER_5.id
                )
            )
        })
    })
}