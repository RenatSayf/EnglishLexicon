package com.myapp.lexicon.repository.network

import com.myapp.lexicon.video.models.VideoSearchResult
import com.myapp.lexicon.video.models.captions.CaptionList
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.util.toByteArray
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import java.io.File

open class NetRepository(
    private val httpClient: HttpClient
): INetRepository {

    private val jsonDecoder = Json { ignoreUnknownKeys }

    override suspend fun getSearchResult(searchString: String): Deferred<VideoSearchResult?> {
        return coroutineScope {
            async {
                null
            }
        }
    }

    override suspend fun fetchCaptionsList(
        videoId: String,
        authToken: String
    ): Deferred<Result<CaptionList?>> {
        return coroutineScope {
            async {
                val httpResponse = httpClient.get("https://www.googleapis.com/youtube/v3/captions", block = {
                    header("Authorization", "Bearer $authToken")
                    parameter("part", "snippet")
                    parameter("videoId", videoId)
                    parameter("key", "AIzaSyBF5uCkyXVQLVUtclUBcHDQqklEf9JAMq4")
                    parameter("Accept", "application/json")
                })
                val statusCode = httpResponse.status
                val result = when(statusCode.value) {
                    200 -> {
                        val bodyAsText = httpResponse.bodyAsText()
                        val captionList = jsonDecoder.decodeFromString<CaptionList>(bodyAsText)
                        captionList.authToken = authToken
                        Result.success(captionList)
                    }
                    else -> {
                        Result.failure(Throwable("********** Error description: ${statusCode.description}. Code: ${statusCode.value} ************"))
                    }
                }
                result
            }
        }
    }

    override suspend fun downloadCaptions(
        captionId: String,
        authToken: String
    ): Deferred<Result<File>> {
        return coroutineScope {
            async {
                val url = "https://www.googleapis.com/youtube/v3/captions/$captionId"
                val httpResponse = httpClient.get(url, block = {
                    header("Authorization", "Bearer $authToken")
                    header("Content-Type", "application/octet-stream")
                    parameter("id", captionId)
                    parameter("key", "AIzaSyBF5uCkyXVQLVUtclUBcHDQqklEf9JAMq4")
                })
                val statusCode = httpResponse.status
                when(statusCode.value) {
                    200 -> {
                        val bodyAsChannel = httpResponse.bodyAsChannel()
                        val byteArray = bodyAsChannel.toByteArray()
                        byteArray
                    }
                    else -> {
                        Result.failure<Throwable>(Throwable("********** Error description: ${statusCode.description}. Code: ${statusCode.value} ************"))
                    }
                }

                Result.success(File(""))
            }
        }
    }


}