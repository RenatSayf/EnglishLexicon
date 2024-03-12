package com.myapp.lexicon.video.models

import kotlinx.serialization.Serializable


@Serializable
data class Id(
    val kind: String,
    val videoId: String
)