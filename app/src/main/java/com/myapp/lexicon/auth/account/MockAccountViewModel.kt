package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.di.INetRepositoryModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MockAccountViewModel(netModule: INetRepositoryModule): AccountViewModel(netModule) {

    companion object {
        const val testMessage = "This is test message for user"
    }

    override val explainMessage: String
        get() = testMessage

    override val state: LiveData<State>
        get() = super.state

    override val bankList: LiveData<Result<List<String>>>
        get() = super.bankList

    override fun fetchBankListFromNet() {
        viewModelScope.launch {
            delay(1000)
            _bankList.value = Result.success(listOf(
                "Сбербанк",
                "Газпромбанк",
                "Альфа банк"
            ))
        }
    }

    override fun getBankListFromCloud() {
        viewModelScope.launch {
            delay(1000)
            _bankList.value = Result.success(listOf(
                "Сбербанк",
                "Газпромбанк",
                "Альфа банк"
            ))
        }
    }

    init {
        this.getBankListFromCloud()
    }
}