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
        data class OnValid(
            var phone: Boolean = true,
            var card: Boolean = true,
            var firstName: Boolean = true,
            var lastName: Boolean = true
        ): State()
    }

    private var _state = MutableLiveData<State>().apply {
        value = State.ReadOnly
    }
    val state: LiveData<State> = _state

    fun setState(state: State) {
        _state.value = state
    }

    val url = "https://play.google.com/store/apps/details?id=com.myapp.lexicon"
}