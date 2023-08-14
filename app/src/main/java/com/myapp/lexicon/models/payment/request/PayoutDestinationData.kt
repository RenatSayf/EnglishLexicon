package com.myapp.lexicon.models.payment.request

import kotlinx.serialization.Serializable


@Serializable
data class PayoutDestinationData(
    val account_number: String,
    val type: String
)