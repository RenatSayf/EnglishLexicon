package com.myapp.lexicon.video.models

import kotlinx.serialization.Serializable


@Serializable
data class PageInfo(
    val resultsPerPage: Int,
    val totalResults: Int
)