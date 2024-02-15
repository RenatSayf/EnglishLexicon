package com.myapp.lexicon.video.models.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("errors")
data class Reason(
    val domain: String,
    val location: String,
    val locationType: String,
    val message: String,
    val reason: String
)