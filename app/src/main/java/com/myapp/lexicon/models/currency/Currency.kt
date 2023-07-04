package com.myapp.lexicon.models.currency

data class Currency(
    val date: String,
    val name: String,
    val rate: Double
) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "date" to date,
            "currency" to name,
            "rate" to rate.toString()
        )
    }
}