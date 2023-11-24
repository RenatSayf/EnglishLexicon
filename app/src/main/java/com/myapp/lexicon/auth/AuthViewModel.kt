@file:Suppress("ObjectLiteralToLambda", "PropertyName")

package com.myapp.lexicon.auth

import android.app.Application
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.common.mapToUser
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.settings.getAuthDataFromPref
import com.parse.DeleteCallback
import com.parse.GetCallback
import com.parse.LogInCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.RequestPasswordResetCallback
import com.parse.SignUpCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


open class AuthViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    sealed class LoadingState {
        object Start: LoadingState()
        object Complete: LoadingState()
    }

    protected var _loadingState = MutableLiveData<LoadingState>()
    open val loadingState: LiveData<LoadingState> = _loadingState

    open fun setLoadingState(state: LoadingState) {
        _loadingState.value = state
    }

    sealed class ScreenState {
        object Init: ScreenState()
        data class Current(
            val emailText: String,
            val emailIsFocused: Boolean,
            val emailBackground: Drawable,
            val passwordText: String,
            val passwordIsFocused: Boolean,
            val passwordBackground: Drawable,
            val btnSignInEnable: Boolean,
            val btnSignUpEnable: Boolean,
        ): ScreenState()
    }

    private var _screenState = MutableLiveData<ScreenState>(ScreenState.Init)
    open val screenState: LiveData<ScreenState> = _screenState
    fun setScreenState(state: ScreenState) {
        _screenState.value = state
    }

    protected var _state = MutableLiveData<UserState>().apply {
        value = UserState.Init
    }
    open val state: LiveData<UserState> = _state

    protected val _stateFlow = MutableStateFlow<UserState>(UserState.Init)
    open val stateFlow: StateFlow<UserState> = _stateFlow

    open fun setState(state: UserState) {
        _state.value = state
        _stateFlow.value = state
    }

    open fun registerWithEmailAndPassword(email: String, password: String) {

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

    open fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    open fun signInWithEmailAndPassword(email: String, password: String) {

        _loadingState.value = LoadingState.Start
        ParseUser.logInInBackground(
            email,
            password,
            object : LogInCallback {
                override fun done(user: ParseUser?, e: ParseException?) {

                    when {
                        user is ParseUser -> {
                            _loadingState.value = LoadingState.Start
                            val query = ParseQuery<ParseObject>("_User")
                            query.getInBackground(user.objectId, object : GetCallback<ParseObject> {

                                override fun done(obj: ParseObject?, e: ParseException?) {
                                    when {
                                        obj is ParseObject -> {
                                            val userFromCloud = obj.mapToUser()
                                            val newState = UserState.SignIn(userFromCloud.apply {
                                                this.email = email
                                                this.password = password
                                            })
                                            _state.value = newState
                                            _stateFlow.value = newState
                                        }

                                        e is ParseException -> {
                                            _state.value = UserState.Failure(Exception(e.message))
                                            _stateFlow.value = UserState.Failure(Exception(e.message))
                                        }
                                    }
                                    _loadingState.value = LoadingState.Complete
                                }
                            })
                        }
                        e is ParseException -> {
                            ParseUser.logOut()
                            if (e.code == ParseException.OBJECT_NOT_FOUND) {

                                _state.value = UserState.NotRegistered
                                _stateFlow.value = UserState.NotRegistered
                            } else {

                                _state.value = UserState.Failure(Exception(e.message))
                                _stateFlow.value = UserState.Failure(Exception(e.message))
                            }
                        }
                    }
                    _loadingState.value = LoadingState.Complete
                }
            }
        )
    }

    open fun resetPassword(email: String) {

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

    open fun deleteAccount(
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
                        ParseUser.logOut()
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