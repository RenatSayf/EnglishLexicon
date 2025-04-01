package com.myapp.lexicon.models

import com.myapp.lexicon.helpers.printStackTraceIfDebug
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Currency



@Serializable
data class UserX(

    val email: String,

    val phone: String? = null,

    @SerialName(KEY_FIRST_NAME)
    val firstName: String? = null,

    @SerialName(KEY_SECOND_NAME)
    val secondName: String? = null,

    @SerialName(KEY_LAST_NAME)
    val lastName: String? = null,

    @SerialName(KEY_BANK_CARD)
    val bankCard: String? = null,

    @SerialName(KEY_BANK_NAME)
    val bankName: String? = null,

    @SerialName(KEY_MESSAGE_TO_USER)
    val messageToUser: String? = null,

    @SerialName(KEY_CURRENCY_CODE)
    val currencyCode: String? = null,

    @SerialName(KEY_MONTH_BALANCE)
    val monthBalance: Double? = null,

    @SerialName(KEY_PREVIOUS_MONTH_BALANCE)
    val previousMonthBalance: Int? = null,

    @SerialName(KEY_RESERVED_PAYOUT)
    val reservedPayout: Int? = null,

    @SerialName(KEY_TODAY_BALANCE)
    val todayBalance: Double? = null,

    @SerialName(KEY_YESTERDAY_BALANCE)
    val yesterdayBalance: Double? = null,

    @SerialName(value = "reward_ratio")
    val rewardRatio: Double = 0.5,

    @SerialName(value = "payout_threshold")
    val payoutThreshold: Int = 100
) {
    companion object {
        const val KEY_EMAIL = "email"
        const val KEY_PHONE = "phone"
        const val KEY_FIRST_NAME = "first_name"
        const val KEY_SECOND_NAME = "second_name"
        const val KEY_LAST_NAME = "last_name"
        const val KEY_BANK_CARD = "bank_card"
        const val KEY_BANK_NAME = "bank_name"
        const val KEY_MESSAGE_TO_USER = "message_to_user"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_MONTH_BALANCE = "month_balance"
        const val KEY_PREVIOUS_MONTH_BALANCE = "previous_month_balance"
        const val KEY_RESERVED_PAYOUT = "reserved_payout"
        const val KEY_TODAY_BALANCE = "today_balance"
        const val KEY_YESTERDAY_BALANCE = "yesterday_balance"
    }

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