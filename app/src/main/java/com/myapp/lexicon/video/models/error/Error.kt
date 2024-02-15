package com.myapp.lexicon.video.models.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Error(
    @SerialName("error")
    val details: Details
)