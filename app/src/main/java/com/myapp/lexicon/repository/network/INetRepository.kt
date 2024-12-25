package com.myapp.lexicon.repository.network

import com.myapp.lexicon.models.SignInData
import com.myapp.lexicon.models.SignUpData
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.UserX
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

interface INetRepository {

    suspend fun signUp(data: SignUpData): Flow<Result<Tokens>>

    suspend fun signIn(data: SignInData): Flow<Result<Tokens>>

    suspend fun getUserProfile(accessToken: String): Deferred<Result<UserX>>
}