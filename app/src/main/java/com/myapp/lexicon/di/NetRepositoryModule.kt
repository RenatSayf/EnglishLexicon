package com.myapp.lexicon.di

import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.common.API_KEY
import com.myapp.lexicon.common.BASE_URL
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.repository.network.NetRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json


class NetRepositoryModule(
    private val clientEngine: HttpClientEngine = CIO.create(block = {
        maxConnectionsCount = 1000
        endpoint {
            connectTimeout = 20000
            keepAliveTime = 20000
            connectAttempts = 5
        }
    }),
    private val baseUrl: String = BASE_URL
) {

    fun provideNetRepository(
        refreshToken: String = ""
    ): INetRepository {

        val httpClient = HttpClient(engine = clientEngine, block = {
            install(plugin = Logging, configure = {
                logger = object : Logger {
                    override fun log(message: String) {
                        "******** HTTP call: $message *****************".logIfDebug()
                    }
                }
                level = LogLevel.ALL
                if (!BuildConfig.DEBUG) {
                    sanitizeHeader { header: String ->
                        header == "Api-Key"
                    }
                }
            })
            install(plugin = ContentNegotiation) {
                Json
            }
            defaultRequest {
                header(key = "Api-Key", value = API_KEY)
            }
        })
        httpClient.plugin(HttpSend).intercept { request: HttpRequestBuilder ->
            val originalCall = execute(request)
            if (originalCall.response.status == HttpStatusCode.Unauthorized) {
                val refreshResponse = httpClient.post(urlString = "$baseUrl/auth/refresh", block = {
                    contentType(ContentType.Application.Json)
                    setBody("{'refresh_token': '$refreshToken'}")
                })
                when(refreshResponse.status) {
                    HttpStatusCode.Accepted -> {
                        val newTokens = Json.decodeFromString<Tokens>(refreshResponse.body())
                        request.url.encodedParameters["access_token"] = newTokens.accessToken
                        execute(request)
                    }
                    else -> {
                        throw ClientRequestException(refreshResponse, refreshResponse.status.description)
                    }
                }
            }
            else {
                originalCall
            }
        }
        return NetRepository(httpClient, baseUrl)
    }


}