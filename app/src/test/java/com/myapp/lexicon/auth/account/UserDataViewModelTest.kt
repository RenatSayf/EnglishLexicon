package com.myapp.lexicon.auth.account

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.KEY_API
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.models.Tokens
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test



class UserDataViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {

    }

    @Test
    fun fetchUserData_Success() {

        val userJson = """{
  "email": "user-test@mail.com",
  "today_balance": 10.0,
  "yesterday_balance": 20.0,
  "month_balance": 100.0,
  "previous_month_balance": 200,
  "reserved_payout": 300,
  "currency_code": "RUB",
  "phone": "+79998887755",
  "first_name": "Кутман",
  "second_name": "Уулу",
  "last_name": "Бекмурза",
  "bank_name": "Сбербанк",
  "bank_card": null,
  "message_to_user": "Тестовое сообщение"
}""".trimIndent()

        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = userJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondError(status = HttpStatusCode.Forbidden)
            }
        }
        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repositoryModule.setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {
                Assert.assertEquals("access00000000000", tokens.accessToken)
                Assert.assertEquals("refresh0000000000000", tokens.refreshToken)
            }
            override fun onAuthorizationRequired() {}
        })
//        val viewModel = UserDataViewModel(repositoryModule) // TODO not mocked firebase
//
//        runBlocking {
//
//            viewModel.fetchUserData(token = "access00000000000")
//
//            delay(2000)
//            val userState = viewModel.userState.value
//            when(userState) {
//                is UserDataViewModel.UserDataState.ReceivedUserData -> {
//                    val user = userState.user
//                    Assert.assertEquals("user-test@mail.com", user.email)
//                }
//                else -> {
//                    Assert.assertTrue(false)
//                }
//            }
//        }
    }

}