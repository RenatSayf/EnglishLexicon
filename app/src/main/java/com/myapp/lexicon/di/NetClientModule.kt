package com.myapp.lexicon.di

import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.models.DataSource
import com.myapp.lexicon.network.INetClient
import com.myapp.lexicon.network.MockNetClient
import com.myapp.lexicon.network.NetClient

object NetClientModule {

    fun provideNetClient(apiKey: String): INetClient {
        return if (BuildConfig.DATA_SOURCE == DataSource.LOCALHOST.name) {
            MockNetClient()
        }
        else {
            NetClient().apply {
                setSecretKey(apiKey)
            }
        }
    }
}