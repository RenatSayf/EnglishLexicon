package com.myapp.lexicon.video.models

import kotlinx.serialization.Serializable


@Serializable
data class VideoReference(
    val title: String,
    val url: String
)
