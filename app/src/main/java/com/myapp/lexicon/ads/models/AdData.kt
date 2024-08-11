package com.myapp.lexicon.ads.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdData(
    var adType: String? = null,
    @SerialName("ad_unit_id")
    var adUnitId: String? = null,
    var blockId: String? = null,
    var currency: String? = null,
    var network: Network? = null,
    var precision: String? = null,
    var requestId: String? = null,
    var revenue: Double = 0.0,
    var revenueUSD: Double = 0.0
)