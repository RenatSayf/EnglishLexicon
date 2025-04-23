package com.myapp.lexicon.repository.network

import com.myapp.lexicon.helpers.castToHttpThrowable
import com.myapp.lexicon.models.AdsReward
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONObject


open class NetRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String
): INetRepository {

    private val jsonDecoder = Json { ignoreUnknownKeys }

    override suspend fun signUp(data: SignUpData): Flow<Result<Tokens>> {
        return flow {
            val response = withContext(context = Dispatchers.IO){
                httpClient.post(urlString = "$baseUrl/auth/sign-up", block = {
                    contentType(ContentType.Application.Json)
                    val json = jsonDecoder.encodeToString(SignUpData.serializer(), data)
                    setBody(json)
                })
            }
            when(response.status) {
                HttpStatusCode.OK -> {
                    runCatching {
                        val json = response.body<String>()
                        jsonDecoder.decodeFromString<Tokens>(json)
                    }.onSuccess { tokens ->
                        emit(Result.success(tokens))
                    }.onFailure { t ->
                        val throwable = t.castToHttpThrowable()
                        Result.failure<Throwable>(throwable)
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
            val response = withContext(context = Dispatchers.IO) {
                httpClient.post(urlString = "$baseUrl/auth/sign-in", block = {
                    contentType(ContentType.Application.Json)
                    val json = jsonDecoder.encodeToString(SignInData.serializer(), data)
                    setBody(json)
                })
            }
            when(response.status) {
                HttpStatusCode.Accepted, HttpStatusCode.OK -> {
                    runCatching {
                        val json = response.body<String>()
                        jsonDecoder.decodeFromString<Tokens>(json)
                    }.onSuccess { tokens ->
                        emit(Result.success(tokens))
                    }.onFailure { t ->
                        val throwable = t.castToHttpThrowable()
                        Result.failure<Throwable>(throwable)
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
            val response = withContext(context = Dispatchers.IO) {
                httpClient.post(urlString = "$baseUrl/auth/sign-out", block = {
                    contentType(ContentType.Application.Json)
                    parameter("access_token", accessToken)
                })
            }
            when(response.status) {
                HttpStatusCode.OK -> {
                    runCatching {
                        val json = response.body<String>()
                        jsonDecoder.decodeFromString<Tokens>(json)

                    }.onSuccess { tokens ->
                        emit(Result.success(tokens))
                    }.onFailure { t ->
                        val throwable = t.castToHttpThrowable()
                        Result.failure<Throwable>(throwable)
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

    override suspend fun getUserProfile(accessToken: String): Deferred<Result<UserX>> {
        return coroutineScope {
            async(context = Dispatchers.IO) {
                val response = httpClient.get(urlString = "$baseUrl/mobile-user", block = {
                    contentType(ContentType.Application.Json)
                    parameter("access_token", accessToken)
                })
                when(response.status) {
                    HttpStatusCode.OK -> {
                        runCatching {
                            val bodyText = response.body<String>()
                            jsonDecoder.decodeFromString<UserX>(bodyText)
                        }.onSuccess { user ->
                            Result.success(user)
                        }.onFailure { t ->
                            val throwable = t.castToHttpThrowable()
                            Result.failure<Throwable>(throwable)
                        }
                    }
                    else -> {
                        val status = response.status
                        Result.failure(HttpThrowable(message = status.description, errorCode = status.value))
                    }
                }
            }
        }
    }

    override suspend fun updateUserBalance(
        accessToken: String,
        revenue: RevenueX
    ): Flow<Result<AdsReward>> {
        return flow {
            val response = withContext(context = Dispatchers.IO) {
                httpClient.put(urlString = "$baseUrl/user/balance-increment", block = {
                    contentType(ContentType.Application.Json)
                    parameter("access_token", accessToken)
                    val json = jsonDecoder.encodeToString(RevenueX.serializer(), revenue)
                    setBody(json)
                })
            }
            when(response.status) {
                HttpStatusCode.OK -> {
                    val bodyText = response.body<String>()
                    runCatching {
                        jsonDecoder.decodeFromString<AdsReward>(bodyText)
                    }.onSuccess { reward: AdsReward ->
                        emit(Result.success(reward))
                    }.onFailure { t ->
                        val throwable = t.castToHttpThrowable()
                        Result.failure<Throwable>(throwable)
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

    override suspend fun updateUserProfile(
        accessToken: String,
        profile: UserProfile
    ): Flow<Result<UserX>> {
        return flow {
            val response = withContext(context = Dispatchers.IO) {
                httpClient.put(urlString = "$baseUrl/user/profile", block = {
                    contentType(ContentType.Application.Json)
                    parameter("access_token", accessToken)
                    val json = jsonDecoder.encodeToString(UserProfile.serializer(), profile)
                    setBody(json)
                })
            }
            when(response.status) {
                HttpStatusCode.OK -> {
                    runCatching {
                        val bodyText = response.body<String>()
                        jsonDecoder.decodeFromString<UserX>(bodyText)
                    }.onSuccess { user ->
                        emit(Result.success(user))
                    }.onFailure { t ->
                        val throwable = t.castToHttpThrowable()
                        Result.failure<Throwable>(throwable)
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

    override suspend fun updateUserData(
        accessToken: String,
        json: String
    ): Flow<Result<UserX>> {
        return flow {
            val response = withContext(context = Dispatchers.IO) {
                httpClient.put(urlString = "$baseUrl/user/new-user-data", block = {
                    contentType(ContentType.Application.Json)
                    parameter("access_token", accessToken)
                    setBody(json)
                })
            }
            when(response.status) {
                HttpStatusCode.OK -> {
                    runCatching {
                        val bodyText = response.body<String>()
                        jsonDecoder.decodeFromString<UserX>(bodyText)
                    }.onSuccess { user ->
                        emit(Result.success(user))
                    }.onFailure { t ->
                        val throwable = t.castToHttpThrowable()
                        Result.failure<Throwable>(throwable)
                    }
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(HttpThrowable (message = status.description, errorCode = status.value)))
                }
            }
        }
    }

    override suspend fun reservedPaymentToUser(
        accessToken: String,
        map: Map<String, Any>
    ): Flow<Result<UserX>> {
        return flow {
            val response = withContext(context = Dispatchers.IO) {
                httpClient.put(urlString = "$baseUrl/user/payment", block = {
                    contentType(ContentType.Application.Json)
                    parameter("access_token", accessToken)
                    val json = JSONObject(map).toString()
                    setBody(json)
                })
            }
            when(response.status) {
                HttpStatusCode.OK -> {
                    runCatching {
                        val bodyText = response.body<String>()
                        jsonDecoder.decodeFromString<UserX>(bodyText)
                    }.onSuccess { user ->
                        emit(Result.success(user))
                    }.onFailure { t ->
                        val throwable = t.castToHttpThrowable()
                        Result.failure<Throwable>(throwable)
                    }
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(HttpThrowable (message = status.description, errorCode = status.value)))
                }
            }
        }
    }

    override suspend fun updateClickCounter(accessToken: String): Flow<Result<Boolean>> {
        return flow {
            val response = withContext(context = Dispatchers.IO) {
                httpClient.put(urlString = "$baseUrl/user/ad-click", block = {
                    contentType(ContentType.Application.Json)
                    parameter("token", accessToken)
                })
            }
            when(response.status) {
                HttpStatusCode.OK -> {
                    runCatching {
                        val bodyText = response.body<String>()
                        jsonDecoder.decodeFromString<Boolean>(bodyText)
                    }.onSuccess { isUpdated ->
                        emit(Result.success(isUpdated))
                    }.onFailure { t ->
                        val throwable = t.castToHttpThrowable()
                        Result.failure<Throwable>(throwable)
                    }
                }
                else -> {
                    val status = response.status
                    emit(Result.failure(HttpThrowable (message = status.description, errorCode = status.value)))
                }
            }
        }
    }

    override suspend fun deleteUser(accessToken: String): Flow<Result<Boolean>> {
        return flow {
            val response = withContext(context = Dispatchers.IO) {
                httpClient.put(urlString = "$baseUrl/user/delete", block = {
                    contentType(ContentType.Application.Json)
                    parameter("access_token", accessToken)
                })
            }
            when(response.status) {
                HttpStatusCode.OK -> {
                    runCatching {
                        val bodyText = response.body<String>()
                        jsonDecoder.decodeFromString<Boolean>(bodyText)
                    }.onSuccess { isDeleted ->
                        emit(Result.success(isDeleted))
                    }.onFailure { t ->
                        val throwable = t.castToHttpThrowable()
                        Result.failure<Throwable>(throwable)
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

    override suspend fun forgotPassword(email: String): Flow<Result<String>> {
        return flow {
            val response = withContext(context = Dispatchers.IO) {
                httpClient.put(urlString = "$baseUrl/user/forgot-password", block = {
                    contentType(ContentType.Application.Json)
                    parameter("email", email)
                })
            }
            when(response.status) {
                HttpStatusCode.OK -> {
                    val bodyText = response.body<String>()
                    emit(Result.success(bodyText))
                }
                else -> {
                    val status = response.status
                    val httpThrowable = HttpThrowable(message = status.description, errorCode = status.value)
                    emit(Result.failure(httpThrowable))
                }
            }
        }
    }

    override suspend fun fetchRemoteConfig(checkSum: Long): Deferred<Result<String?>> {
        return coroutineScope {
            async(context = Dispatchers.IO) {
                val response = httpClient.get(urlString = "$baseUrl/config/client", block = {
                    contentType(ContentType.Application.Json)
                    parameter("client_sum", checkSum)
                })

                when(response.status) {
                    HttpStatusCode.OK -> {
                        runCatching {
                            response.body<String>()
                        }.onSuccess { config ->
                            (Result.success(config))
                        }.onFailure { t ->
                            val throwable = t.castToHttpThrowable()
                            Result.failure<Throwable>(throwable)
                        }
                    }
                    HttpStatusCode.NoContent -> {
                        Result.success(null)
                    }
                    else -> {
                        val status = response.status
                        val httpThrowable = HttpThrowable(message = status.description, errorCode = status.value)
                        Result.failure(httpThrowable)
                    }
                }
            }
        }
    }


}