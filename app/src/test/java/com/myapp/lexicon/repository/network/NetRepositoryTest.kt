@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.repository.network

import com.myapp.lexicon.di.NetRepositoryModule
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test


class NetRepositoryTest {


    private lateinit var repository: INetRepository
    private lateinit var mockEngine: MockEngine

    @Before
    fun before() {


    }
    @After
    fun after() {
    }

    @Test
    fun getUserProfile() {

        val oldAccessToken = "access00000000000"
        val newAccessToken = "access11111111111"
        val refreshToken = "refresh0000000000000"

        mockEngine = MockEngine { request ->
            when(request.url.fullPath) {
                "/auth/refresh" -> {
                    respond(
                        content = """
                            {
                              "access_token": "$newAccessToken",
                              "refresh_token": "refresh2222222222"
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.Accepted,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),

                    )
                }
                "/user?access_token=$oldAccessToken" -> {
                    respond(
                        content = "{'error':'error'}",
                        status = HttpStatusCode.Unauthorized,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/user?access_token=$newAccessToken" -> {
                    respond(
                        content = "{'Success':'success'}",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> {
                    respondBadRequest()
                }
            }
        }
        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repository = repositoryModule.provideNetRepository(refreshToken = refreshToken)
        runBlocking {
            val result = repository.getUserProfile(accessToken = oldAccessToken).await()
            result.onSuccess { value: String ->
                value
            }
            result.onFailure { exception: Throwable ->
                throw exception as ClientRequestException
            }
        }
    }



}