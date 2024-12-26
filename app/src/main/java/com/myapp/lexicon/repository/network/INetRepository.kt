package com.myapp.lexicon.repository.network

import com.myapp.lexicon.models.Balance
import com.myapp.lexicon.models.RevenueX
import com.myapp.lexicon.models.SignInData
import com.myapp.lexicon.models.SignUpData
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.UserProfile
import com.myapp.lexicon.models.UserX
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

interface INetRepository {

    suspend fun signUp(data: SignUpData): Flow<Result<Tokens>>

    suspend fun signIn(data: SignInData): Flow<Result<Tokens>>

    suspend fun signOut(accessToken: String): Flow<Result<Tokens>>

    suspend fun getUserProfile(accessToken: String): Deferred<Result<UserX>>

    suspend fun updateUserBalance(accessToken: String, revenue: RevenueX): Flow<Result<Balance>>

    suspend fun updateUserProfile(accessToken: String, profile: UserProfile): Flow<Result<UserProfile>>

    suspend fun reservedPaymentToUser(accessToken: String, sum: Int): Flow<Result<Balance>>
}