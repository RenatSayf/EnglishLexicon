package com.myapp.lexicon.repository.network

import com.myapp.lexicon.video.models.VideoSearchResult
import kotlinx.coroutines.Deferred

interface INetRepository {
    suspend fun getSearchResult(searchString: String): Deferred<VideoSearchResult?>
}