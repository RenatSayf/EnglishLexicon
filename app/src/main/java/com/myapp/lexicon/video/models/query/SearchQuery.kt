package com.myapp.lexicon.video.models.query

data class SearchQuery(
    override val text: String
): ISearchItem
