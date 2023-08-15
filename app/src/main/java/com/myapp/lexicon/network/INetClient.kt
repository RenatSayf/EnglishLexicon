package com.myapp.lexicon.network

import com.myapp.lexicon.models.User
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Deferred

interface INetClient {
    suspend fun sendPayoutRequest(user: User): Deferred<Result<HttpResponse>>
    suspend fun getPayoutStatus(payoutId: String): HttpResponse
}