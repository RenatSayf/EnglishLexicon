package com.myapp.lexicon.repository.network

import com.myapp.lexicon.models.SignInData
import com.myapp.lexicon.models.SignUpData
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.UserX
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

open class NetRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String
): INetRepository {

    private val jsonDecoder = Json { ignoreUnknownKeys }

    override suspend fun signUp(data: SignUpData): Flow<Result<Tokens>> {
        return flow {
            val response = httpClient.post(urlString = "$baseUrl/auth/sign-up", block = {
                contentType(ContentType.Application.Json)
                val json = jsonDecoder.encodeToString(SignUpData.serializer(), data)
                setBody(json)
            })
            when(response.status) {
                HttpStatusCode.OK -> {
                    val json = response.body<String>()
                    val tokens = jsonDecoder.decodeFromString<Tokens>(json)
                    emit(Result.success(tokens))
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(Throwable("********** Error description: ${status.description}. Code: ${status.value} ************")))
                }
            }
        }
    }

    override suspend fun signIn(data: SignInData): Flow<Result<Tokens>> {
        return flow {
            val response = httpClient.post(urlString = "$baseUrl/auth/sign-in", block = {
                contentType(ContentType.Application.Json)
                val json = jsonDecoder.encodeToString(SignInData.serializer(), data)
                setBody(json)
            })
            when(response.status) {
                HttpStatusCode.OK -> {
                    val json = response.body<String>()
                    val tokens = jsonDecoder.decodeFromString<Tokens>(json)
                    emit(Result.success(tokens))
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(Throwable("********** Error description: ${status.description}. Code: ${status.value} ************")))
                }
            }
        }
    }

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


}