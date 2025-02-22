package com.myapp.lexicon.ads.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class AdName {
    NATIVE_MAIN,
    NATIVE_TRANSLATE,
    NATIVE_TEST,
    NATIVE_VIDEO,
    NATIVE_SERVICE,
    BANNER_MAIN,
    BANNER_TRANSLATE,
    BANNER_EDITOR,
    BANNER_SERVICE,
    BANNER_ACTIVITY_1,
    BANNER_ACTIVITY_2,
    INTERSTITIAL_MAIN,
    INTERSTITIAL_TRANSLATE,
    INTERSTITIAL_TEST,
    INTERSTITIAL_VIDEO,
    INTERSTITIAL_SERVICE
}

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
){
    var adCount = emptyMap<String, Int>()
}