package com.myapp.lexicon.video.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class VideoSearchResult(
    val etag: String,
    @SerialName("items")
    val videoItems: List<VideoItem>,
    val kind: String,
    val prevPageToken: String? = null,
    val nextPageToken: String,
    val pageInfo: PageInfo,
    val regionCode: String
) {
    var query: String = ""
}