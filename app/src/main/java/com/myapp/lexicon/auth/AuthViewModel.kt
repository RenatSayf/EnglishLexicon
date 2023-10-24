@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.auth

import android.app.Application
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.settings.getAuthDataFromPref
import com.parse.DeleteCallback
import com.parse.LogInCallback
import com.parse.ParseException
import com.parse.ParseUser
import com.parse.RequestPasswordResetCallback
import com.parse.SignUpCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {
    sealed class LoadingState {
        object Start: LoadingState()
        object Complete: LoadingState()
    }

    private var _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState

    fun setLoadingState(state: LoadingState) {
        _loadingState.value = state
    }

    private var _state = MutableLiveData<UserState>().apply {
        value = UserState.Init
    }
    val state: LiveData<UserState> = _state

    private val _stateFlow = MutableStateFlow<UserState>(UserState.Init)
    val stateFlow: StateFlow<UserState> = _stateFlow

    fun setState(state: UserState) {
        _state.value = state
        _stateFlow.value = state
    }

    fun registerWithEmailAndPassword(email: String, password: String) {

        _loadingState.value = LoadingState.Start
        val parseUser = ParseUser()
        parseUser.apply {
            this.username = email
            this.email = email
            setPassword(password)
        }.signUpInBackground(object : SignUpCallback {
            override fun done(e: ParseException?) {
                if (e == null) {
                    signInWithEmailAndPassword(email, password)
                }
                else {
                    ParseUser.logOut()
                    when (e.code) {
                        ParseException.EMAIL_TAKEN, ParseException.USERNAME_TAKEN -> {
                            _state.value = UserState.AlreadyExists
                            _stateFlow.value = UserState.AlreadyExists
                        }
                        else -> {
                            _state.value = UserState.Failure(e)
                            _stateFlow.value = UserState.Failure(e)
                        }
                    }
                }
                _loadingState.value = LoadingState.Complete
            }
        })
    }

    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun signInWithEmailAndPassword(email: String, password: String) {

        _loadingState.value = LoadingState.Start
        ParseUser.logInInBackground(
            email,
            password,
            object : LogInCallback {
                override fun done(user: ParseUser?, e: ParseException?) {
                    if (user is ParseUser) {
                        val newState = UserState.SignIn(User(user.objectId).apply {
                            this.email = email
                            this.password = password
                            ///this.sessionToken = user.sessionToken
                        })
                        _state.value = newState
                        _stateFlow.value = newState
                    }
                    else {
                        ParseUser.logOut()
                        if (e is ParseException && e.code == ParseException.OBJECT_NOT_FOUND) {
                            _state.value = UserState.NotRegistered
                            _stateFlow.value = UserState.NotRegistered
                        }
                        else {
                            _state.value = UserState.Failure(e?: Exception("Unknown error"))
                            _stateFlow.value = UserState.Failure(e?: Exception("Unknown error"))
                        }
                    }
                    _loadingState.value = LoadingState.Complete
                }
            }
        )
    }

    fun resetPassword(email: String) {

        _loadingState.value = LoadingState.Start

        ParseUser.requestPasswordResetInBackground(email, object : RequestPasswordResetCallback {
            override fun done(e: ParseException?) {
                if (e == null) {
                    _state.value = UserState.PasswordReset
                    _stateFlow.value = UserState.PasswordReset
                }
                else {
                    _state.value = UserState.Failure(e)
                    _stateFlow.value = UserState.Failure(e)
                }
                _loadingState.value = LoadingState.Complete
            }
        })
    }

    fun deleteAccount(
        onStart: () -> Unit = {},
        onSuccess: () -> Unit,
        onComplete: (Exception?) -> Unit = {}
    ) {
        onStart.invoke()
        val currentUser = ParseUser.getCurrentUser()
        if (currentUser != null) {
            currentUser.deleteInBackground(object : DeleteCallback {
                override fun done(e: ParseException?) {
                    if (e == null) {
                        onSuccess.invoke()
                        onComplete.invoke(null)
                    }
                    else {
                        onComplete.invoke(e)
                    }
                }
            })
        }
        else {
            onComplete.invoke(Exception("************ Current user is NULL ***********"))
        }
    }


    init {

        app.getAuthDataFromPref(
            onNotRegistered = {
                _state.value = UserState.Init
                _stateFlow.value = UserState.Init
            },
            onSuccess = { email, password ->
                signInWithEmailAndPassword(email, password)
            },
            onFailure = {exception ->
                _state.value = UserState.Failure(exception)
                _stateFlow.value = UserState.Failure(exception)
            }
        )

    }
}