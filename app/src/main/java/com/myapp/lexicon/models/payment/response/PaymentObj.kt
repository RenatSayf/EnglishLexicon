package com.myapp.lexicon.models.payment.response

import com.myapp.lexicon.models.payment.common.Amount
import com.myapp.lexicon.models.payment.common.Metadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PaymentObj(

    @SerialName("amount")
    val amount: Amount,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("description")
    val description: String,

    @SerialName("id")
    val id: String,

    @SerialName("metadata")
    val metadata: Metadata,

    @SerialName("payout_destination")
    val payoutDestination: PayoutDestination,

    @SerialName("cancellation_details")
    val cancellationDetails: CancellationDetails? = null,

    @SerialName("status")
    val status: String,

    @SerialName("test")
    val test: String
)