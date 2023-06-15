package com.myapp.lexicon.models

import java.math.BigDecimal
import java.math.RoundingMode


data class User(
    val id: String
) {
    companion object {
        const val KEY_TOTAL_REVENUE = "total_revenue"
        const val KEY_REWARD = "reward"
        const val KEY_CURRENCY = "currency"
    }

    var totalRevenue: Double = 0.0
    var reward: Double = 0.0
    var currency: String? = "USD"

    var rewardToDisplay: String = ""
        get() {
            val decimal = BigDecimal(reward).setScale(3, RoundingMode.DOWN)
            return "$decimal $currency"
        }
        private set

    fun toHashMap(): HashMap<String, String?> {
        return hashMapOf(
            KEY_TOTAL_REVENUE to totalRevenue.toString(),
            KEY_REWARD to reward.toString(),
            KEY_CURRENCY to currency
        )
    }
}
