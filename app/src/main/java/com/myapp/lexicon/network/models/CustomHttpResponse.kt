package com.myapp.lexicon.network.models

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.InternalAPI
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class CustomHttpResponse(
    private val jsonContent: String = "",
    private val statusCode: HttpStatusCode = HttpStatusCode.OK,
    private val description: String = ""
): HttpResponse() {
    override val call: HttpClientCall
        get() = HttpClientCall(HttpClient())

    @InternalAPI
    override val content: ByteReadChannel
        get()  {
            return ByteReadChannel(jsonContent.toByteArray())
        }
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext
    override val headers: Headers
        get() = Headers.Empty
    override val requestTime: GMTDate
        get() = GMTDate(System.currentTimeMillis())
    override val responseTime: GMTDate
        get() = GMTDate(System.currentTimeMillis())
    override val status: HttpStatusCode
        get() = HttpStatusCode(statusCode.value, description)
    override val version: HttpProtocolVersion
        get() = HttpProtocolVersion(name = "HTTPS", major = 1, minor = 1)
}