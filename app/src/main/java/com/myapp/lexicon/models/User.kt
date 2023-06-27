package com.myapp.lexicon.models

import com.google.firebase.auth.PhoneAuthProvider
import java.math.BigDecimal
import java.math.RoundingMode


data class User(
    val id: String
) {
    companion object {
        const val KEY_TOTAL_REVENUE = "total_revenue"
        const val KEY_REALLY_REVENUE = "really_revenue"
        const val KEY_USER_REWARD = "reward"
        const val KEY_CURRENCY = "currency"
        const val KEY_EMAIL = "email"
    }

    var email: String = ""
    var password: String = ""
    var totalRevenue: Double = 0.0
    var reallyRevenue: Double = 0.0
    var userReward: Double = 0.0
    var revenuePerAd: Double = 0.0
    var currency: String? = "USD"

    val rewardToDisplay: String
        get() {
            val decimal = BigDecimal(userReward).setScale(3, RoundingMode.DOWN)
            return "$decimal $currency"
        }

    fun toHashMap(): HashMap<String, String?> {
        return hashMapOf(
            KEY_TOTAL_REVENUE to totalRevenue.toString(),
            KEY_REALLY_REVENUE to reallyRevenue.toString(),
            KEY_USER_REWARD to userReward.toString(),
            KEY_CURRENCY to currency,
            KEY_EMAIL to email
        )
    }
}
