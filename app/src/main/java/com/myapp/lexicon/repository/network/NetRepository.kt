package com.myapp.lexicon.repository.network

import com.myapp.lexicon.video.models.VideoSearchResult
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

open class NetRepository: INetRepository {
    override suspend fun getSearchResult(searchString: String): Deferred<VideoSearchResult?> {
        return coroutineScope {
            async {
                null
            }
        }
    }

}