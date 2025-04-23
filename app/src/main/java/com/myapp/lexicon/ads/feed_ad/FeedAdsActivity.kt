package com.myapp.lexicon.ads.feed_ad

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.ads.ext.toRevenue
import com.myapp.lexicon.ads.models.TestAdData
import com.myapp.lexicon.common.AdsSource
import com.myapp.lexicon.databinding.ActivityFeedAdsBinding
import com.myapp.lexicon.di.App
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.screenWidth
import com.myapp.lexicon.main.ext.redirectToAuthScreen
import com.myapp.lexicon.models.AdsReward
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.settings.refreshToken
import com.myapp.lexicon.settings.saveAuthTokens
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.feed.FeedAd
import com.yandex.mobile.ads.feed.FeedAdAdapter
import com.yandex.mobile.ads.feed.FeedAdAppearance
import com.yandex.mobile.ads.feed.FeedAdEventListener
import com.yandex.mobile.ads.feed.FeedAdLoadListener
import com.yandex.mobile.ads.feed.FeedAdRequestConfiguration
import kotlin.math.roundToInt


class FeedAdsActivity : AppCompatActivity() {

    companion object {

        const val AD_ID = "AD_ID_35978123"

        private var listener: Listener? = null

        fun setAdDataListener(listener: Listener) {
            this.listener = listener
        }
    }

    private var binding: ActivityFeedAdsBinding? = null

    private var timer: CountDownTimer? = null

    interface Listener {
        fun onDismissed(reward: AdsReward)
        fun onError(error: String)
    }

    private val repository: INetRepository = NetRepositoryModule().apply {
        setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {
                this@apply.setRefreshToken(tokens.refreshToken)
                this@FeedAdsActivity.saveAuthTokens(tokens)
            }
            override fun onAuthorizationRequired() {
                this@FeedAdsActivity.redirectToAuthScreen()
            }
        })
        setRefreshToken(App.INSTANCE.refreshToken)
    }.provideNetRepository()

    private val testAdData: String
        get() = """{
      "currency": "RUB",
      "revenueUSD": "0.01321",
      "precision": "estimated",
      "revenue": "1.0",
      "requestId": "${System.currentTimeMillis()}-demo-feed-yandex",
      "blockId": "demo-feed-yandex",
      "adType": "feed",
      "ad_unit_id": "demo-feed-yandex",
      "network": {
        "name": "Yandex",
        "adapter": "Yandex",
        "ad_unit_id": "demo-feed-yandex"
      }
    }"""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFeedAdsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        if (BuildConfig.DEBUG && listener == null) throw NullPointerException("${this::class.java.simpleName}.setAdDataListener must be implemented")

        this.orientationLock()

        val adId = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) "demo-feed-yandex"
        else {
            val id = intent.extras?.getString(AD_ID)?: FeedAdIds.FEED_1.id
            id
        }

        val feedMarginDp = 16
        val screenWidthDp = (this.screenWidth / resources.displayMetrics.density).roundToInt()
        val cardWidthDp = screenWidthDp - 2 * feedMarginDp
        val cardCornerRadiusDp = 16.0

        val feedAdAppearance = FeedAdAppearance.Builder(cardWidthDp)
            .setCardCornerRadius(cardCornerRadiusDp)
            .build()

        val feedAdRequestConfiguration = FeedAdRequestConfiguration.Builder(adId).build()

        val feedAd = FeedAd.Builder(this, feedAdRequestConfiguration, feedAdAppearance).build()
        feedAd.apply {
            loadListener = object : FeedAdLoadListener {

                override fun onAdLoaded() {
                    return
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    listener?.onError(error.description)
                    finish()
                }
            }
        }.preloadAd()

        val feedAdAdapter = FeedAdAdapter(feedAd).apply {
            eventListener = object : FeedAdEventListener {
                override fun onAdClicked() {

                }

                override fun onImpression(impressionData: ImpressionData?) {
                    val impressData: ImpressionData? = if (BuildConfig.ADS_SOURCE == AdsSource.TEST_AD.name) {
                        TestAdData(testAdData)
                    }
                    else {
                        impressionData
                    }
                    val revenue = impressData?.rawData?.toRevenue()
                    if (revenue != null) {
                        revenue
                    }
                }
            }
        }
        binding?.rvFeedAds?.adapter = feedAdAdapter
    }

    override fun onDestroy() {

        this.orientationUnLock()

        super.onDestroy()
    }
}

fun Activity.startFeedAdsActivity(
    adId: String,
    onDismissed: (reward: AdsReward) -> Unit,
    onError: (error: String) -> Unit
) {
    FeedAdsActivity.setAdDataListener(object : FeedAdsActivity.Listener {
        override fun onDismissed(reward: AdsReward) {
            if (true) {
                try {
                    onDismissed.invoke(reward)
                } catch (e: Exception) {
                    e.printStackTraceIfDebug()
                }
            }
        }

        override fun onError(error: String) {
            onError.invoke(error)
        }

    })
    this.startActivity(Intent(this, FeedAdsActivity ::class.java).apply {
        putExtra(FeedAdsActivity.AD_ID, adId)
    })
}






