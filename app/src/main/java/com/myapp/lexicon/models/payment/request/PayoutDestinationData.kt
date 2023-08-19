package com.myapp.lexicon.models.payment.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PayoutDestinationData(

    @SerialName("account_number")
    val accountNumber: String,

    @SerialName("type")
    val type: String
)