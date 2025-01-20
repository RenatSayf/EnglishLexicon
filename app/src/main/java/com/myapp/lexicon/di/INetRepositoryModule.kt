package com.myapp.lexicon.di

import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.repository.network.INetRepository

interface INetRepositoryModule {

    interface Listener {
        fun onUpdateTokens(tokens: Tokens)
        fun onAuthorizationRequired()
    }

    fun setRefreshToken(token: String)

    fun setTokensUpdateListener(listener: Listener)

    fun provideNetRepository(): INetRepository
}