package com.myapp.lexicon.auth.account

import android.util.proto.ProtoOutputStream
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
import org.junit.Test
import org.junit.Rule

class AccountViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun signOut_success_200() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{
                          "access_token": "",
                          "refresh_token": ""
                        }""".trimIndent(),
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
                Assert.assertEquals("", tokens.accessToken)
                Assert.assertEquals("", tokens.refreshToken)
            }
            override fun onAuthorizationRequired() {}
        })

        val viewModel = AccountViewModel(repositoryModule)

        runBlocking {
            delay(1000)

            viewModel.signOut(
                token = "XXXXXXXXX",
                onStart = {
                    Assert.assertTrue(true)
                },
                onSuccess = { tokens ->
                    Assert.assertEquals("", tokens.accessToken)
                    Assert.assertEquals("", tokens.refreshToken)
                },
                onComplete = {
                    Assert.assertTrue(false)
                }
            )
        }
    }
}