package com.myapp.lexicon.models

import com.myapp.lexicon.helpers.printStackTraceIfDebug
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Currency


@Serializable
data class UserX(

    val email: String,

    val phone: String = "",

    @SerialName("first_name")
    val firstName: String = "",

    @SerialName("second_name")
    val secondName: String = "",

    @SerialName("last_name")
    val lastName: String = "",

    @SerialName("bank_card")
    val bankCard: String = "",

    @SerialName("bank_name")
    val bankName: String = "",

    @SerialName("message_to_user")
    val messageToUser: String = "",

    @SerialName("currency_code")
    val currencyCode: String = "",

    @SerialName("month_balance")
    val monthBalance: Double = 0.0,

    @SerialName("previous_month_balance")
    val previousMonthBalance: Int = 0,

    @SerialName("reserved_payout")
    val reservedPayout: Int = 0,

    @SerialName("today_balance")
    val todayBalance: Double = 0.0,

    @SerialName("yesterday_balance")
    val yesterdayBalance: Double = 0.0
) {

    val currencySymbol: String
        get() {
            return try {
                Currency.getInstance(currencyCode).symbol
            } catch (e: IllegalArgumentException) {
                e.printStackTraceIfDebug()
                ""
            }
        }
}