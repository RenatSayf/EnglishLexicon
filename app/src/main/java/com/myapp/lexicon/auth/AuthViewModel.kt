package com.myapp.lexicon.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.myapp.lexicon.models.AppResult
import com.myapp.lexicon.settings.getPhoneFromPref
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private var _state = MutableLiveData<AppResult>().apply {
        app.getPhoneFromPref(
            onInit = {
                value = AppResult.Init
            },
            onSuccess = { phone ->

            },
            onFailure = { ex ->
                ex.printStackTrace()
            }
        )
    }
    val state: LiveData<AppResult> = _state

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
}