package com.myapp.lexicon.auth

import android.app.Application
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
import com.myapp.lexicon.settings.getEmailPasswordFromPref
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private var _state = MutableLiveData<UserState>().apply {
        value = UserState.Init
    }
    val state: LiveData<UserState> = _state

    fun registerWithEmailAndPassword(email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                _state.value = UserState.SignUp(User(user?.email.toString()))
            }
            .addOnFailureListener { ex ->
                val authException = ex as FirebaseAuthException
                when (authException.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        _state.value = UserState.AlreadyExists
                    }
                    else -> {
                        _state.value = UserState.Failure(ex)
                    }
                }
            }
    }

    private fun isEmailExists(
        email: String,
        onYes: () -> Unit,
        onNo: () -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val methods = task.result.signInMethods
                    if (methods.isNullOrEmpty()) {
                        onNo.invoke()
                    }
                    else {
                        onYes.invoke()
                    }
                }
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            onFailure.invoke(e)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    user?.uid?.let { id ->
                        _state.value = UserState.SignIn(User(id).apply {
                            this.email = email
                            this.password = password
                        })
                    }
                } else {
                    val exception = task.exception as Exception
                    _state.value = UserState.Failure(exception)
                }
            }
    }

    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = UserState.PasswordReset
                } else {
                    val exception = task.exception as FirebaseAuthException
                    _state.value = UserState.Failure(exception)
                }
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

        app.getEmailPasswordFromPref(
            onNotRegistered = {
                _state.value = UserState.NotRegistered
            },
            onSuccess = { email, password ->
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    signInWithEmailAndPassword(email, password)
                }
                else {
                    _state.value = UserState.SignIn(User(currentUser.email.toString()))
                }
            },
            onFailure = {exception ->
                _state.value = UserState.Failure(exception)
            }
        )

    }
}