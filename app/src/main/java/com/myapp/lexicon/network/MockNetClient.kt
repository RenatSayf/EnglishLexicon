package com.myapp.lexicon.network

import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.jsonToPaymentObjClass
import com.myapp.lexicon.models.payment.response.PaymentObj
import com.myapp.lexicon.network.models.CustomHttpResponse
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MockNetClient: INetClient {

    private val statusCode = HttpStatusCode.OK

    private val jsonBodySuccess = """{
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
}"""

    override fun setSecretKey(value: String) {}

    override suspend fun sendPayoutRequest(
        user: User,
        onTimeout: () -> Unit,
        onFailure: (Exception) -> Unit
    ): Deferred<Result<PaymentObj?>> {
        return coroutineScope {
            async {
                when (statusCode) {
                    HttpStatusCode.OK -> {
                        val paymentObj = jsonBodySuccess.jsonToPaymentObjClass()
                        if (paymentObj != null) {
                            Result.success(paymentObj)
                        } else Result.failure(Exception("paymentObj is null"))
                    }
                    else -> {
                        Result.failure(Exception(""))
                    }
                }
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