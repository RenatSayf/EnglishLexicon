@file:Suppress("ObjectLiteralToLambda", "PropertyName", "UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.auth

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.common.mapToUser
import com.myapp.lexicon.di.INetRepositoryModule
import com.myapp.lexicon.di.NetRepositoryModule
import com.myapp.lexicon.helpers.castToHttpThrowable
import com.myapp.lexicon.models.SignInData
import com.myapp.lexicon.models.SignUpData
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.repository.network.INetRepository
import com.parse.GetCallback
import com.parse.LogInCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


open class AuthViewModel(
    private val netModule: INetRepositoryModule
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val netModule: INetRepositoryModule = NetRepositoryModule()
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == AuthViewModel::class.java)
            return AuthViewModel(netModule) as T
        }
    }

    companion object {
        const val ACCOUNT_DELETING_ERROR = "Account deleting error"
    }

    sealed class LoadingState {
        data object Start: LoadingState()
        data object Complete: LoadingState()
    }

    protected var _loadingState = MutableLiveData<LoadingState>()
    open val loadingState: LiveData<LoadingState> = _loadingState

    open fun setLoadingState(state: LoadingState) {
        _loadingState.value = state
    }

    sealed class ScreenState {
        data object Init: ScreenState()
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

    open fun setState(state: UserState) {
        _state.value = state
    }

    private val repository: INetRepository = netModule.apply {
        setTokensUpdateListener(object : INetRepositoryModule.Listener {
            override fun onUpdateTokens(tokens: Tokens) {
                netModule.setRefreshToken(tokens.refreshToken)
                _state.postValue(UserState.TokensUpdated(tokens))
            }

            override fun onAuthorizationRequired() {
                _state.postValue(UserState.NotAcceptable)
            }
        })
    }.provideNetRepository()

    open fun registerForNewUser(email: String, password: String, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        _loadingState.value = LoadingState.Start

        val signUpData = SignUpData(appVersion = BuildConfig.VERSION_NAME, email, password)
        viewModelScope.launch(dispatcher) {
            repository.signUp(data = signUpData).collect(collector = { result ->
                result.onSuccess { value: Tokens ->
                    netModule.setRefreshToken(value.refreshToken)
                    _state.postValue(UserState.LogUp(value))
                }
                result.onFailure { exception: Throwable ->
                    val errorCode = exception.castToHttpThrowable().errorCode
                    when(errorCode) {
                        409 -> {
                            _state.postValue(UserState.AlreadyExists)
                        }
                        422 -> {
                            _state.postValue(UserState.PasswordValid(false))
                        }
                        else -> {
                            _state.postValue(UserState.HttpFailure(exception.message))
                        }
                    }
                }
                _loadingState.postValue(LoadingState.Complete)
            })
        }

    }

    open fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // login in TimeWeb
    open fun logInWithEmailAndPassword(email: String, password: String, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        _loadingState.value = LoadingState.Start
        viewModelScope.launch(dispatcher) {
            val signInData = SignInData(email, password)
            repository.signIn(signInData).collect(collector = { result ->
                result.onSuccess { value: Tokens ->
                    netModule.setRefreshToken(value.refreshToken)
                    _state.postValue(UserState.LogIn(value))
                }
                result.onFailure { exception: Throwable ->
                    val errorCode = exception.castToHttpThrowable().errorCode
                    when(errorCode) {
                        404 -> {
                            _state.postValue(UserState.NotRegistered)
                        }
                        406 -> {
                            _state.postValue(UserState.NotAcceptable)
                        }
                        else -> {
                            _state.postValue(UserState.HttpFailure(exception.message))
                        }
                    }
                }
                _loadingState.postValue(LoadingState.Complete)
            })
        }
    }

    var user: User? = null
        private set

    // login in Back4App
    open fun signInWithEmailAndPassword(email: String, password: String) {

        _loadingState.value = LoadingState.Start
        ParseUser.logInInBackground(
            email,
            password,
            object : LogInCallback {
                override fun done(u: ParseUser?, e: ParseException?) {

                    when {
                        u is ParseUser -> {
                            _loadingState.value = LoadingState.Start
                            val query = ParseQuery<ParseObject>("_User")
                            query.getInBackground(u.objectId, object : GetCallback<ParseObject> {

                                override fun done(obj: ParseObject?, e: ParseException?) {
                                    when {
                                        obj is ParseObject -> {
                                            val userFromCloud = obj.mapToUser()
                                            val newState = UserState.SignIn(userFromCloud.apply {
                                                this.email = email
                                                this.password = password
                                            })
                                            user = userFromCloud
                                            _state.value = newState
                                        }

                                        e is ParseException -> {
                                            _state.value = UserState.Failure(Exception(e.message))
                                        }
                                    }
                                    _loadingState.value = LoadingState.Complete
                                }
                            })
                        }
                        e is ParseException -> {
                            ParseUser.logOut()
                            if (e.code == ParseException.OBJECT_NOT_FOUND) {
                                ParseException.EMAIL_TAKEN
                                _state.value = UserState.NotRegistered
                            } else {

                                _state.value = UserState.Failure(Exception(e.message))
                            }
                        }
                    }
                    _loadingState.value = LoadingState.Complete
                }
            }
        )
    }

    open fun resetUserPassword(email: String, dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        _loadingState.value = LoadingState.Start
        viewModelScope.launch(dispatcher) {
            repository.forgotPassword(email).collect(collector = { result ->
                result.onSuccess { value: String ->
                    _state.postValue(UserState.PasswordReset)
                }
                result.onFailure { exception: Throwable ->
                    val errorCode = exception.castToHttpThrowable().errorCode
                    when(errorCode) {
                        404 -> {
                            _state.postValue(UserState.NotRegistered)
                        }
                        else -> {
                            _state.postValue(UserState.HttpFailure(exception.message))
                        }
                    }
                }
                _loadingState.postValue(LoadingState.Complete)
            })
        }

    }

    open fun deleteUserAccount(
        token: String,
        onStart: () -> Unit = {},
        onSuccess: () -> Unit,
        onComplete: (Exception?) -> Unit = {},
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        onStart.invoke()
        viewModelScope.launch(dispatcher) {
            repository.deleteUser(token).collect(collector = { result ->
                result.onSuccess { value: Boolean ->
                    if (value) {
                        onSuccess.invoke()
                        onComplete.invoke(null)
                    }
                    else {
                        onComplete.invoke(Exception(ACCOUNT_DELETING_ERROR))
                    }
                }
                result.onFailure { exception: Throwable ->
                    onComplete.invoke(exception as Exception)
                }
            })
        }
    }


}