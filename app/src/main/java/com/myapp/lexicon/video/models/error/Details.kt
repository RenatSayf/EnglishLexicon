package com.myapp.lexicon.video.models.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("error")
data class Details(
    val code: Int,
    val errors: List<Reason>,
    val message: String,
    val status: String
)