package com.myapp.lexicon.network

import com.myapp.lexicon.models.User
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Deferred

interface INetClient {

    fun setSecretKey(value: String)
    suspend fun sendPayoutRequest(
        user: User,
        onTimeout: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ): Deferred<Result<HttpResponse>>
    suspend fun getPayoutStatus(
        payoutId: String,
        onTimeout: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ): Deferred<Result<HttpResponse>>
}