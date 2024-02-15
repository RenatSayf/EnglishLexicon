package com.myapp.lexicon.repository.network

import com.myapp.lexicon.video.models.VideoSearchResult
import io.ktor.client.HttpClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class MockNetRepository: NetRepository(HttpClient()) {
    override suspend fun getSearchResult(searchString: String): Deferred<VideoSearchResult?> {
        return coroutineScope {
            async {
                val result = Json.decodeFromString<VideoSearchResult>(TEST_VIDEO_LIST)
                result
            }
        }
    }
}