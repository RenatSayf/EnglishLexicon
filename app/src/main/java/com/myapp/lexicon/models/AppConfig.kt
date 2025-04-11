package com.myapp.lexicon.models

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val adTypePerScreen: AdType,
    val bannerIds: BannerIds,
    val nativeIds: NativeIds,
    val interstitialAdIds: InterstitialIds,
    val rewardedIds: RewardedIds,
    val feedIds: FeedAdIds,
    val paymentsConditions: String,
    val selfEmployedThreshold: Int
) {
    @Serializable
    data class AdType(
        val main: Int,
        val service: Int,
        val test: Int,
        val translate: Int,
        val video: Int
    )
    @Serializable
    data class BannerIds(
        val main: String,
        val service: String,
        val editor: String,
        val translate: String
    )
    @Serializable
    data class InterstitialIds(
        val main: String,
        val service: String,
        val translate: String,
        val test: String,
        val video: String
    )
    @Serializable
    data class NativeIds(
        val main: String,
        val service: String,
        val translate: String,
        val test: String,
        val video: String
    )
    @Serializable
    data class RewardedIds(
        val main: String,
        val service: String,
        val translate: String,
        val test: String,
        val video: String
    )
    @Serializable
    data class FeedAdIds(
        val main: String,
        val service: String,
        val translate: String,
        val test: String,
        val video: String
    )
}