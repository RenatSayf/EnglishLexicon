package com.myapp.lexicon.models.currency

import java.util.Locale

data class Currency(
    val date: String,
    val name: String,
    val rate: Double
) {
    var symbol: String = ""
        private set

    fun toMap(): Map<String, String> {
        return mapOf(
            "date" to date,
            "currency" to name,
            "rate" to rate.toString()
        )
    }

    init {
        val currency = java.util.Currency.getInstance(Locale.getDefault())
        symbol = if (this.name == Currencies.RUB.name) {
            currency.symbol
        }
        else java.util.Currency.getInstance(Locale.US).symbol
    }
}