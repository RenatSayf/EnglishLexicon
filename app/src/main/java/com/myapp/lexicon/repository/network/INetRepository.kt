package com.myapp.lexicon.repository.network

import com.myapp.lexicon.video.models.VideoSearchResult
import com.myapp.lexicon.video.models.captions.CaptionList
import kotlinx.coroutines.Deferred
import java.io.File

interface INetRepository {
    suspend fun getSearchResult(searchString: String): Deferred<VideoSearchResult?>

    suspend fun fetchCaptionsList(videoId: String, authToken: String): Deferred<Result<CaptionList?>>

    suspend fun downloadCaptions(captionId: String, authToken: String): Deferred<Result<File>>
}