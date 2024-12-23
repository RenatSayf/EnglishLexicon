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



}