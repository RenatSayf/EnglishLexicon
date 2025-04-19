package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.common.DynamicLookupSerializer
import com.myapp.lexicon.common.NonEmptyStringSerializer
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.castToHttpThrowable
import com.myapp.lexicon.models.AdsReward
import com.myapp.lexicon.models.HttpThrowable
import com.myapp.lexicon.models.RevenueX
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.UserX
import com.myapp.lexicon.repository.network.INetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.coroutines.CoroutineContext

open class UserDataViewModel(
    netModule: INetRepositoryModule,
    private val coroutineContext: CoroutineContext = Dispatchers.Main
) : AccountViewModel(netModule) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val netModule: INetRepositoryModule = NetRepositoryModule()
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == UserDataViewModel::class.java)
            return UserDataViewModel(netModule) as T
        }
    }

    sealed interface UserDataState {
        data object Init: UserDataState
        data class ReceivedUserData(val user: UserX): UserDataState
        data class UserDataUpdated(val userX: UserX): UserDataState
        data class RevenueUpdated(val reward: AdsReward): UserDataState
        data class PaymentRequestSent(val user: UserX, val payout: Int, val remainder: Double): UserDataState
        data class TokensUpdated(val tokens: Tokens): UserDataState
        data object AuthorizationRequired: UserDataState
        data class Error(val message: String): UserDataState
    }

    private val jsonCoder = Json {
        prettyPrint = true
        serializersModule = SerializersModule {
            contextual(Any::class, DynamicLookupSerializer)
            contextual(String::class, NonEmptyStringSerializer)
        }
        encodeDefaults = false
        explicitNulls = false
    }

    private var _userState = MutableLiveData<UserDataState>(UserDataState.Init)
    val userState: LiveData<UserDataState> = _userState

    fun setUserState(state: UserDataState) {
        _userState.value = state
    }

    open var user: UserX? = null

    override val repository: INetRepository
        get() = super.repository

    override fun setRefreshToken(token: String) {
        super.setRefreshToken(token)
    }

    override fun setLoadingState(state: LoadingState) {
        super.setLoadingState(state)
    }

    fun fetchUserData(token: String) {
        super._loadingState.value = LoadingState.Start

        viewModelScope.launch(context = this.coroutineContext) {

            val result = repository.getUserProfile(accessToken = token).await()
            result.onSuccess { user: UserX ->
                this@UserDataViewModel.user = user
                _userState.value = UserDataState.ReceivedUserData(user)
            }
            result.onFailure { exception ->
                val errorCode = exception.castToHttpThrowable().errorCode
                when(errorCode) {
                    401, 406 -> {
                        _userState.value = UserDataState.AuthorizationRequired
                    }
                    else -> {
                        _userState.value = UserDataState.Error(exception.message?: "Unknown error")
                    }
                }
                _userState.value = UserDataState.Error(exception.message?: "Unknown error")
            }
            super._loadingState.value = LoadingState.Complete
        }
    }

    fun updateUserData(
        token: String,
        data: UserX
    ) {
        super._loadingState.value = LoadingState.Start

        viewModelScope.launch(context = this.coroutineContext) {
            val jsonString = data.toJsonString()
            repository.updateUserData(token, jsonString).collect(
                collector = { result ->
                    result.onSuccess { user ->
                        this@UserDataViewModel.user = user
                        _userState.value = UserDataState.UserDataUpdated(user)
                    }
                    result.onFailure { ex ->
                        val errorCode = (ex as HttpThrowable).errorCode
                        when(errorCode) {
                            401, 406 -> {
                                _userState.value = UserDataState.AuthorizationRequired
                            }
                            else -> {
                                _userState.value = UserDataState.Error(ex.message?: "Unknown error")
                            }
                        }
                        _userState.value = UserDataState.Error(ex.message?: "Unknown error")
                    }
                    super._loadingState.value = LoadingState.Complete
                }
            )
        }
    }

    fun updateUserData(
        token: String,
        data: Map<String, Any?>
    ) {
        super._loadingState.value = LoadingState.Start

        viewModelScope.launch(context = this.coroutineContext) {
            val jsonString = jsonCoder.encodeToString(data)
            repository.updateUserData(token, jsonString).collect(
                collector = { result ->
                    result.onSuccess { user ->
                        this@UserDataViewModel.user = user
                        _userState.value = UserDataState.UserDataUpdated(user)
                    }
                    result.onFailure { ex ->
                        super._loadingState.postValue(LoadingState.Complete)
                        val errorCode = (ex as HttpThrowable).errorCode
                        when(errorCode) {
                            401, 406 -> {
                                _userState.value = UserDataState.AuthorizationRequired
                            }
                            else -> {
                                _userState.value = UserDataState.Error(ex.message?: "Unknown error")
                            }
                        }
                        _userState.value = UserDataState.Error(ex.message?: "Unknown error")
                    }
                    super._loadingState.value = LoadingState.Complete
                }
            )
        }
    }

    fun updateUserBalance(token: String, data: RevenueX) {
        super._loadingState.value = LoadingState.Start

        viewModelScope.launch(context = this.coroutineContext) {
            repository.updateUserBalance(token, data).collect(
                collector = { result ->
                    result.onSuccess { reward: AdsReward ->
                        _userState.value = UserDataState.RevenueUpdated(reward = reward)
                    }
                    result.onFailure { ex ->
                        super._loadingState.value = LoadingState.Complete
                        val errorCode = (ex as HttpThrowable).errorCode
                        when(errorCode) {
                            401, 406 -> {
                                _userState.value = UserDataState.AuthorizationRequired
                            }
                            else -> {
                                _userState.value = UserDataState.Error(ex.message?: "Unknown error")
                            }
                        }
                        _userState.value = UserDataState.Error(ex.message?: "Unknown error")
                    }
                    super._loadingState.value = LoadingState.Complete
                }
            )
        }
    }

    init {
        netModule.setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {
                _userState.postValue(UserDataState.TokensUpdated(tokens))
            }

            override fun onAuthorizationRequired() {
                _userState.postValue(UserDataState.AuthorizationRequired)
            }
        })
    }



}