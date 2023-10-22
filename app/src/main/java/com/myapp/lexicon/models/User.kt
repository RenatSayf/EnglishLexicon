package com.myapp.lexicon.models

import com.myapp.lexicon.helpers.toStringTime
import java.math.BigDecimal
import java.math.RoundingMode


data class User(
    val id: String
) {
    companion object {
        const val KEY_SESSION_TOKEN = "sessionToken"
        const val KEY_REVENUE_USD = "revenueUsd"
        const val KEY_TOTAL_REVENUE = "totalRevenue"
        const val KEY_USER_REWARD = "userReward"
        const val KEY_RESERVED_PAYMENT = "reservedPayment"
        const val KEY_CURRENCY = "currencyCode"
        const val KEY_CURRENCY_SYMBOL = "currencySymbol"
        const val KEY_CURRENCY_RATE = "currencyRate"
        const val KEY_EMAIL = "email"
        const val KEY_FIRST_NAME = "firstName"
        const val KEY_LAST_NAME = "lastName"
        const val KEY_PHONE = "phone"
        const val KEY_BANK_CARD = "bankCard"
        const val KEY_BANK_NAME = "bankName"
        const val KEY_PAYMENT_DATE = "paymentDate"
        const val KEY_MESSAGE = "messageToUser"
        const val KEY_LAST_UPDATE_TIME = "lastUpdateTime"
        const val KEY_MESSAGING_TOKEN = "messagingToken"
    }

    var sessionToken: String = ""
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

