package com.myapp.lexicon.repository.network

import com.myapp.lexicon.helpers.castToHttpThrowable
import com.myapp.lexicon.helpers.getCRC32CheckSum
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
            val response = httpClient.post(urlString = "$baseUrl/auth/sign-in", block = {
                contentType(ContentType.Application.Json)
                val json = jsonDecoder.encodeToString(SignInData.serializer(), data)
                setBody(json)
            })
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
            val response = httpClient.post(urlString = "$baseUrl/auth/sign-out", block = {
                contentType(ContentType.Application.Json)
                parameter("token", accessToken)
            })
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
            async {
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
            val response = httpClient.put(urlString = "$baseUrl/user/balance-increment", block = {
                contentType(ContentType.Application.Json)
                parameter("access_token", accessToken)
                val json = jsonDecoder.encodeToString(RevenueX.serializer(), revenue)
                setBody(json)
            })
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
            val response = httpClient.put(urlString = "$baseUrl/user/profile", block = {
                contentType(ContentType.Application.Json)
                parameter("access_token", accessToken)
                val json = jsonDecoder.encodeToString(UserProfile.serializer(), profile)
                setBody(json)
            })
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
            val response = httpClient.put(urlString = "$baseUrl/user/new-user-data", block = {
                contentType(ContentType.Application.Json)
                parameter("access_token", accessToken)
                setBody(json)
            })
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
        sum: Int
    ): Flow<Result<UserX>> {
        return flow {
            val response = httpClient.put(urlString = "$baseUrl/user/payment", block = {
                contentType(ContentType.Application.Json)
                parameter("token", accessToken)
                setBody("""{"reserved_payout":"$sum"}""")
            })
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
            val response = httpClient.put(urlString = "$baseUrl/user/ad-click", block = {
                contentType(ContentType.Application.Json)
                parameter("token", accessToken)
            })
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
            val response = httpClient.put(urlString = "$baseUrl/user/delete", block = {
                contentType(ContentType.Application.Json)
                parameter("token", accessToken)
            })
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
                    val httpThrowable = HttpThrowable(message = status.description, errorCode = status.value)
                    emit(Result.failure(httpThrowable))
                }
            }
        }

    }

    override suspend fun fetchRemoteConfig(checkSum: Long): Flow<Result<String?>> {
        return flow {
            val response = httpClient.get(urlString = "$baseUrl/config", block = {
                contentType(ContentType.Application.Json)
                parameter("check_sum", checkSum)
            })
            when(response.status) {
                HttpStatusCode.NotFound, HttpStatusCode.BadGateway, HttpStatusCode.BadRequest -> {
                    runCatching {
                        //val json = response.body<String>()

                        val remoteCheckSum = jsonConfig.getCRC32CheckSum()
                        if (remoteCheckSum != checkSum) {
                            jsonConfig
                        }
                        else {
                            null
                        }
                    }.onSuccess { config ->
                        emit(Result.success(config))
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

    private val jsonConfig = """{
  "adTypePerScreen" : {
    "main" : 1,
    "service" : 1,
    "test" : 3,
    "translate" : 1,
    "video" : 3
  },
  "bannerIds" : {
    "main" : "R-M-711878-1",
    "service" : "R-M-711878-1",
    "editor" : "R-M-711878-2",
    "translate" : "R-M-711878-3"
  },
  "nativeIds" : {
    "main" : "R-M-711878-14",
    "service" : "R-M-711878-14",
    "translate" : "R-M-711878-15",
    "test" : "R-M-711878-15",
    "video" : "R-M-711878-14"
  },
  "interstitialAdIds" : {
    "main" : "R-M-711878-4",
    "service" : "R-M-711878-4",
    "translate" : "R-M-711878-5",
    "test" : "R-M-711878-6",
    "video" : "R-M-711878-6"
  },
  "rewardedIds" : {
    "main" : "R-M-711878-10",
    "service" : "R-M-711878-10",
    "translate" : "R-M-711878-11",
    "test" : "R-M-711878-12",
    "video" : "R-M-711878-12"
  },
  "feedIds" : {
    "main" : "R-M-711878-18",
    "service" : "R-M-711878-18",
    "translate" : "R-M-711878-18",
    "test" : "R-M-711878-18",
    "video" : "R-M-711878-18"
  }
}"""


}