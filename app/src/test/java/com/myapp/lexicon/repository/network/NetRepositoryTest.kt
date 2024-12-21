@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.repository.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class NetRepositoryTest {

    private val testSuggestionsResponse = """window.google.ac.h(["дру",[["друг вокруг",0,[512,433,131]],["другой мир",0,[512,433,131]],
        |["другой взгляд",0,[512,650,433,131]],["другой мужчина",0,[512,650,433,131]],["друзья",0,[512,433]],
        |["дружба это",0,[512]],["дружба",0,[512,433]],["другой мужчина сериал",0,[512,650,433,131]],
        |["друзья сериал",0,[512,433]],["другая женщина",0,[512,433,131]],
        |["другой мужчина смотреть онлайн 2023",0,[512,650,433,131]],["другие фильм",0,[512,433,131]],
        |["друбич",0,[512,433,131]],["друг вокруг скачать",0,[512,433,131]]],
        |{"k":1,"q":"58rBWO2jxrS0e8Hdk5jSH6gXYc4"}])""".trimMargin()

    private lateinit var repository: NetRepository
    private lateinit var mockEngine: MockEngine

    @Before
    fun before() {

        mockEngine = MockEngine { request ->
            respond(
                content = ""
            )
        }
        repository = NetRepository(httpClient = HttpClient(mockEngine))
    }
    @After
    fun after() {
    }

    @Test
    fun decodeSearchResult() {
        val result = Json.decodeFromString<VideoSearchResult>(TEST_VIDEO_LIST)
        Assert.assertTrue(result.videoItems.isNotEmpty())
    }

    @Test
    fun decodeSuggestionsFromString() {
        val actualList = repository.decodeSuggestionsFromString(testSuggestionsResponse)
        val actualSize = actualList.size
        Assert.assertEquals(14, actualSize)
    }

    @Test
    fun fetchSuggestions_success() {
        runBlocking {
            mockEngine = MockEngine(handler = {
                respond(content = testSuggestionsResponse, status = HttpStatusCode.OK)
            })
            repository = NetRepository(HttpClient(mockEngine))

            val result = repository.fetchSuggestions("xxx", "ru", userAgent = "USER_AGENT").await()
            result.onSuccess { value: List<String> ->
                Assert.assertEquals(14, value.size)
            }
            result.onFailure { exception ->
                exception.printStackTrace()
                Assert.assertTrue(exception.message, false)
            }
        }
    }

    @Test
    fun getSearchResult_success() {
        runBlocking {
            mockEngine = MockEngine(handler = { requestData ->
                if (requestData.url.parameters.contains("q", "friends"))
                    respond(content = TEST_VIDEO_LIST, status = HttpStatusCode.OK)
                else
                    respond(content = "")
            })

            repository = NetRepository(HttpClient(mockEngine))

            val actualResult = repository.getSearchResult(query = "friends", pageToken = "", maxResults = 5).await()
            actualResult.onSuccess { value: VideoSearchResult ->
                Assert.assertTrue(value.videoItems.isNotEmpty())
            }
            actualResult.onFailure { exception ->
                exception.printStackTrace()
                Assert.assertTrue(exception.message, false)
            }
        }
    }
}