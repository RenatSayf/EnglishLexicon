package com.myapp.lexicon.network

import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.payment.request.PayClaims
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Deferred

interface INetClient {
    //suspend fun sendPayoutRequest(user: User): Deferred<HttpResponse>
    suspend fun getPayoutStatus(payoutId: String): HttpResponse
}