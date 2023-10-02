package com.myapp.lexicon.models

import com.myapp.lexicon.helpers.toStringTime
import java.math.BigDecimal
import java.math.RoundingMode


data class User(
    val id: String
) {
    companion object {
        const val KEY_REVENUE_USD = "revenue_usd"
        const val KEY_TOTAL_REVENUE = "total_revenue"
        const val KEY_USER_REWARD = "reward"
        const val KEY_RESERVED_PAYMENT = "reserved_payment"
        const val KEY_CURRENCY = "currency"
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val KEY_CURRENCY_RATE = "currency_rate"
        const val KEY_EMAIL = "email"
        const val KEY_FIRST_NAME = "first_name"
        const val KEY_LAST_NAME = "last_name"
        const val KEY_PHONE = "phone"
        const val KEY_BANK_CARD = "bank_card"
        const val KEY_BANK_NAME = "bank_name"
        const val KEY_PAYMENT_DATE = "payment_date"
        const val KEY_MESSAGE = "message_to_user"
        const val KEY_LAST_UPDATE_TIME = "last_update_time"
        const val KEY_MESSAGING_TOKEN = "messaging_token"
    }

    var email: String = ""
    var password: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var phone: String = ""
    var bankCard: String = ""
    var bankName: String = ""
    var revenueUSD: Double = 0.0
        set(value) {
            field = value
            currencyRate = try {
                totalRevenue / field
            } catch (e: ArithmeticException) {
                0.0
            }
        }
    var totalRevenue: Double = 0.0
        set(value) {
            field = value
            currencyRate = try {
                field / revenueUSD
            } catch (e: ArithmeticException) {
                0.0
            }
        }
    var userReward: Double = 0.0
    var currencyRate: Double = 0.0
        private set

    var reservedPayment: Double = 0.0
        get() = BigDecimal(field).setScale(2, RoundingMode.DOWN).toDouble()

    var currency: String? = ""
        set(value) {
            field = value
            if (!value.isNullOrEmpty() && value.length == 3) {
                val instance = java.util.Currency.getInstance(field)
                currencySymbol = instance.symbol
            }
        }
    var currencySymbol: String = ""
        private set

    var paymentDate: String = ""
    var message: String = ""

    fun reservePayment(
        threshold: Int,
        onReserve: (map: Map<String, Any?>) -> Unit,
        onNotEnough: () -> Unit = {}
    ) {
        if (this.userReward > threshold) {
            val reservedPayment = this.reservedPayment + this.userReward
            val userMap = mapOf<String, Any?>(
                KEY_RESERVED_PAYMENT to reservedPayment,
                KEY_USER_REWARD to 0.0,
                KEY_TOTAL_REVENUE to 0.0,
                //KEY_REVENUE_USD to 0.0,
                KEY_PAYMENT_DATE to System.currentTimeMillis().toStringTime()
            )
            onReserve.invoke(userMap)
        }
        else {
            onNotEnough.invoke()
        }
    }

}

fun Double.to2DigitsScale(): Double {
    return BigDecimal(this).setScale(2, RoundingMode.DOWN).toDouble()
}

