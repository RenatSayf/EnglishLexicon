package com.myapp.lexicon.models.payment.request

import com.myapp.lexicon.models.payment.common.Amount
import com.myapp.lexicon.models.payment.common.Metadata
import kotlinx.serialization.Serializable


@Serializable
data class PayClaims(
    val amount: Amount,
    val description: String,
    val metadata: Metadata,
    val payout_destination_data: PayoutDestinationData
)