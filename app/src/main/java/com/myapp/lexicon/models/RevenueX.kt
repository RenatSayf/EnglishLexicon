package com.myapp.lexicon.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RevenueX(

    @SerialName("currency_code")
    val currencyCode: String,

    @SerialName("last_ad_id")
    val lastAdId: String,

    @SerialName("revenue_rub")
    val revenueRub: Double,

    @SerialName("revenue_usd")
    val revenueUsd: Double
)