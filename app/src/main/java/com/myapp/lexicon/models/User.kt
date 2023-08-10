package com.myapp.lexicon.models

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import java.math.BigDecimal
import java.math.RoundingMode


data class User(
    val id: String
) {
    companion object {
        const val KEY_TOTAL_REVENUE = "total_revenue"
        const val KEY_REALLY_REVENUE = "really_revenue"
        const val KEY_USER_REWARD = "reward"
        const val KEY_DEFAULT_CURRENCY_REWARD = "default_currency_reward"
        const val KEY_RESERVED_PAYMENT = "reserved_payment"
        const val KEY_CURRENCY = "currency"
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val KEY_EMAIL = "email"
        const val KEY_FIRST_NAME = "first_name"
        const val KEY_LAST_NAME = "last_name"
        const val KEY_PHONE = "phone"
        const val KEY_BANK_CARD = "bank_card"
        const val KEY_PAYMENT_REQUIRED = "payment_required"
        const val KEY_PAYMENT_DATE = "payment_date"
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
    var defaultCurrencyReward: Double = 0.0
    var reservedPayment: Int = 0
    var revenuePerAd: Double = 0.0
    var currency: String? = "USD"
    var currencySymbol: String = ""
    var paymentRequired: Boolean = false
    var paymentDate: String = ""

    private val userPercentage: Double by lazy {
        Firebase.remoteConfig.getDouble("USER_PERCENTAGE")
    }

    fun toHashMap(): Map<String, String?> {
        return mapOf(
            KEY_TOTAL_REVENUE to totalRevenue.toString(),
            KEY_REALLY_REVENUE to reallyRevenue.toString(),
            KEY_USER_REWARD to userReward.toString(),
            KEY_CURRENCY to currency,
            KEY_CURRENCY_SYMBOL to currencySymbol,
            KEY_DEFAULT_CURRENCY_REWARD to defaultCurrencyReward.toString(),
            KEY_RESERVED_PAYMENT to reservedPayment.toString(),
            KEY_EMAIL to email,
            KEY_FIRST_NAME to firstName,
            KEY_LAST_NAME to lastName,
            KEY_PHONE to phone,
            KEY_BANK_CARD to bankCard,
            KEY_PAYMENT_REQUIRED to paymentRequired.toString(),
            KEY_PAYMENT_DATE to paymentDate
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
            defaultCurrencyReward = try {
                map[KEY_DEFAULT_CURRENCY_REWARD]?.toDouble()?: 0.0
            } catch (e: NumberFormatException) {
                0.0
            }
            reservedPayment = try {
                map[KEY_RESERVED_PAYMENT]?.toInt()?: 0
            } catch (e: NumberFormatException) {
                0
            }
            currency = map[KEY_CURRENCY]
            currencySymbol = map[KEY_CURRENCY_SYMBOL]?: ""
            firstName = map[KEY_FIRST_NAME]?: ""
            lastName = map[KEY_LAST_NAME]?: ""
            phone = map[KEY_PHONE]?: ""
            bankCard = map[KEY_BANK_CARD]?: ""
            paymentRequired = map[KEY_PAYMENT_REQUIRED].toBoolean()
            paymentDate = map[KEY_PAYMENT_DATE]?: ""
        }
    }

    fun convertToDefaultCurrency(rate: Double) {
        val defaultReward = BigDecimal(this.userReward * rate).setScale(2, RoundingMode.DOWN).toDouble()
        this.defaultCurrencyReward = defaultReward
    }

    fun reservePayment(
        threshold: Double,
        currencyRate: Double,
        onReserve: (user: User) -> Unit,
        onNotEnough: () -> Unit = {}
    ) {
        if (this.defaultCurrencyReward > threshold) {
            this.reservedPayment = this.defaultCurrencyReward.toInt()
            this.defaultCurrencyReward -= this.reservedPayment
            this.defaultCurrencyReward = BigDecimal(defaultCurrencyReward).setScale(2, RoundingMode.DOWN).toDouble()

            this.userReward = this.defaultCurrencyReward / currencyRate
            this.totalRevenue = this.userReward / userPercentage

            onReserve.invoke(this)
        }
        else {
            onNotEnough.invoke()
        }
    }

}
