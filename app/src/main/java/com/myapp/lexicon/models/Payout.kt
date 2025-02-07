package com.myapp.lexicon.models

import com.myapp.lexicon.helpers.toStringTime


data class Payout(
    val reservedSum: Int,
    val payoutSum: Int,
    val payoutTime: Long,
    val checkReference: String
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            User.KEY_RESERVED_PAYMENT to this.reservedSum,
            User.KEY_REQUIRES_PAYMENT to this.payoutSum,
            User.KEY_PAYMENT_DATE to this.payoutTime.toStringTime(),
            User.KEY_CHECK_REFERENCE to this.checkReference
        )
    }
}
