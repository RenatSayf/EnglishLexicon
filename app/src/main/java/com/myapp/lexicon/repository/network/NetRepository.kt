package com.myapp.lexicon.repository.network

import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.video.models.VideoSearchResult
import com.myapp.lexicon.video.models.captions.CaptionList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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

    override suspend fun getSearchResult(
        query: String,
        pageToken: String,
        maxResults: Int,
        subtitles: Boolean
    ): Deferred<Result<VideoSearchResult>> {
        return coroutineScope {
            async {
                val httpResponse = httpClient.get(
                    urlString = "https://www.googleapis.com/youtube/v3/search",
                    block = {
                        parameter("part", "snippet")
                        parameter("key", BuildConfig.YOUTUBE_API_KEY)
                        parameter("q", query)
                        parameter("maxResults", "$maxResults")
                        parameter("pageToken", pageToken)
                        if (subtitles) {
                            parameter("videoCaption", "closedCaption")
                            parameter("type", "video")
                        }
                    })
                val statusCode = httpResponse.status
                val result = when (statusCode.value) {
                    200 -> {
                        val bodyStr = httpResponse.body<String>()
                        val decodeFromString = jsonDecoder.decodeFromString<VideoSearchResult>(bodyStr)
                        Result.success(decodeFromString)
                    }
                    403 -> {
                        val bodyStr = httpResponse.body<String>()
                        if (bodyStr.contains("quotaExceeded")) {
                            Result.failure(Throwable("quotaExceeded"))
                        }
                        else Result.failure(Throwable("********** Error description: ${statusCode.description}. Code: ${statusCode.value} ************"))
                    }
                    else -> {
                        Result.failure(Throwable("********** Error description: ${statusCode.description}. Code: ${statusCode.value} ************"))
                    }
                }
                result
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
                        Result.success(File(""))
                    }
                    else -> {
                        Result.failure(Throwable("********** Error description: ${statusCode.description}. Code: ${statusCode.value} ************"))
                    }
                }
            }
        }
    }

    override suspend fun fetchSuggestions(query: String, lang: String, userAgent: String): Deferred<Result<List<String>>> {
        return coroutineScope {
            async {
                val url = "https://suggestqueries-clients6.youtube.com/complete/search?client=youtube"
                val httpResponse = httpClient.get(url, block = {
                    header("User-Agent", userAgent)
                    parameter("hl", lang)
                    parameter("gl", lang)
                    parameter("q", query)
                })
                val statusCode = httpResponse.status
                when(statusCode.value) {
                    200 -> {
                        val bodyStr = httpResponse.body<String>()
                        val list = decodeSuggestionsFromString(bodyStr)
                        Result.success(list)
                    }
                    else -> {
                        Result.failure(Throwable("********** Error description: ${statusCode.description}. Code: ${statusCode.value} ************"))
                    }
                }
            }
        }
    }

    fun decodeSuggestionsFromString(text: String): List<String> {

        var substring = text.trimMargin().substringAfter("(").substringBeforeLast(")")
        repeat(2) {
            substring = substring.substringAfter("[").substringBeforeLast("]")
        }
        val strList = substring
            .replace("[", "")
            .replace("]", "")
            .split(",")
            .map { item ->
                item.trim()
            }
            .filter { item ->
                item.matches(Regex("\"([^\"]*)\""))
            }
            .map { item ->
                item.trim { char -> char == '"' }
            }
        return strList
    }


}