package com.myapp.lexicon.auth

import android.app.Application
import android.icu.util.Currency
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.models.currency.Currencies
import com.myapp.lexicon.settings.getAuthDataFromPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {
    sealed class LoadingState {
        object Start: LoadingState()
        object Complete: LoadingState()
    }

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private var _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState

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
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                val user = User(firebaseUser?.uid.toString()).apply {
                    this.email = email
                    this.password = password
                    val currency = Currency.getInstance(Locale.getDefault())
                    if (currency.currencyCode == Currencies.RUB.name) {
                        this.currency = currency.currencyCode
                        this.currencySymbol = currency.symbol
                    } else {
                        this.currency = Currencies.USD.name
                        this.currencySymbol = "$"
                    }
                }
                _state.value = UserState.SignUp(user)
                _stateFlow.value = UserState.SignUp(user)
            }
            .addOnFailureListener { ex ->
                val authException = ex as FirebaseAuthException
                when (authException.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        _state.value = UserState.AlreadyExists
                        _stateFlow.value = UserState.AlreadyExists
                    }
                    else -> {
                        _state.value = UserState.Failure(ex)
                        _stateFlow.value = UserState.Failure(ex)
                    }
                }
            }
            .addOnCompleteListener {
                _loadingState.value = LoadingState.Complete
            }
    }

    private fun isEmailExists(
        email: String,
        onYes: () -> Unit,
        onNo: () -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            _loadingState.value = LoadingState.Start
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val methods = task.result.signInMethods
                        if (methods.isNullOrEmpty()) {
                            onNo.invoke()
                        } else {
                            onYes.invoke()
                        }
                    }
                    _loadingState.value = LoadingState.Complete
                }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            onFailure.invoke(e)
            _loadingState.value = LoadingState.Complete
        }
    }

    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun signInWithEmailAndPassword(email: String, password: String) {

        _loadingState.value = LoadingState.Start
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    user?.uid?.let { id ->
                        val newState = UserState.SignIn(User(id).apply {
                            this.email = email
                            this.password = password
                        })
                        _state.value = newState
                        _stateFlow.value = newState
                    }
                } else {
                    val exception = task.exception as Exception
                    _state.value = UserState.Failure(exception)
                    _stateFlow.value = UserState.Failure(exception)
                }
                _loadingState.value = LoadingState.Complete
            }
    }

    fun resetPassword(email: String) {

        _loadingState.value = LoadingState.Start
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = UserState.PasswordReset
                    _stateFlow.value = UserState.PasswordReset
                } else {
                    val exception = task.exception as FirebaseAuthException
                    _state.value = UserState.Failure(exception)
                    _stateFlow.value = UserState.Failure(exception)
                }
                _loadingState.value = LoadingState.Complete
            }
    }


    fun verifyPhoneNumber(phone: String) {
        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {

                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {

                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    init {

        app.getAuthDataFromPref(
            onNotRegistered = {
                _state.value = UserState.NotRegistered
                _stateFlow.value = UserState.NotRegistered
            },
            onSuccess = { email, password ->
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    signInWithEmailAndPassword(email, password)
                }
                else {
                    val newState = UserState.SignIn(User(currentUser.uid).apply {
                        this.email = email
                        this.password = password
                    })
                    _state.value = newState
                    _stateFlow.value = newState
                }
            },
            onFailure = {exception ->
                _state.value = UserState.Failure(exception)
                _stateFlow.value = UserState.Failure(exception)
            }
        )

    }
}