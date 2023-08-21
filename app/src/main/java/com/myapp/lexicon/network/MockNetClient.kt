package com.myapp.lexicon.network

import com.myapp.lexicon.models.User
import com.myapp.lexicon.network.models.CustomHttpResponse
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MockNetClient: INetClient {

    private val statusCode = HttpStatusCode.OK
    override fun setSecretKey(value: String) {}

    override suspend fun sendPayoutRequest(
        user: User,
        onTimeout: () -> Unit,
        onFailure: (Exception) -> Unit
    ): Deferred<Result<HttpResponse>> {

        val result = when (statusCode) {
            HttpStatusCode.OK -> {
                val response = CustomHttpResponse(
                    jsonContent = """{
    "id": "SSSSSSSSSSSSSSS",
    "amount": {
        "value": "100.00",
        "currency": "RUB"
    },
    "status": "succeeded",
    "payout_destination": {
        "type": "yoo_money",
        "account_number": "1111"
    },
    "description": "Выплата по заказу Xxxx Yyyy",
    "created_at": "21.06.2021T14:28:45.132Z",
    "metadata": {
        "order_id": "37"
    },
    "test": "false"
}""",
                    statusCode = HttpStatusCode.OK
                )
                Result.success(response)
            }

            HttpStatusCode.ServiceUnavailable -> {
                val response = CustomHttpResponse(
                    jsonContent = "",
                    statusCode = HttpStatusCode.ServiceUnavailable,
                    description = "Service Unavailable"
                )
                Result.failure<Exception>(Exception(response.status.description))
            }

            HttpStatusCode.GatewayTimeout -> {
                val response = CustomHttpResponse(
                    jsonContent = "",
                    statusCode = HttpStatusCode.GatewayTimeout,
                    description = "Gateway Timeout"
                )
                Result.failure<Exception>(Exception(response.status.description))
            }

            else -> {
                val response = CustomHttpResponse(
                    jsonContent = "",
                    statusCode = HttpStatusCode.BadRequest,
                    description = "Bad Request"
                )
                Result.failure<Exception>(Exception(response.status.description))
            }
        }
        return coroutineScope {
            async {
                result as Result<HttpResponse>
            }
        }
    }

    override suspend fun getPayoutStatus(
        payoutId: String,
        onTimeout: () -> Unit,
        onFailure: (Exception) -> Unit
    ): Deferred<Result<HttpResponse>> {
        TODO("Not yet implemented")
    }


}