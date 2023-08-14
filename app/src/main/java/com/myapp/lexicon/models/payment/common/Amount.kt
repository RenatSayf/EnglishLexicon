package com.myapp.lexicon.models.payment.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Amount(

    @SerialName("currency")
    val currency: String,

    @SerialName("value")
    val value: String
)