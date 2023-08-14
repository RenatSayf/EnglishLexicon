package com.myapp.lexicon.models.payment.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Metadata(

    @SerialName("order_id")
    val orderId: String
)