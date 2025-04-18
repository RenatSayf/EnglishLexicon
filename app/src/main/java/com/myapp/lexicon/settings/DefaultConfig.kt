package com.myapp.lexicon.settings

import com.myapp.lexicon.ads.banner.BannerAdIds
import com.myapp.lexicon.ads.feed_ad.FeedAdIds
import com.myapp.lexicon.ads.interstitial.InterstitialAdIds
import com.myapp.lexicon.ads.native_ad.NativeAdIds
import com.myapp.lexicon.ads.rewarded.RewardedAdIds
import com.myapp.lexicon.models.AppConfig


var DEFAULT_CONFIG = AppConfig(
    adTypePerScreen = AppConfig.AdType(
        main = 1,
        service = 1,
        test = 3,
        translate = 1,
        video = 3
    ),
    bannerIds = AppConfig.BannerIds(
        main = BannerAdIds.BANNER_1.id,
        service = BannerAdIds.BANNER_1.id,
        editor = BannerAdIds.BANNER_2.id,
        translate = BannerAdIds.BANNER_3.id
    ),
    nativeIds = AppConfig.NativeIds(
        main = NativeAdIds.NATIVE_1.id,
        service = NativeAdIds.NATIVE_1.id,
        translate = NativeAdIds.NATIVE_2.id,
        test = NativeAdIds.NATIVE_2.id,
        video = NativeAdIds.NATIVE_1.id
    ),
    interstitialAdIds = AppConfig.InterstitialIds(
        main = InterstitialAdIds.INTERSTITIAL_1.id,
        service = InterstitialAdIds.INTERSTITIAL_1.id,
        translate = InterstitialAdIds.INTERSTITIAL_2.id,
        test = InterstitialAdIds.INTERSTITIAL_3.id,
        video = InterstitialAdIds.INTERSTITIAL_3.id
    ),
    rewardedIds = AppConfig.RewardedIds(
        main = RewardedAdIds.REWARDED_1.id,
        service = RewardedAdIds.REWARDED_1.id,
        translate = RewardedAdIds.REWARDED_2.id,
        test = RewardedAdIds.REWARDED_3.id,
        video = RewardedAdIds.REWARDED_3.id
    ),
    feedIds = AppConfig.FeedAdIds(
        main = FeedAdIds.FEED_1.id,
        service = FeedAdIds.FEED_1.id,
        translate = FeedAdIds.FEED_1.id,
        test = FeedAdIds.FEED_1.id,
        video = FeedAdIds.FEED_1.id
    ),
    paymentsConditions = "Следующая выплата вознаграждения состоится в период с 15 по 25 мая, при условии, что сумма будет больше",
    selfEmployedThreshold = 150000,
    paymentCode = "58742554" // TODO перед релизом изменить
)

const val DEFAULT_CONFIG_JSON = """{
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
  },
  "paymentsConditions" : "Следующая выплата вознаграждения состоится в период с 15 по 25 мая, при условии, что сумма будет больше",
  "selfEmployedThreshold" : 150000,
  "paymentCode" : "58742554"
}"""



