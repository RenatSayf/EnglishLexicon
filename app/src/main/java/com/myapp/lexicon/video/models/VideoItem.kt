package com.myapp.lexicon.video.models

import kotlinx.serialization.Serializable


@Serializable
data class VideoItem(
    val etag: String,
    val id: Id,
    val kind: String,
    val snippet: Snippet
)