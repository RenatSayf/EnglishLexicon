package com.myapp.lexicon.models.payment.common

import kotlinx.serialization.Serializable


@Serializable
data class Metadata(
    val order_id: String
)