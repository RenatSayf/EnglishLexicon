package com.myapp.lexicon.main.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class MockUserViewModel @Inject constructor(
    app: Application
): UserViewModel(app) {

    companion object {
        var testData: User? = null
    }

    override fun getUserFromCloud(): LiveData<Result<User>> {

        _loadingState.value = LoadingState.Start
        val result = MutableLiveData<Result<User>>()
        viewModelScope.launch {
            delay(3000)
            if (testData is User) {
                _user.value = testData
                _state.value = State.ReceivedUserData(_user.value!!)
                result.value = Result.success(testData!!)
            }
            else {
                val errorState = _state.value as State.Error
                _state.value = State.Error("********* ${errorState.message} **********")
                result.value = Result.failure(Throwable(errorState.message))
            }
            _loadingState.value = LoadingState.Complete
        }
        return result
    }
}