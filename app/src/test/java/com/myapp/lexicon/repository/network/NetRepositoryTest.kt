package com.myapp.lexicon.repository.network

import com.myapp.lexicon.common.APP_VERSION
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.models.Balance
import com.myapp.lexicon.models.RevenueX
import com.myapp.lexicon.models.SignInData
import com.myapp.lexicon.models.SignUpData
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.UserProfile
import com.myapp.lexicon.models.UserX
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
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
    fun signUp_success() {
        val signUpData = SignUpData(
            appVersion = "v.Test",
            email = "user-test@mail.com",
            password = "123456"
        )
        mockEngine = MockEngine.invoke {
            respond(
                content = """{
                  "access_token": "access00000000000",
                  "refresh_token": "refresh0000000000000"
                }""".trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repository = repositoryModule.provideNetRepository(refreshToken = "")
        runBlocking {
            repository.signUp(signUpData).collect(collector = { result ->
                result.onSuccess { value: Tokens ->
                    Assert.assertEquals("access00000000000", value.accessToken)
                    Assert.assertEquals("refresh0000000000000", value.refreshToken)
                }
                result.onFailure { exception: Throwable ->
                    exception.message!!.logIfDebug()
                    Assert.assertTrue(false)
                }
            })
        }
    }

    @Test
    fun signIn_success() {
        val signInData = SignInData(
            email = "user-test@mail.com",
            password = "123456"
        )
        mockEngine = MockEngine.invoke {
            respond(
                content = """{
                  "access_token": "access00000000000",
                  "refresh_token": "refresh0000000000000"
                }""".trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repository = repositoryModule.provideNetRepository(refreshToken = "")
        runBlocking {
            repository.signIn(signInData).collect(collector = { result ->
                result.onSuccess { value: Tokens ->
                    Assert.assertEquals("access00000000000", value.accessToken)
                    Assert.assertEquals("refresh0000000000000", value.refreshToken)
                }
                result.onFailure { exception: Throwable ->
                    exception.message!!.logIfDebug()
                    Assert.assertTrue(false)
                }
            })
        }
    }

    @Test
    fun signOut_success() {
        mockEngine = MockEngine.invoke {
            respond(
                content = """{
                  "access_token": "",
                  "refresh_token": ""
                }""".trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repository = repositoryModule.provideNetRepository(refreshToken = "")

        runBlocking {
            repository.signOut(accessToken = "access00000000000").collect(collector = { result ->
                result.onSuccess { value: Tokens ->
                    Assert.assertEquals("", value.accessToken)
                    Assert.assertEquals("", value.refreshToken)
                }
                result.onFailure { exception: Throwable ->
                    exception.message!!.logIfDebug()
                    Assert.assertTrue(false)
                }
            })
        }
    }

    @Test
    fun getUserProfile_with_expired_token() {

        val userJson = """{
  "today_balance": 10.5,
  "yesterday_balance": 20.3,
  "month_balance": 100.89,
  "currency_code": "RUB",
  "reserved_payout": 200,
  "email": "user-test@mail.com",
  "phone": "+79998887755",
  "first_name": "User",
  "second_name": null,
  "last_name": "Test",
  "bank_name": "BCS",
  "bank_card": null,
  "message_to_user": null
}"""

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
                        content = userJson,
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
            result.onSuccess { value: UserX ->
                Assert.assertEquals(value.email, "user-test@mail.com")
            }
            result.onFailure { exception: Throwable ->
                exception.message!!.logIfDebug()
                Assert.assertTrue(false)
            }
        }
    }

    @Test
    fun updateUserBalance_success() {
        val accessToken = "XXXXXXXXXXXXXX"
        val responseJson = """{
  "today_balance": 10.5,
  "yesterday_balance": 20.6,
  "month_balance": 50.59,
  "currency_code": "RUB",
  "reserved_payout": 200
}""".trimIndent()

        mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains("Api-Key")
            if (isApiKey) {
                when(request.url.fullPath) {
                    "/user/balance?token=$accessToken" -> {
                        respond(
                            content = responseJson,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    else -> {
                        respondBadRequest()
                    }
                }
            } else {
                respondBadRequest()
            }
        }

        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repository = repositoryModule.provideNetRepository(refreshToken = "VVVVVVVVV")

        val revenue = RevenueX(
            currencyCode = "RUB",
            lastAdId = "Adfsdfsdfjk",
            revenueRub = 0.12,
            revenueUsd = 0.0012
        )
        runBlocking {
            repository.updateUserBalance(accessToken = accessToken, revenue = revenue)
                .collect(collector = { result ->
                    result.onSuccess { value: Balance ->
                        Assert.assertEquals(10.5, value.todayBalance, 0.0001)
                        Assert.assertEquals(20.6, value.yesterdayBalance, 0.0001)
                        Assert.assertEquals(50.59, value.monthBalance, 0.0001)
                        Assert.assertEquals(200, value.reservedPayout)
                    }
                    result.onFailure { exception: Throwable ->
                        exception.message!!.logIfDebug()
                        Assert.assertTrue(false)
                    }
                })
        }

    }

    @Test
    fun updateUserProfile_success() {
        val accessToken = "XXXXXXXXXXXXXX"
        val responseJson = """{
  "email": "user-test@mail.com",
  "phone": "+79998887755",
  "first_name": "User",
  "second_name": null,
  "last_name": "Test",
  "bank_name": "BCS",
  "bank_card": null
}""".trimEnd()

        mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains("Api-Key")
            if (isApiKey) {
                when(request.url.fullPath) {
                    "/user/profile?access_token=$accessToken" -> {
                        respond(
                            content = responseJson,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    else -> {
                        respondBadRequest()
                    }
                }
            }
            else {
                respondBadRequest()
            }
        }

        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repository = repositoryModule.provideNetRepository(refreshToken = "VVVVVVVVV")

        val userProfile = UserProfile(
            email = "user-test@mail.com",
            phone = "+79998887755",
            firstName = "User",
            secondName = null,
            lastName = "Test",
            bankCard = null,
            bankName = "BCS"
        )
        runBlocking {
            repository.updateUserProfile(accessToken = accessToken, profile = userProfile)
                .collect(collector = { result ->
                    result.onSuccess { value: UserProfile ->
                        Assert.assertEquals("user-test@mail.com", value.email)
                        Assert.assertEquals("+79998887755", value.phone)
                        Assert.assertEquals(null, value.secondName)
                        Assert.assertEquals(APP_VERSION, value.appVersion)
                    }
                    result.onFailure { exception: Throwable ->
                        exception.message!!.logIfDebug()
                        Assert.assertTrue(false)
                    }
                })
        }

    }

    @Test
    fun reservedPaymentToUser_succes() {
        val accessToken = "XXXXXXXXXXXXXX"

        mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains("Api-Key")
            if (isApiKey) {
                val responseJson = """{
                      "today_balance": 10.5,
                      "yesterday_balance": 20.4,
                      "month_balance": 50.54,
                      "currency_code": "RUB",
                      "reserved_payout": 200
                    }""".trimIndent()
                when(request.url.fullPath) {
                    "/user/payment?token=$accessToken" -> {
                        respond(
                            content = responseJson,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    else -> {
                        respondBadRequest()
                    }
                }
            }
            else {
                respondBadRequest()
            }
        }

        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repository = repositoryModule.provideNetRepository(refreshToken = "VVVVVVVVV")

        runBlocking {
            val sum = 200
            repository.reservedPaymentToUser(accessToken = accessToken, sum = sum)
                .collect(collector = { result ->
                    result.onSuccess { value: Balance ->
                        Assert.assertEquals(200, value.reservedPayout)
                    }
                    result.onFailure { exception: Throwable ->
                        exception.message!!.logIfDebug()
                        Assert.assertTrue(false)
                    }
                })
        }
    }



}