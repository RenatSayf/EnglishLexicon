package com.myapp.lexicon.models.payment.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CancellationDetails(

    @SerialName("party")
    val party: String,

    @SerialName("reason")
    val reason: String
)