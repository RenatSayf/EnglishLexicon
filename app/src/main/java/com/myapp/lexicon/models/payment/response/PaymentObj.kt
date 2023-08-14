package com.myapp.lexicon.models.payment.response

import com.myapp.lexicon.models.payment.common.Amount
import com.myapp.lexicon.models.payment.common.Metadata
import kotlinx.serialization.Serializable


@Serializable
data class PaymentObj(
    val amount: Amount,
    val created_at: String,
    val description: String,
    val id: String,
    val metadata: Metadata,
    val payout_destination: PayoutDestination,
    val status: String,
    val test: String
)