package com.myapp.lexicon.models.currency

abstract class Currency(
    open val date: String,
    open val name: String,
    open val rate: Double
) {

    abstract fun toMap(): Map<String, String>
}