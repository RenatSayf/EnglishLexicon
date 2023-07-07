package com.myapp.lexicon.models

import java.lang.NumberFormatException
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
        const val KEY_FIRST_NAME = "first_name"
        const val KEY_LAST_NAME = "last_name"
        const val KEY_PHONE = "phone"
        const val KEY_BANK_CARD = "bank_card"
        const val KEY_PAYMENT_REQUIRED = "payment_required"
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
    var revenuePerAd: Double = 0.0
    var currency: String? = "USD"
    var paymentRequired: Boolean = false

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
            KEY_EMAIL to email,
            KEY_FIRST_NAME to firstName,
            KEY_LAST_NAME to lastName,
            KEY_PHONE to phone,
            KEY_BANK_CARD to bankCard,
            KEY_PAYMENT_REQUIRED to paymentRequired.toString()
        )
    }

    fun mapToUser(map: Map<String, String?>): User {
        return this.apply {
            email = map[KEY_EMAIL]?: ""
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
            currency = map[KEY_CURRENCY]
            firstName = map[KEY_FIRST_NAME]?: ""
            lastName = map[KEY_LAST_NAME]?: ""
            phone = map[KEY_PHONE]?: ""
            bankCard = map[KEY_BANK_CARD]?: ""
        }
    }

}
