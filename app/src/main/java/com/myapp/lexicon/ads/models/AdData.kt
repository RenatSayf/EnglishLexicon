package com.myapp.lexicon.ads.models

data class AdData(
    val adType: String,
    val ad_unit_id: String,
    val blockId: String,
    val currency: String,
    val network: Network,
    val precision: String,
    val requestId: String,
    val revenue: Double,
    val revenueUSD: Double
)