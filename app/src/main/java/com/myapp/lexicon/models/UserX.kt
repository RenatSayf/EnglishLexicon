package com.myapp.lexicon.models

import com.myapp.lexicon.helpers.printStackTraceIfDebug
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Currency


const val MESSAGE_TO_USER = "message_to_user"


@Serializable
data class UserX(

    val email: String,

    val phone: String? = null,

    @SerialName("first_name")
    val firstName: String? = null,

    @SerialName("second_name")
    val secondName: String? = null,

    @SerialName("last_name")
    val lastName: String? = null,

    @SerialName("bank_card")
    val bankCard: String? = null,

    @SerialName("bank_name")
    val bankName: String? = null,

    @SerialName(MESSAGE_TO_USER)
    val messageToUser: String? = null,

    @SerialName("currency_code")
    val currencyCode: String? = null,

    @SerialName("month_balance")
    val monthBalance: Double? = null,

    @SerialName("previous_month_balance")
    val previousMonthBalance: Int? = null,

    @SerialName("reserved_payout")
    val reservedPayout: Int? = null,

    @SerialName("today_balance")
    val todayBalance: Double? = null,

    @SerialName("yesterday_balance")
    val yesterdayBalance: Double? = null,

    @SerialName("is_ads_enabled")
    val isAdsEnabled: Boolean = true
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

    fun toJsonString(): String {
        val format = Json { explicitNulls = false }
        return try {
            format.encodeToString(serializer(), this)
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            "******"
        }
    }
}