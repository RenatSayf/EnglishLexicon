package com.myapp.lexicon.models.config

import kotlinx.serialization.SerialName

data class RemoteConfig(
    @SerialName("is_ads_enabled")
    val isAdsEnabled: Boolean = true,

    @SerialName(value = "reward_ratio")
    val rewardRatio: Double = 0.5,

    @SerialName(value = "payout_threshold")
    val payoutThreshold: Int = 100
)