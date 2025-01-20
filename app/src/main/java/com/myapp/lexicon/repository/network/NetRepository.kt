package com.myapp.lexicon.repository.network

import com.myapp.lexicon.models.Balance
import com.myapp.lexicon.models.HttpThrowable
import com.myapp.lexicon.models.RevenueX
import com.myapp.lexicon.models.SignInData
import com.myapp.lexicon.models.SignUpData
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.UserProfile
import com.myapp.lexicon.models.UserX
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
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
                    try {
                        val json = response.body<String>()
                        val tokens = jsonDecoder.decodeFromString<Tokens>(json)
                        emit(Result.success(tokens))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
                else -> {
                    val status = response.status
                    val httpThrowable = HttpThrowable(message = status.description, errorCode = status.value)
                    emit(Result.failure(httpThrowable))
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
                HttpStatusCode.Accepted -> {
                    try {
                        val json = response.body<String>()
                        val tokens = jsonDecoder.decodeFromString<Tokens>(json)
                        emit(Result.success(tokens))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
                else -> {
                    val status = response.status
                    val httpThrowable = HttpThrowable(message = status.description, errorCode = status.value)
                    emit(Result.failure(httpThrowable))
                }
            }
        }
    }

    override suspend fun signOut(accessToken: String): Flow<Result<Tokens>> {
        return flow {
            val response = httpClient.post(urlString = "$baseUrl/auth/sign-out", block = {
                contentType(ContentType.Application.Json)
                parameter("token", accessToken)
            })
            when(response.status) {
                HttpStatusCode.OK -> {
                    val json = response.body<String>()
                    try {
                        val tokens = jsonDecoder.decodeFromString<Tokens>(json)
                        emit(Result.success(tokens))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
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
                val response = httpClient.get(urlString = "$baseUrl/mobile-user", block = {
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
                        val status = response.status
                        Result.failure(Throwable("********** Error description: ${status.description}. Code: ${status.value} ************"))
                    }
                }
            }
        }
    }

    override suspend fun updateUserBalance(
        accessToken: String,
        revenue: RevenueX
    ): Flow<Result<Balance>> {
        return flow {
            val response = httpClient.put(urlString = "$baseUrl/user/balance", block = {
                contentType(ContentType.Application.Json)
                parameter("token", accessToken)
                val json = jsonDecoder.encodeToString(RevenueX.serializer(), revenue)
                setBody(json)
            })
            when(response.status) {
                HttpStatusCode.OK -> {
                    val bodyText = response.body<String>()
                    try {
                        val balance = jsonDecoder.decodeFromString<Balance>(bodyText)
                        emit(Result.success(balance))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(Throwable("********** Error description: ${status.description}. Code: ${status.value} ************")))
                }
            }
        }
    }

    override suspend fun updateUserProfile(
        accessToken: String,
        profile: UserProfile
    ): Flow<Result<UserProfile>> {
        return flow {
            val response = httpClient.put(urlString = "$baseUrl/user/profile", block = {
                contentType(ContentType.Application.Json)
                parameter("access_token", accessToken)
                val json = jsonDecoder.encodeToString(UserProfile.serializer(), profile)
                setBody(json)
            })
            when(response.status) {
                HttpStatusCode.OK -> {
                    val bodyText = response.body<String>()
                    try {
                        val profile1 = jsonDecoder.decodeFromString<UserProfile>(bodyText)
                        emit(Result.success(profile1))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(Throwable("********** Error description: ${status.description}. Code: ${status.value} ************")))
                }
            }
        }
    }

    override suspend fun reservedPaymentToUser(
        accessToken: String,
        sum: Int
    ): Flow<Result<Balance>> {
        return flow {
            val response = httpClient.put(urlString = "$baseUrl/user/payment", block = {
                contentType(ContentType.Application.Json)
                parameter("token", accessToken)
                setBody("""{"reserved_payout":"$sum"}""")
            })
            when(response.status) {
                HttpStatusCode.OK -> {
                    val bodyText = response.body<String>()
                    try {
                        val balance = jsonDecoder.decodeFromString<Balance>(bodyText)
                        emit(Result.success(balance))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(Throwable("********** Error description: ${status.description}. Code: ${status.value} ************")))
                }
            }
        }
    }

    override suspend fun updateClickCounter(accessToken: String): Flow<Result<Boolean>> {
        return flow {
            val response = httpClient.put(urlString = "$baseUrl/user/ad-click", block = {
                contentType(ContentType.Application.Json)
                parameter("token", accessToken)
            })
            when(response.status) {
                HttpStatusCode.OK -> {
                    val bodyText = response.body<String>()
                    try {
                        val isDeleted = jsonDecoder.decodeFromString<Boolean>(bodyText)
                        emit(Result.success(isDeleted))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(Throwable("********** Error description: ${status.description}. Code: ${status.value} ************")))
                }
            }
        }
    }

    override suspend fun deleteUser(accessToken: String): Flow<Result<Boolean>> {
        return flow {
            val response = httpClient.put(urlString = "$baseUrl/user/delete", block = {
                contentType(ContentType.Application.Json)
                parameter("token", accessToken)
            })
            when(response.status) {
                HttpStatusCode.OK -> {
                    val bodyText = response.body<String>()
                    try {
                        val isDeleted = jsonDecoder.decodeFromString<Boolean>(bodyText)
                        emit(Result.success(isDeleted))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(Throwable("********** Error description: ${status.description}. Code: ${status.value} ************")))
                }
            }
        }
    }

    override suspend fun forgotPassword(email: String): Flow<Result<String>> {
        return flow {
            val response = httpClient.put(urlString = "$baseUrl/user/forgot-password", block = {
                contentType(ContentType.Application.Json)
                parameter("email", email)
            })
            when(response.status) {
                HttpStatusCode.OK -> {
                    val bodyText = response.body<String>()
                    emit(Result.success(bodyText))
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(Throwable("********** Error description: ${status.description}. Code: ${status.value} ************")))
                }
            }
        }

    }


}