package com.myapp.lexicon.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.KEY_API
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.models.AppConfig
import com.myapp.lexicon.models.Tokens
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RemoteConfigViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: RemoteConfigViewModel

    @Before
    fun setUp() {
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
            override fun onUpdateTokens(tokens: Tokens) {}
            override fun onAuthorizationRequired() {}
        })

        viewModel = RemoteConfigViewModel(repositoryModule)
    }

    @After
    fun tearDown() {

    }

    @Test
    fun encodeToJsonString() {

        val config = DEFAULT_CONFIG

        val jsonString = viewModel.encodeToJsonString(config)

        Assert.assertTrue(jsonString != null)

    }

    @Test
    fun decodeFromString() {

        val jsonString = DEFAULT_CONFIG_JSON
        val config = viewModel.decodeFromString(json = jsonString)

        Assert.assertTrue(config is AppConfig)
    }

}