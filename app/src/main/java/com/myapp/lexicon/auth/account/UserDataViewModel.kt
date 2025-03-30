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
import com.myapp.lexicon.models.HttpThrowable
import com.myapp.lexicon.models.RevenueX
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.UserX
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.repository.network.INetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

open class UserDataViewModel(netModule: INetRepositoryModule) : AccountViewModel(netModule) {

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
        data class RevenueUpdated(val bonus: Double, val user: UserX): UserDataState
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

        viewModelScope.launch(context = Dispatchers.IO) {
            val result = repository.getUserProfile(accessToken = token).await()
            result.onSuccess { user: UserX ->
                super._loadingState.postValue(LoadingState.Complete)
                this@UserDataViewModel.user = user
                _userState.postValue(UserDataState.ReceivedUserData(user))
            }
            result.onFailure { exception ->
                super._loadingState.postValue(LoadingState.Complete)
                val errorCode = exception.castToHttpThrowable().errorCode
                when(errorCode) {
                    401, 406 -> {
                        _userState.postValue(UserDataState.AuthorizationRequired)
                    }
                    else -> {
                        _userState.postValue(UserDataState.Error(exception.message?: "Unknown error"))
                    }
                }
                _userState.postValue(UserDataState.Error(exception.message?: "Unknown error"))
            }
        }
    }

    fun updateUserData(token: String, data: UserX, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        super._loadingState.value = LoadingState.Start

        viewModelScope.launch(context = dispatcher) {
            val jsonString = data.toJsonString()
            repository.updateUserData(token, jsonString).collect(
                collector = { result ->
                    result.onSuccess { user ->
                        super._loadingState.postValue(LoadingState.Complete)
                        this@UserDataViewModel.user = user
                        _userState.postValue(UserDataState.UserDataUpdated(user))
                    }
                    result.onFailure { ex ->
                        super._loadingState.postValue(LoadingState.Complete)
                        val errorCode = (ex as HttpThrowable).errorCode
                        when(errorCode) {
                            401, 406 -> {
                                _userState.postValue(UserDataState.AuthorizationRequired)
                            }
                            else -> {
                                _userState.postValue(UserDataState.Error(ex.message?: "Unknown error"))
                            }
                        }
                        _userState.postValue(UserDataState.Error(ex.message?: "Unknown error"))
                    }
                }
            )
        }
    }

    fun updateUserData(token: String, data: Map<String, Any?>) {
        super._loadingState.value = LoadingState.Start

        viewModelScope.launch(context = Dispatchers.IO) {
            val jsonString = jsonCoder.encodeToString(data)
            repository.updateUserData(token, jsonString).collect(
                collector = { result ->
                    result.onSuccess { user ->
                        super._loadingState.postValue(LoadingState.Complete)
                        this@UserDataViewModel.user = user
                        _userState.postValue(UserDataState.UserDataUpdated(user))
                    }
                    result.onFailure { ex ->
                        super._loadingState.postValue(LoadingState.Complete)
                        val errorCode = (ex as HttpThrowable).errorCode
                        when(errorCode) {
                            401, 406 -> {
                                _userState.postValue(UserDataState.AuthorizationRequired)
                            }
                            else -> {
                                _userState.postValue(UserDataState.Error(ex.message?: "Unknown error"))
                            }
                        }
                        _userState.postValue(UserDataState.Error(ex.message?: "Unknown error"))
                    }
                }
            )
        }
    }

    fun updateUserBalance(token: String, data: RevenueX) {
        super._loadingState.value = LoadingState.Start

        viewModelScope.launch(context = Dispatchers.IO) {
            repository.updateUserBalance(token, data).collect(
                collector = { result ->
                    result.onSuccess { user: UserX ->
                        super._loadingState.postValue(LoadingState.Complete)
                        _userState.postValue(UserDataState.RevenueUpdated(
                            bonus = (data.revenueRub * user.rewardRatio).to2DigitsScale(),
                            user = user
                        ))
                    }
                    result.onFailure { ex ->
                        super._loadingState.postValue(LoadingState.Complete)
                        val errorCode = (ex as HttpThrowable).errorCode
                        when(errorCode) {
                            401, 406 -> {
                                _userState.postValue(UserDataState.AuthorizationRequired)
                            }
                            else -> {
                                _userState.postValue(UserDataState.Error(ex.message?: "Unknown error"))
                            }
                        }
                        _userState.postValue(UserDataState.Error(ex.message?: "Unknown error"))
                    }
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