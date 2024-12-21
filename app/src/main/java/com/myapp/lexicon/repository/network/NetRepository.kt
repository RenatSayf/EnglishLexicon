package com.myapp.lexicon.repository.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

open class NetRepository(
    private val httpClient: HttpClient
): INetRepository {

    private val jsonDecoder = Json { ignoreUnknownKeys }

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