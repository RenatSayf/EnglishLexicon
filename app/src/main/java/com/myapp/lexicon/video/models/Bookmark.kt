package com.myapp.lexicon.video.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Bookmark(
    @SerialName("thumbnail_url")
    val thumbnailUrl: String?,
    val title: String,
    val url: String
)
