package com.myapp.lexicon.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.myapp.lexicon.ads.banner.BannerAdIds
import com.myapp.lexicon.ads.feed_ad.FeedAdIds
import com.myapp.lexicon.ads.interstitial.InterstitialAdIds
import com.myapp.lexicon.ads.native_ad.NativeAdIds
import com.myapp.lexicon.ads.rewarded.RewardedAdIds
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.KEY_API
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.models.Tokens
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RemoteConfigViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: RemoteConfigViewModel

    @Before
    fun setUp() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{
                          "access_token": "",
                          "refresh_token": ""
                        }""".trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondError(status = HttpStatusCode.Forbidden)
            }
        }
        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repositoryModule.setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {}
            override fun onAuthorizationRequired() {}
        })

        viewModel = RemoteConfigViewModel(repositoryModule)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun encodeToJsonString() {

        val config = RemoteConfigViewModel.Config(
            adTypePerScreen = RemoteConfigViewModel.Config.AdType(
                main = 1,
                service = 1,
                test = 3,
                translate = 1,
                video = 3
            ),
            bannerIds = RemoteConfigViewModel.Config.BannerIds(
                main = BannerAdIds.BANNER_1.id,
                service = BannerAdIds.BANNER_1.id,
                editor = BannerAdIds.BANNER_2.id,
                translate = BannerAdIds.BANNER_3.id
            ),
            nativeIds = RemoteConfigViewModel.Config.NativeIds(
                main = NativeAdIds.NATIVE_1.id,
                service = NativeAdIds.NATIVE_1.id,
                translate = NativeAdIds.NATIVE_2.id,
                test = NativeAdIds.NATIVE_2.id,
                video = NativeAdIds.NATIVE_1.id
            ),
            interstitialAdIds = RemoteConfigViewModel.Config.InterstitialIds(
                main = InterstitialAdIds.INTERSTITIAL_1.id,
                service = InterstitialAdIds.INTERSTITIAL_1.id,
                translate = InterstitialAdIds.INTERSTITIAL_2.id,
                test = InterstitialAdIds.INTERSTITIAL_3.id,
                video = InterstitialAdIds.INTERSTITIAL_3.id
            ),
            rewardedIds = RemoteConfigViewModel.Config.RewardedIds(
                main = RewardedAdIds.REWARDED_1.id,
                service = RewardedAdIds.REWARDED_1.id,
                translate = RewardedAdIds.REWARDED_2.id,
                test = RewardedAdIds.REWARDED_3.id,
                video = RewardedAdIds.REWARDED_3.id
            ),
            feedIds = RemoteConfigViewModel.Config.FeedAdIds(
                main = FeedAdIds.FEED_1.id,
                service = FeedAdIds.FEED_1.id,
                translate = FeedAdIds.FEED_1.id,
                test = FeedAdIds.FEED_1.id,
                video = FeedAdIds.FEED_1.id
            )
        )

        val jsonString = viewModel.encodeToJsonString(config)

        Assert.assertTrue(jsonString != null)

    }

    @Test
    fun decodeFromString() {

        val jsonString = """{
  "adTypePerScreen" : {
    "main" : 1,
    "service" : 1,
    "test" : 3,
    "translate" : 1,
    "video" : 3
  },
  "bannerIds" : {
    "main" : "R-M-711878-1",
    "service" : "R-M-711878-1",
    "editor" : "R-M-711878-2",
    "translate" : "R-M-711878-3"
  },
  "nativeIds" : {
    "main" : "R-M-711878-14",
    "service" : "R-M-711878-14",
    "translate" : "R-M-711878-15",
    "test" : "R-M-711878-15",
    "video" : "R-M-711878-14"
  },
  "interstitialAdIds" : {
    "main" : "R-M-711878-4",
    "service" : "R-M-711878-4",
    "translate" : "R-M-711878-5",
    "test" : "R-M-711878-6",
    "video" : "R-M-711878-6"
  },
  "rewardedIds" : {
    "main" : "R-M-711878-10",
    "service" : "R-M-711878-10",
    "translate" : "R-M-711878-11",
    "test" : "R-M-711878-12",
    "video" : "R-M-711878-12"
  },
  "feedIds" : {
    "main" : "R-M-711878-18",
    "service" : "R-M-711878-18",
    "translate" : "R-M-711878-18",
    "test" : "R-M-711878-18",
    "video" : "R-M-711878-18"
  }
}"""
        val config = viewModel.decodeFromString(json = jsonString)

        Assert.assertTrue(config is RemoteConfigViewModel.Config)
    }

}