package com.myapp.lexicon.models

import com.myapp.lexicon.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RevenueX(

    @SerialName("currency_code")
    val currencyCode: String,

    @SerialName("ad_request_id")
    val adRequestID: String,

    @SerialName("ad_block_id")
    val adBlockId: String,

    @SerialName("revenue_rub")
    val revenueRub: Double,

    @SerialName("revenue_usd")
    val revenueUsd: Double,

    @SerialName("app_version")
    val appVersion: String = "v.${BuildConfig.VERSION_NAME}"
)