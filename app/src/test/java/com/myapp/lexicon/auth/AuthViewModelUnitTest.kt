@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.myapp.lexicon.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.KEY_API
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.UserState
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class AuthViewModelUnitTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()



    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {
    }

    @Test
    fun registerForNewUser_success() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{
                          "access_token": "access00000000000",
                          "refresh_token": "refresh0000000000000"
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
                Assert.assertEquals("access00000000000", tokens.accessToken)
                Assert.assertEquals("refresh0000000000000", tokens.refreshToken)
            }
            override fun onAuthorizationRequired() {}
        })
        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.registerForNewUser(email = "testuser@gmail.com", password = "123456", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                UserState.AlreadyExists -> {
                    Assert.assertTrue(false)
                }
                is UserState.HttpFailure -> {
                    Assert.assertTrue(false)
                }
                is UserState.LogUp -> {
                    Assert.assertEquals("access00000000000", state.tokens.accessToken)
                    Assert.assertEquals("refresh0000000000000", state.tokens.refreshToken)
                }
                is UserState.TokensUpdated -> {
                    Assert.assertEquals("access00000000000", state.tokens.accessToken)
                    Assert.assertEquals("refresh0000000000000", state.tokens.refreshToken)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }

        }

    }

    @Test
    fun registerForNewUser_error_422() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{"detail": "The password length is too short"}""",
                    status = HttpStatusCode.UnprocessableEntity,
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
        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.registerForNewUser(email = "testuser@gmail.com", password = "123", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                is UserState.PasswordValid -> {
                    Assert.assertEquals(false, state.flag)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }
        }

    }

    @Test
    fun registerForNewUser_error_409() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{"detail": "User is already exists"}""",
                    status = HttpStatusCode.Conflict,
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
        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.registerForNewUser(email = "testuser@gmail.com", password = "123456", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                is UserState.AlreadyExists -> {
                    Assert.assertTrue(true)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }
        }

    }

    @Test
    fun registerForNewUser_error_500() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{"detail": "Internal server error"}""",
                    status = HttpStatusCode.InternalServerError,
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
        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.registerForNewUser(email = "testuser@gmail.com", password = "123456", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                is UserState.HttpFailure -> {
                    Assert.assertEquals("Internal Server Error", state.message)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }
        }

    }

    @Test
    fun logInWithEmailAndPassword_success_202() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{
                          "access_token": "access00000000000",
                          "refresh_token": "refresh0000000000000"
                        }""".trimIndent(),
                    status = HttpStatusCode.Accepted,
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

        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.logInWithEmailAndPassword(email = "testuser@gmail.com", password = "123456", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                is UserState.LogIn -> {
                    Assert.assertEquals("access00000000000", state.tokens.accessToken)
                    Assert.assertEquals("refresh0000000000000", state.tokens.refreshToken)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }
        }
    }

    @Test
    fun logInWithEmailAndPassword_error_404() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{"detail": "User not found"}""",
                    status = HttpStatusCode.NotFound,
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

        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.logInWithEmailAndPassword(email = "testuser@gmail.com", password = "123456", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                is UserState.NotRegistered -> {
                    Assert.assertTrue(true)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }
        }
    }

    @Test
    fun logInWithEmailAndPassword_error_406() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{"detail": "Invalid email or password"}""",
                    status = HttpStatusCode.NotAcceptable,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondError(status = HttpStatusCode.Forbidden)
            }
        }
        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repositoryModule.setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {}
            override fun onAuthorizationRequired() {
                Assert.assertTrue(true)
            }
        })

        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.logInWithEmailAndPassword(email = "testuser@gmail.com", password = "123456", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                is UserState.NotAcceptable -> {
                    Assert.assertTrue(true)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }
        }
    }

    @Test
    fun resetUserPassword_success_200() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{"message": "Email sent successfully"}""",
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
            override fun onAuthorizationRequired() {
                Assert.assertTrue(false)
            }
        })

        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.resetUserPassword(email = "testuser@gmail.com", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                is UserState.PasswordReset -> {
                    Assert.assertTrue(true)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }
        }
    }

    @Test
    fun resetUserPassword_error_404() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{"detail": "User not found"}""",
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondError(status = HttpStatusCode.Forbidden)
            }
        }
        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repositoryModule.setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {}
            override fun onAuthorizationRequired() {
                Assert.assertTrue(false)
            }
        })

        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.resetUserPassword(email = "testuser@gmail.com", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                is UserState.NotRegistered -> {
                    Assert.assertTrue(true)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }
        }
    }

    @Test
    fun resetUserPassword_error_500() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{"detail": "INTERNAL_SERVER_ERROR"}""",
                    status = HttpStatusCode.InternalServerError,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondError(status = HttpStatusCode.Forbidden)
            }
        }
        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repositoryModule.setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {}
            override fun onAuthorizationRequired() {
                Assert.assertTrue(false)
            }
        })

        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {
            viewModel.resetUserPassword(email = "testuser@gmail.com", dispatcher = Dispatchers.Unconfined)

            delay(2000)

            val state = viewModel.state.value
            when(state) {
                is UserState.HttpFailure -> {
                    Assert.assertEquals("Internal Server Error", state.message)
                }
                else -> {
                    Assert.assertTrue(false)
                }
            }
        }
    }

    @Test
    fun deleteUserAccount_success_200() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = "true",
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
            override fun onAuthorizationRequired() {
                Assert.assertTrue(false)
            }
        })

        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {

            delay(2000)

            viewModel.deleteUserAccount(
                token = "XXXXXXXX",
                onSuccess = {
                    Assert.assertTrue(true)
                },
                onComplete = {
                    Assert.assertTrue(false)
                },
                dispatcher = Dispatchers.Unconfined
            )
        }
    }

    @Test
    fun deleteUserAccount_error_500() {
        val mockEngine = MockEngine.invoke { request ->
            val isApiKey = request.headers.contains(KEY_API)
            if (isApiKey) {
                respond(
                    content = """{"detail": "INTERNAL_SERVER_ERROR"}""",
                    status = HttpStatusCode.InternalServerError,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respondError(status = HttpStatusCode.Forbidden)
            }
        }
        val repositoryModule = NetRepositoryModule(baseUrl = "", clientEngine = mockEngine)
        repositoryModule.setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {}
            override fun onAuthorizationRequired() {
                Assert.assertTrue(false)
            }
        })

        val viewModel = AuthViewModel(repositoryModule)

        runBlocking {

            delay(2000)

            viewModel.deleteUserAccount(
                token = "XXXXXXXX",
                onSuccess = {
                    Assert.assertTrue(false)
                },
                onComplete = {
                    Assert.assertTrue(true)
                },
                dispatcher = Dispatchers.Unconfined
            )
        }
    }


}