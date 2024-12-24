package com.myapp.lexicon.repository.network

import com.myapp.lexicon.common.USER_AGENT
import kotlinx.coroutines.Deferred
import java.io.File

interface INetRepository {
    suspend fun getUserProfile(accessToken: String): Deferred<Result<String>>
}