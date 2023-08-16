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
    private val user = User("SSSSSSSSSSSSSSS").apply {
        reservedPayment = 100
        currency = "RUB"
        bankCard = "1111"
        firstName = "Xxxx"
        lastName = "Yyyy"
    }
    private val response = """{
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
}"""

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {
    }

    @Test
    fun sendPayoutRequest_success() {
        runBlocking {
            mockEngine = MockEngine {
                respond(
                    content = response,
                    status = HttpStatusCode.OK,
                    headers = headersOf()
                )
            }
            client = NetClient(engine = mockEngine).apply {
                setSecretKey("QQQQQQQQQQ")
            }

            val responseResult = client.sendPayoutRequest(user).await()
            responseResult.onSuccess { httpResponse ->
                val bodyString = httpResponse.body<String>()
                bodyString.jsonToPaymentObjClass(
                    onSuccess = {paymentObj ->
                        val actualStatus = paymentObj.status
                        Assert.assertEquals("pending", actualStatus)
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
    fun sendPayoutRequest_negative_amount() {
        runBlocking {
            mockEngine = MockEngine {
                respond(
                    content = response,
                    status = HttpStatusCode.OK
                )
            }
            client = NetClient(engine = mockEngine).apply {
                setSecretKey("QQQQQQQQQQ")
            }

            val responseResult = client.sendPayoutRequest(
                user.apply {
                    reservedPayment = -100
                }
            ).await()
            responseResult.onSuccess {
                Assert.assertTrue(false)
            }
            responseResult.onFailure { exception ->
                Assert.assertEquals(User.WRONG_AMOUNT, exception.message)
            }
        }
    }

    @Test
    fun sendPayoutRequest_empty_currency() {
        runBlocking {
            mockEngine = MockEngine {
                respond(
                    content = response,
                    status = HttpStatusCode.OK
                )
            }
            client = NetClient(engine = mockEngine).apply {
                setSecretKey("QQQQQQQQQQ")
            }

            val responseResult = client.sendPayoutRequest(user.apply {
                currency = ""
            }).await()
            responseResult.onSuccess {
                Assert.assertTrue(false)
            }
            responseResult.onFailure { exception ->
                Assert.assertEquals(User.WRONG_CURRENCY, exception.message)
            }
        }
    }

    @Test
    fun sendPayoutRequest_empty_wallet_number() {
        runBlocking {
            mockEngine = MockEngine {
                respond(
                    content = response,
                    status = HttpStatusCode.OK
                )
            }
            client = NetClient(engine = mockEngine).apply {
                setSecretKey("QQQQQQQQQQ")
            }

            val responseResult = client.sendPayoutRequest(user.apply {
                bankCard = ""
            }).await()
            responseResult.onSuccess {
                Assert.assertTrue(false)
            }
            responseResult.onFailure { exception ->
                Assert.assertEquals(User.WRONG_WALLET_NUMBER, exception.message)
            }
        }
    }

    @Test
    fun sendPayoutRequest_gate_way_timeout() {
        runBlocking {
            mockEngine = MockEngine {
                respond(
                    content = response,
                    status = HttpStatusCode.GatewayTimeout
                )
            }
            client = NetClient(engine = mockEngine).apply {
                setSecretKey("QQQQQQQQQQ")
            }

            val responseResult = client.sendPayoutRequest(user, onTimeout = {
                Assert.assertTrue(true)
            }).await()
            responseResult.onSuccess {
                Assert.assertTrue(false)
            }
            responseResult.onFailure { exception ->
                Assert.assertEquals(HttpStatusCode.GatewayTimeout.description, exception.message)
            }
        }
    }
    @Test
    fun getPayoutStatus_Ok200() {
        runBlocking {
            mockEngine = MockEngine {
                respond(
                    content = response,
                    status = HttpStatusCode.OK
                )
            }
            client = NetClient(engine = mockEngine).apply {
                setSecretKey("QQQQQQQQQQ")
            }
            val responseResult = client.getPayoutStatus("37").await()
            responseResult.onSuccess { response ->
                val body = response.body<String>()
                body.jsonToPaymentObjClass(
                    onSuccess = { paymentObj ->
                        val actualStatus = paymentObj.status
                        Assert.assertEquals("pending", actualStatus)
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
    fun getPayoutStatus_gateway_timeout() {
        runBlocking {
            mockEngine = MockEngine {
                respond(
                    content = response,
                    status = HttpStatusCode.GatewayTimeout
                )
            }
            client = NetClient(engine = mockEngine).apply {
                setSecretKey("QQQQQQQQQQ")
            }
            val responseResult = client.getPayoutStatus("37", onTimeout = {
                Assert.assertTrue(true)
            }).await()
            responseResult.onSuccess {
                Assert.assertTrue(false)
            }
            responseResult.onFailure { exception ->
                Assert.assertEquals(HttpStatusCode.GatewayTimeout.description, exception.message)
            }
        }
    }
}