package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MockAccountViewModel: AccountViewModel() {

    companion object {
        const val testMessage = "This is test message for user"
    }

    override val explainMessage: String
        get() = testMessage

    override val state: LiveData<State>
        get() = super.state

    override fun setState(state: State) {
        super.setState(state)
    }

    override val bankList: LiveData<Result<List<String>>>
        get() = super.bankList

    override fun fetchBankList() {
        viewModelScope.launch {
            delay(1000)
            _bankList.value = Result.success(listOf(
                "Сбербанк",
                "Газпромбанк",
                "Альфа банк"
            ))
        }
    }
}