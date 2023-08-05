package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.models.User

class AccountViewModel : ViewModel() {

    sealed class State {
        object ReadOnly: State()
        object Editing: State()
        data class OnSave(val user: User): State()
        data class OnNotValid(val user: User): State()
    }

    private var _state = MutableLiveData<State>().apply {
        value = State.ReadOnly
    }
    val state: LiveData<State> = _state

    fun setState(state: State) {
        _state.value = state
    }
}