package com.myapp.lexicon.models

import java.math.BigDecimal
import java.math.RoundingMode


data class User(
    val id: String
) {
    companion object {
        const val KEY_REVENUE_USD = "revenueUsd"
        const val KEY_TOTAL_REVENUE = "totalRevenue"
        const val KEY_USER_REWARD = "userReward"
        const val KEY_USER_DAILY_REWARD = "userDailyReward"
        const val KEY_DAILY_REVENUE_FROM_USER = "dailyRevenueFromUser"
        const val KEY_YESTERDAY_USER_REWARD = "yesterdayUserReward"
        const val KEY_YESTERDAY_REVENUE_FROM_USER = "yesterdayRevenueFromUser"
        const val KEY_REWARD_UPDATE_AT = "rewardUpdateAt"
        const val KEY_RESERVED_PAYMENT = "reservedPayment"
        const val KEY_REQUIRES_PAYMENT = "requiresPayment"
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
        const val KEY_RESERVED_PAYMENT_DATE = "reservedPaymentDate"
        const val KEY_CHECK_REFERENCE = "checkReference"
        const val KEY_MESSAGE = "messageToUser"
        const val KEY_MESSAGING_TOKEN = "messagingToken"
        const val KEY_IS_ADS_ENABLED = "isAdsEnabled"
        const val KEY_USER_PERCENT = "userPercent"
        const val KEY_APP_VERSION = "appVersion"
    }

    var email: String = ""
    var password: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var phone: String = ""
    var bankCard: String = ""
    var bankName: String = ""
    var revenueUSD: Double = 0.0
    var totalRevenue: Double = 0.0
    var userReward: Double = 0.0
    var userDailyReward: Double = 0.0
    var dailyRevenueFromUser: Double = 0.0
    var yesterdayUserReward: Double = 0.0
    var yesterdayRevenueFromUser: Double = 0.0
    var rewardUpdateAt: String = ""
    var currencyRate: Double = 0.0
    var paymentDate: String = ""
    var userPercent: Double? = null
    var appVersion: String = ""
    var createdAt: Long = 0

    var reservedPayment: Double = 0.0
        get() = BigDecimal(field).setScale(2, RoundingMode.DOWN).toDouble()

    var requiresPayment: Int = 0

    var reservedPaymentDate: String = ""
    var checkReference: String = ""
    var currency: String = ""
    var currencySymbol: String = ""
    var message: String = ""
    var isAdsEnabled: Boolean = true


}

fun Double.to2DigitsScale(): Double {
    return BigDecimal(this).setScale(2, RoundingMode.DOWN).toDouble()
}

