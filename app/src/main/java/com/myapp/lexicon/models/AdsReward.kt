package com.myapp.lexicon.models

import com.myapp.lexicon.helpers.printStackTraceIfDebug
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Currency


@Serializable
data class AdsReward(

    @SerialName("currency_code")
    val currencyCode: String,

    @SerialName("today_balance")
    val todayBalance: Double,

    @SerialName("month_balance")
    val monthBalance: Double,

    @SerialName("reward_per_ad")
    val rewardPerAd: Double
) {
    val currencySymbol: String
        get() {
            return try {
                Currency.getInstance(currencyCode).symbol
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
                ""
            }
        }
}

private val decoder = Json(builderAction = { ignoreUnknownKeys })

fun String.toAdsReward(): AdsReward? {
    return try {
        decoder.decodeFromString(AdsReward.serializer(), this)
    } catch (e: Exception) {
        e.printStackTraceIfDebug()
        null
    }
}