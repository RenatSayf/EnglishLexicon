package com.myapp.lexicon.di

import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.repository.network.INetRepository

interface INetRepositoryModule {

    interface Listener {
        fun onUpdateTokens(tokens: Tokens)
    }

    fun setRefreshToken(token: String)

    fun setTokensUpdateListener(listener: Listener)

    fun provideNetRepository(): INetRepository
}