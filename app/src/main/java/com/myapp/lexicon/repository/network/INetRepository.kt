package com.myapp.lexicon.repository.network

import com.myapp.lexicon.models.UserX
import kotlinx.coroutines.Deferred

interface INetRepository {
    suspend fun getUserProfile(accessToken: String): Deferred<Result<UserX>>
}