package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.jsonToPaymentObjClass
import com.myapp.lexicon.models.payment.response.PaymentObj
import com.myapp.lexicon.network.INetClient
import com.myapp.lexicon.network.NetClient
import io.ktor.client.call.body
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AccountViewModel(
    private val client: INetClient
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val apiKey: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == AccountViewModel::class.java)
            return AccountViewModel(client = NetClient().apply {
                setSecretKey(apiKey)
            }) as T
        }
    }

    sealed class LoadingState {
        object Start: LoadingState()
        object Complete: LoadingState()
    }

    private var _loadingState = MutableLiveData<LoadingState>(LoadingState.Complete)
    val loadingState: LiveData<LoadingState> = _loadingState

    sealed class State {
        object ReadOnly: State()
        object Editing: State()
        data class OnSave(val user: User): State()
        data class OnValid(
            var phone: Boolean = true,
            var card: Boolean = true,
            var firstName: Boolean = true,
            var lastName: Boolean = true
        ): State()
    }

    private var _state = MutableLiveData<State>(State.ReadOnly)
    val state: LiveData<State> = _state

    fun setState(state: State) {
        _state.value = state
    }

    private val url = "https://play.google.com/store/apps/details?id=com.myapp.lexicon"

    sealed class PayoutState {
        object Init: PayoutState()
        data class Success(val data: PaymentObj): PayoutState()
        object Timeout: PayoutState()
        data class Failure(val ex: Exception): PayoutState()
    }

    private var _payoutState = MutableStateFlow<PayoutState>(PayoutState.Init)
    val payoutState: StateFlow<PayoutState> = _payoutState
    fun sendPayoutRequest(user: User) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Start
            val responseResult = client.sendPayoutRequest(
                user,
                onTimeout = {
                    _loadingState.value = LoadingState.Complete
                    _payoutState.value = PayoutState.Timeout
                },
                onFailure = { exception ->
                    _loadingState.value = LoadingState.Complete
                    _payoutState.value = PayoutState.Failure(exception)
                }
            ).await()
            responseResult.onSuccess { response ->
                val body = response.body<String>()
                body.jsonToPaymentObjClass(
                    onSuccess = {paymentObj ->
                        _loadingState.value = LoadingState.Complete
                        _payoutState.value = PayoutState.Success(paymentObj)
                    },
                    onFailure = {exception ->
                        _loadingState.value = LoadingState.Complete
                        _payoutState.value = PayoutState.Failure(exception)
                    }
                )
            }
        }
    }



}