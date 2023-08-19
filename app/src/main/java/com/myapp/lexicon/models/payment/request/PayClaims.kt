package com.myapp.lexicon.models.payment.request

import com.myapp.lexicon.models.payment.common.Amount
import com.myapp.lexicon.models.payment.common.Metadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PayClaims(

    @SerialName("amount")
    val amount: Amount,

    @SerialName("description")
    val description: String,

    @SerialName("metadata")
    val metadata: Metadata,

    @SerialName("payout_destination_data")
    val payoutDestinationData: PayoutDestinationData
)