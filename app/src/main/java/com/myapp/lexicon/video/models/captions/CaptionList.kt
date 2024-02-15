package com.myapp.lexicon.video.models.captions

import kotlinx.serialization.Serializable


@Serializable
data class CaptionList(
    val etag: String,
    val items: List<Item>,
    val kind: String
) {

    @Serializable
    data class Item(
        val etag: String,
        val id: String,
        val kind: String,
        val snippet: Snippet
    ) {

        @Serializable
        data class Snippet(
            val audioTrackType: String,
            val isAutoSynced: Boolean,
            val isCC: Boolean,
            val isDraft: Boolean,
            val isEasyReader: Boolean,
            val isLarge: Boolean,
            val language: String,
            val lastUpdated: String,
            val name: String,
            val status: String,
            val trackKind: String,
            val videoId: String
        )
    }

    var authToken: String = ""
}