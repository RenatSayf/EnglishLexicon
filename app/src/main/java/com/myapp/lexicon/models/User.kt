package com.myapp.lexicon.models

import android.icu.util.Currency
import com.myapp.lexicon.helpers.toStringTime
import java.math.BigDecimal
import java.math.RoundingMode


data class User(
    val id: String
) {
    companion object {
        const val KEY_REVENUE_USD = "revenue_usd"
        const val KEY_TOTAL_REVENUE = "total_revenue"
        const val KEY_REALLY_REVENUE = "really_revenue"
        const val KEY_USER_REWARD = "reward"
        const val KEY_PAYOUT_IN_LOCAL_CURRENCY = "payment_in_local_currency"
        const val KEY_RESERVED_PAYMENT = "reserved_payment"
        const val KEY_CURRENCY = "currency"
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val KEY_CURRENCY_RATE = "currency_rate"
        const val KEY_EMAIL = "email"
        const val KEY_FIRST_NAME = "first_name"
        const val KEY_LAST_NAME = "last_name"
        const val KEY_PHONE = "phone"
        const val KEY_BANK_CARD = "bank_card"
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
    var totalRevenue: Double = 0.0
    var reallyRevenue: Double = 0.0
    var userReward: Double = 0.0
    var revenueUSD: Double = 0.0
    var payoutInLocalCurrency: Double = 0.0
        private set

    var reservedPayment: Double = 0.0
        get() = BigDecimal(field).setScale(2, RoundingMode.DOWN).toDouble()
        private set(value) {
            this.payoutInLocalCurrency = BigDecimal(value).setScale(2, RoundingMode.DOWN).toDouble()
            field = value
        }
    var revenuePerAd: Double = 0.0
    var currency: String? = ""
        set(value) {
            field = value
            if (!value.isNullOrEmpty() && value.length == 3) {
                val instance = java.util.Currency.getInstance(field)
                currencySymbol = instance.symbol
            }
        }
    var currencySymbol: String = ""
    var currencyRate: Double = 0.0
    var paymentDate: String = ""
    private var lastUpdateTime: String = ""
    var message: String = ""
        private set
    private var messagingToken: String = ""

    fun toHashMap(): Map<String, String?> {
        return mapOf(
            KEY_REVENUE_USD to revenueUSD.toString(),
            KEY_TOTAL_REVENUE to totalRevenue.toString(),
            KEY_REALLY_REVENUE to reallyRevenue.toString(),
            KEY_USER_REWARD to userReward.toString(),
            KEY_CURRENCY to currency,
            KEY_CURRENCY_SYMBOL to currencySymbol,
            KEY_CURRENCY_RATE to currencyRate.toString(),
            KEY_PAYOUT_IN_LOCAL_CURRENCY to payoutInLocalCurrency.toString(),
            KEY_RESERVED_PAYMENT to reservedPayment.toString(),
            KEY_EMAIL to email,
            KEY_FIRST_NAME to firstName,
            KEY_LAST_NAME to lastName,
            KEY_PHONE to phone,
            KEY_BANK_CARD to bankCard,
            KEY_PAYMENT_DATE to paymentDate,
            KEY_MESSAGE to message,
            KEY_LAST_UPDATE_TIME to System.currentTimeMillis().toStringTime()
        )
    }

    fun mapToUser(map: Map<String, String?>): User {
        return this.apply {
            email = map[KEY_EMAIL]?: ""
            revenueUSD = try {
                map[KEY_REVENUE_USD]?.toDouble()?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            totalRevenue = try {
                map[KEY_TOTAL_REVENUE]?.toDouble()?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            reallyRevenue = try {
                map[KEY_REALLY_REVENUE]?.toDouble()?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            userReward = try {
                map[KEY_USER_REWARD]?.toDouble()?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            payoutInLocalCurrency = try {
                map[KEY_PAYOUT_IN_LOCAL_CURRENCY]?.toDouble()?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            reservedPayment = try {
                map[KEY_RESERVED_PAYMENT]?.toDouble()?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            currencyRate = try {
                map[KEY_CURRENCY_RATE]?.toDouble()?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            currency = map[KEY_CURRENCY]
            currencySymbol = map[KEY_CURRENCY_SYMBOL]?: ""
            firstName = map[KEY_FIRST_NAME]?: ""
            lastName = map[KEY_LAST_NAME]?: ""
            phone = map[KEY_PHONE]?: ""
            bankCard = map[KEY_BANK_CARD]?: ""
            paymentDate = map[KEY_PAYMENT_DATE]?: ""
            message = map[KEY_MESSAGE]?: ""
            messagingToken = map[KEY_MESSAGING_TOKEN]?: ""
            lastUpdateTime = map[KEY_LAST_UPDATE_TIME]?: ""
        }
    }

    fun reservePayment(
            threshold: Double,
            onReserve: (user: User) -> Unit,
            onNotEnough: () -> Unit = {}
    ) {
        if (this.userReward > threshold) {
            this.reservedPayment += this.userReward
            this.userReward = 0.0
            this.totalRevenue = 0.0
            this.reallyRevenue = 0.0
            onReserve.invoke(this)
        }
        else {
            onNotEnough.invoke()
        }
    }

}

fun Double.to2DigitsScale(): Double {
    return BigDecimal(this).setScale(2, RoundingMode.DOWN).toDouble()
}

