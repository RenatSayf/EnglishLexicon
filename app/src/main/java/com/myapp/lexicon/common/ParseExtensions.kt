package com.myapp.lexicon.common

import com.myapp.lexicon.models.Revenue
import com.myapp.lexicon.models.User
import com.parse.ParseObject
import com.parse.ParseUser

fun ParseObject.mapToUser(): User {
    return User(this.objectId).apply {
        var value = this@mapToUser[User.KEY_REVENUE_USD]
        this.revenueUSD = if (value is Number) value.toDouble() else this.revenueUSD

        value = this@mapToUser[User.KEY_TOTAL_REVENUE]
        this.totalRevenue = if (value is Number) value.toDouble() else this.totalRevenue

        value = this@mapToUser[User.KEY_USER_REWARD]
        this.userReward = if (value is Number) value.toDouble() else this.userReward

        value = this@mapToUser[User.KEY_RESERVED_PAYMENT]
        this.reservedPayment = if (value is Number) value.toDouble() else this.reservedPayment

        value = this@mapToUser[User.KEY_CURRENCY]
        this.currency = if (value is String) value else this.currency

        value = this@mapToUser[User.KEY_EMAIL]
        this.email = if (value is String) value else this.email

        value = this@mapToUser[User.KEY_PHONE]
        this.phone = if (value is String) value else this.phone

        value = this@mapToUser[User.KEY_BANK_CARD]
        this.bankCard = if (value is String) value else this.bankCard

        value = this@mapToUser[User.KEY_BANK_NAME]
        this.bankName = if (value is String) value else this.bankName

        value = this@mapToUser[User.KEY_FIRST_NAME]
        this.firstName = if (value is String) value else this.firstName

        value = this@mapToUser[User.KEY_LAST_NAME]
        this.lastName = if (value is String) value else this.lastName

        value = this@mapToUser[User.KEY_MESSAGE]
        this.message = if (value is String) value else this.message
    }
}

fun ParseObject.mapToRevenue(): Revenue {

    var value = this@mapToRevenue[User.KEY_USER_REWARD]
    val userReward = if (value is Number) value.toDouble() else 0.0

    value = this@mapToRevenue[User.KEY_RESERVED_PAYMENT]
    val reservedPayment = if (value is Number) value.toDouble() else 0.0

    value = this@mapToRevenue[User.KEY_CURRENCY]
    val currency = if (value is String) value else ""

    value = this@mapToRevenue[User.KEY_CURRENCY_SYMBOL]
    val symbol = if (value is String) value else ""

    return Revenue(
        reward = userReward,
        toPayout = reservedPayment,
        currencyCode = currency,
        currencySymbol = symbol
    )
}