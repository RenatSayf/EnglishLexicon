package com.myapp.lexicon.auth.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SBPBanks(
    @SerialName("data")
    val banks: List<BankData>
)