package com.myapp.lexicon.repository.network

import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.models.SignInData
import com.myapp.lexicon.models.SignUpData
import com.myapp.lexicon.models.Tokens
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



}