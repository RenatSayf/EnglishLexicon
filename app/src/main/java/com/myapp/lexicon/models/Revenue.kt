package com.myapp.lexicon.models

data class Revenue(
    val reward: Double,
    val toPayout: Double,
    val currencyCode: String,
    val currencySymbol: String
)
