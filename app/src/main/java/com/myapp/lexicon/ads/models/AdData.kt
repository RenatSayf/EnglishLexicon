package com.myapp.lexicon.ads.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdData(
    val adType: String?,
    @SerialName("ad_unit_id")
    val adUnitId: String?,
    val blockId: String?,
    val currency: String?,
    val network: Network?,
    val precision: String?,
    val requestId: String?,
    val revenue: Double = 0.0,
    val revenueUSD: Double = 0.0
)