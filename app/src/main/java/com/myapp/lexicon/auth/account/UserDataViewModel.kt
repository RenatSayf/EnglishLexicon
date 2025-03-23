package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.models.UserX
import com.myapp.lexicon.repository.network.INetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserDataViewModel(netModule: INetRepositoryModule) : AccountViewModel(netModule) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val netModule: INetRepositoryModule = NetRepositoryModule()
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == UserDataViewModel::class.java)
            return UserDataViewModel(netModule) as T
        }
    }

    sealed class UserDataState {
        data object Init: UserDataState()
        data class ReceivedUserData(val user: UserX): UserDataState()
        data object PersonalDataUpdated: UserDataState()
        data class RevenueUpdated(val bonus: Double, val user: UserX): UserDataState()
        data class PaymentRequestSent(val user: UserX, val payout: Int, val remainder: Double): UserDataState()
        data class Error(val message: String): UserDataState()
    }

    private var _userState = MutableLiveData<UserDataState>(UserDataState.Init)
    val userState: LiveData<UserDataState> = _userState

    override val repository: INetRepository
        get() = super.repository

    fun fetchUserData(token: String) {
        super._loadingState.value = LoadingState.Start

        viewModelScope.launch(context = Dispatchers.IO) {
            val result = repository.getUserProfile(accessToken = token).await()
            result.onSuccess { user: UserX ->
                super._loadingState.postValue(LoadingState.Complete)
                _userState.postValue(UserDataState.ReceivedUserData(user))
            }
            result.onFailure { t ->
                super._loadingState.postValue(LoadingState.Complete)
                _userState.postValue(UserDataState.Error(t.message?: "Unknown error"))
            }
        }
    }
}