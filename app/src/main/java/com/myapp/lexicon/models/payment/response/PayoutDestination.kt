package com.myapp.lexicon.models.payment.response

import kotlinx.serialization.Serializable


@Serializable
data class PayoutDestination(
    val account_number: String,
    val type: String
)