package com.myapp.lexicon.network

import com.myapp.lexicon.models.User
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class NetClient(
    engine: HttpClientEngine = CIO.create(),
    private val baseUrl: String = "https://api.yookassa.ru/v3/payouts",
    private val secretKey: String
): INetClient {

    private val client = HttpClient(engine).apply {
        config {
            install(ContentNegotiation)
        }
    }
    private val gatewayId = "XXXXX"
    suspend fun sendPayoutRequest(
        user: User,
        onSuccess: (Deferred<HttpResponse>) -> Unit,
        onWrongInputData: (Exception) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        user.createPayClaimsBodyJson(
            onSuccess = {s: String ->
                onSuccess.invoke(
                    runBlocking {
                        async {
                            val response = client.post(baseUrl) {
                                header(gatewayId, secretKey)
                                header("Idempotence-Key", "Jflk25785ss54s54s5g5f5s6s8798d13dXXXX")
                                contentType(ContentType.Application.Json)
                                setBody(s)
                            }
                            response
                        }
                    }
                )
            },
            onWrongInputData = {exception ->
                onWrongInputData.invoke(exception)
            },
            onFailure = {exception ->
                onFailure.invoke(exception)
            }
        )
    }

    override suspend fun getPayoutStatus(payoutId: String): HttpResponse {
        val response = client.get("${baseUrl}/$payoutId") {
            header(gatewayId, secretKey)
        }
        return response
    }
}