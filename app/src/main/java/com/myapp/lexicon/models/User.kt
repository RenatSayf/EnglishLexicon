package com.myapp.lexicon.models


data class User(
    val id: String
) {
    companion object {
        const val KEY_REWARD = "reward"
        const val KEY_CURRENCY = "currency"
    }
    var reward: Double = 0.00
    var currency: String? = "USD"

    fun toHashMap(): HashMap<String, String?> {
        return hashMapOf(
            KEY_REWARD to reward.toString(),
            KEY_CURRENCY to currency
        )
    }
}
