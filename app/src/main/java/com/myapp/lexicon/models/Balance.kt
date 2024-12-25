package com.myapp.lexicon.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Balance(

    @SerialName("currency_code")
    val currencyCode: String,

    @SerialName("month_balance")
    val monthBalance: Double,

    @SerialName("reserved_payout")
    val reservedPayout: Int,

    @SerialName("today_balance")
    val todayBalance: Double,

    @SerialName("yesterday_balance")
    val yesterdayBalance: Double
)