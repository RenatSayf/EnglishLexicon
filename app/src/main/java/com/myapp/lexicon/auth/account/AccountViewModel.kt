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
        data class OnNotValid(
            val phone: Boolean = true,
            val card: Boolean = true,
            val firstName: Boolean = true,
            val lastName: Boolean = true
        ): State()
        data class OnValid(
            val phone: Boolean = false,
            val card: Boolean = false,
            val firstName: Boolean = false,
            val lastName: Boolean = false
        ): State()
    }

    private var _state = MutableLiveData<State>().apply {
        value = State.ReadOnly
    }
    val state: LiveData<State> = _state

    fun setState(state: State) {
        _state.value = state
    }
}