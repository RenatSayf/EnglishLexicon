package com.myapp.lexicon.repository.network

import com.myapp.lexicon.models.UserX
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

open class NetRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String
): INetRepository {

    private val jsonDecoder = Json { ignoreUnknownKeys }

    override suspend fun getUserProfile(accessToken: String): Deferred<Result<UserX>> {
        return coroutineScope {
            async {
                val response = httpClient.get(urlString = "$baseUrl/user", block = {
                    contentType(ContentType.Application.Json)
                    parameter("access_token", accessToken)
                })
                when(response.status) {
                    HttpStatusCode.OK -> {
                        val bodyText = response.body<String>()
                        try {
                            val user = jsonDecoder.decodeFromString<UserX>(bodyText)
                            Result.success(user)
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }
                    else -> {
                        val statusCode = response.status
                        Result.failure(Throwable("********** Error description: ${statusCode.description}. Code: ${statusCode.value} ************"))
                    }
                }
            }
        }
    }

    suspend fun fetchSuggestions(query: String, lang: String, userAgent: String): Deferred<Result<List<String>>> {
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