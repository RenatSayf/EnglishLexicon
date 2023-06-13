package com.myapp.lexicon.models

import java.math.BigDecimal
import java.math.RoundingMode

private const val percentage: Double = 0.7

data class User(
    val id: String
) {

    var reward: Double = 0.00
        set(value) {
            field = BigDecimal(value * percentage).setScale(3, RoundingMode.DOWN).toDouble()
        }
    var currency: String? = "USD"

    fun toHashMap(): HashMap<String, String?> {
        return hashMapOf(
            "reward" to reward.toString(),
            "currency" to currency
        )
    }
}
