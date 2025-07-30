package com.myapp.lexicon.ads.models

import kotlinx.serialization.Serializable


@Serializable
data class Reward(
    var adId: Int,
    var amount: Double,
    var currency: String
) {
    fun plus(reward: Reward): Reward {
        return Reward(
            adId = reward.adId,
            amount = this.amount + reward.amount,
            currency = reward.currency
        )
    }
}