@file:Suppress("PropertyName")

package com.myapp.lexicon.auth.models

import kotlinx.serialization.Serializable


@Serializable
data class BankData(
    val has_business_payment_acceptance: Boolean?,
    val has_business_payout: Boolean?,
    val has_business_transfer: Boolean?,
    val has_person_account_linking: Boolean?,
    val has_person_payment: Boolean?,
    val has_person_refill: Boolean?,
    val has_person_transfer: Boolean?,
    val has_texts: Boolean?,
    val id: String?,
    val is_24_hours: Boolean?,
    val is_without_opening_account: Boolean?,
    val link: String?,
    val logo: String?,
    val title: String
)