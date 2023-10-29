package com.myapp.lexicon.main.viewmodels

import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MockUserViewModel @Inject constructor(): UserViewModel() {

    companion object {
        var testData: User? = null
    }

    override fun getUserFromCloud() {

        _loadingState.value = LoadingState.Start
        viewModelScope.launch {
            delay(3000)
            if (testData is User) {
                _user.value = testData
                _state.value = State.ReceivedUserData(_user.value!!)
                _stateFlow.value = State.ReceivedUserData(_user.value!!)
            }
            else {
                val errorState = _state.value as State.Error
                _state.value = State.Error("********* ${errorState.message} **********")
                _stateFlow.value = State.Error("********* ${errorState.message} **********")
            }
            _loadingState.value = LoadingState.Complete
        }
    }
}