package com.myapp.lexicon.di

import com.google.common.net.HttpHeaders
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.helpers.printLogIfDebug
import com.myapp.lexicon.repository.network.INetRepository
import com.myapp.lexicon.repository.network.NetRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

object NetRepositoryModule {

    fun provideNetRepository(): INetRepository {

        val httpClient = HttpClient(CIO, block = {
            install(Logging, configure = {
                logger = object : Logger {
                    override fun log(message: String) {
                        printLogIfDebug("******** HTTP call: $message *****************")
                    }
                }
                level = LogLevel.ALL
                if (!BuildConfig.DEBUG) {
                    sanitizeHeader { header: String ->
                        header == HttpHeaders.AUTHORIZATION
                    }
                }
            })
            engine {
                maxConnectionsCount = 1000
                endpoint {
                    connectTimeout = 20000
                    keepAliveTime = 20000
                    connectAttempts = 5
                }
            }
        })
        return NetRepository(httpClient)
    }
}