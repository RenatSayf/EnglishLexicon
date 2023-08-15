package com.myapp.lexicon.network

import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.jsonToPaymentObjClass
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NetClientTest {

    private lateinit var mockEngine: HttpClientEngine

    private lateinit var client: NetClient

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {
    }

    @Test
    fun sendPayoutRequest() {
        runBlocking {
            mockEngine = MockEngine {
                respond(
                    content = """{
    "id": "SSSSSSSSSSSSSSS",
    "amount": {
        "value": "100.00",
        "currency": "RUB"
    },
    "status": "pending",
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
                    status = HttpStatusCode.OK,
                    headers = headersOf()
                )
            }
            client = NetClient(engine = mockEngine, secretKey = "QQQQ")
            val user = User("SSSSSSSSSSSSSSS").apply {
                reservedPayment = 100
                currency = "RUB"
                bankCard = "1111"
                firstName = "Xxxx"
                lastName = "Yyyy"
            }

            val responseResult = client.sendPayoutRequest(user).await()
            responseResult.onSuccess { httpResponse ->
                val bodyString = httpResponse.body<String>()
                bodyString.jsonToPaymentObjClass(
                    onSuccess = {paymentObj ->
                        val actualId = paymentObj.id
                        Assert.assertEquals("SSSSSSSSSSSSSSS", actualId)
                    },
                    onFailure = {exception ->
                        Assert.assertTrue(exception.message, false)
                    }
                )
            }
            responseResult.onFailure { exception ->
                Assert.assertTrue(exception.message, false)
            }
        }
    }

    @Test
    fun getPayoutStatus() {
    }
}