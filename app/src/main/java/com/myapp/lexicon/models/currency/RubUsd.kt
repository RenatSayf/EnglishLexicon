package com.myapp.lexicon.models.currency

data class RubUsd(
    override val date: String,
    override val name: String,
    override val rate: Double
): Currency(date, name, rate) {

    companion object {
        const val name: String = "RUB"
    }

    override fun toMap(): Map<String, String> {
        return mapOf(
            "date" to date,
            "currency" to name,
            "rate" to rate.toString()
        )
    }
}

