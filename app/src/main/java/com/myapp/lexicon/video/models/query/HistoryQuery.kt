package com.myapp.lexicon.video.models.query

data class HistoryQuery(
    override val text: String,
    val thumbnailUrl: String?
): ISearchItem