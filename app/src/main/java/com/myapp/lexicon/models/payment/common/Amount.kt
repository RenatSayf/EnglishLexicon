package com.myapp.lexicon.models.payment.common

import kotlinx.serialization.Serializable


@Serializable
data class Amount(
    val currency: String,
    val value: String
)