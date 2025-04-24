package com.myapp.lexicon.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.getASCIISum
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.models.AppConfig
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.settings.currentConfig
import com.myapp.lexicon.settings.refreshToken
import com.myapp.lexicon.settings.remoteConfigJsonFromPref
import com.myapp.lexicon.settings.saveAsRemoteConfigToPref
import com.myapp.lexicon.settings.saveAuthTokens
import kotlinx.serialization.json.Json


class RemoteConfigWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val localCheckSum = context.remoteConfigJsonFromPref.getASCIISum()

            val result = fetchRemoteConfig(checkSum = localCheckSum)
            if (result.isSuccess) {
                val configStr = result.getOrNull()
                if (configStr != null) {
                    context.saveAsRemoteConfigToPref(configStr)
                }
                val newConfig = decodeFromString(context.remoteConfigJsonFromPref)
                if (newConfig != null) {
                    context.currentConfig = newConfig
                }
                Result.success()
            }
            else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    @Suppress("RemoveRedundantQualifierName")
    private suspend fun fetchRemoteConfig(checkSum: Int): kotlin.Result<String?> {

        val repository = NetRepositoryModule().apply {
            this.setRefreshToken(context.refreshToken)
            this.setTokensUpdateListener(object : INetRepositoryModule.Listener {
                override fun onUpdateTokens(tokens: Tokens) {
                    context.saveAuthTokens(tokens)
                }
                override fun onAuthorizationRequired() {}
            })
        }.provideNetRepository()

        val result = repository.fetchRemoteConfig(checkSum).await()
        return result
    }

    private fun decodeFromString(json: String): AppConfig? {
        return try {
            val decoder = Json(builderAction = {
                ignoreUnknownKeys = true
            })
            val config = decoder.decodeFromString(AppConfig.serializer(), json)
            config
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            null
        }
    }
}

fun Context.scheduleRemoteConfigRequest() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val workRequest = OneTimeWorkRequestBuilder<RemoteConfigWorker>()
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(this).enqueue(workRequest)
}