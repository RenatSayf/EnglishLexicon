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

    override fun fetchBankList(): LiveData<Result<List<String>>> {
        val result = MutableLiveData<Result<List<String>>>()
        viewModelScope.launch {
            delay(1000)
            result.value = Result.success(listOf(
                "Сбербанк",
                "Газпромбанк",
                "Альфа банк"
            ))
        }
        return result
    }
}