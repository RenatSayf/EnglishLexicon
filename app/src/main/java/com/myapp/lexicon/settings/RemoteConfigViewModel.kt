package com.myapp.lexicon.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.castToHttpThrowable
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.models.AppConfig
import com.myapp.lexicon.models.HttpThrowable
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.repository.network.INetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext


class RemoteConfigViewModel(
    private val netModule: INetRepositoryModule,
    private val coroutineContext: CoroutineContext = Dispatchers.Main
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val netModule: INetRepositoryModule = NetRepositoryModule()
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == RemoteConfigViewModel::class.java)
            return RemoteConfigViewModel(netModule) as T
        }
    }

    sealed interface State {
        data class RemoteConfigLoaded(val config: String): State
        data object NoDifferences: State
        data class FailureLoad(val throwable: HttpThrowable): State
    }

    private var _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val repository: INetRepository = netModule.apply {
        setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {
                netModule.setRefreshToken(tokens.refreshToken)
            }

            override fun onAuthorizationRequired() {

            }
        })
    }.provideNetRepository()

    private val decoder = Json(builderAction = {
        ignoreUnknownKeys = true
    })

    fun fetchRemoteConfig(
        checkSum: Long,
        onSuccess: (config: String) -> Unit,
        noDifferences: () -> Unit,
        onFailure: (t: Throwable) -> Unit
    ) {

        viewModelScope.launch(context = this.coroutineContext) {
            repository.fetchRemoteConfig(checkSum).collect(collector = { res ->
                res.onSuccess { configStr: String? ->
                    if (configStr != null) {
                        _state.value = State.RemoteConfigLoaded(configStr)
                        onSuccess.invoke(configStr)
                    } else {
                        _state.value = State.NoDifferences
                        noDifferences.invoke()
                    }
                }
                res.onFailure { t ->
                    _state.value = State.FailureLoad(t.castToHttpThrowable())
                    onFailure.invoke(t)
                }
            })
        }
    }

    fun encodeToJsonString(config: AppConfig): String? {
        return try {
            val string = decoder.encodeToString(AppConfig.serializer(), config)
            string
        } catch (e: SerializationException) {
            e.printStackTraceIfDebug()
            null
        }
    }

    fun decodeFromString(json: String): AppConfig? {
        return try {
            val config = decoder.decodeFromString(AppConfig.serializer(), json)
            config
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            null
        }
    }


}




