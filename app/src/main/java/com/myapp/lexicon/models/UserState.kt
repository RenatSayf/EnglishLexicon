package com.myapp.lexicon.models

sealed class UserState {
    object Init: UserState()
    object NotRegistered: UserState()
    object EmailNotValid: UserState()
    data class SignUp(val user: User): UserState()
    data class SignIn(val user: User): UserState()
    data class Failure(val error: Exception): UserState()

    fun onInit(onInit: () -> Unit) {
        if (this is Init) {
            onInit.invoke()
        }
    }

    fun onNotRegistered(onNotRegistered: () -> Unit) {
        if (this is NotRegistered) {
            onNotRegistered.invoke()
        }
    }

    fun onNotValid(onNotValid: () -> Unit) {
        if (this is EmailNotValid) {
            onNotValid.invoke()
        }
    }

    fun onSignUp(onSignUp: (User) -> Unit) {
        if (this is SignUp) {
            onSignUp.invoke(this.user)
        }
    }

    fun onSignIn(onSignIn: (User) -> Unit) {
        if (this is SignIn) {
            onSignIn.invoke(this.user)
        }
    }

    fun onFailure(onFailure: (Exception) -> Unit) {
        if (this is Failure) {
            onFailure.invoke(this.error)
        }
    }
}
