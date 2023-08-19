package com.myapp.lexicon.models.payment.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PayoutDestination(

    @SerialName("account_number")
    val accountNumber: String,

    @SerialName("type")
    val type: String
)