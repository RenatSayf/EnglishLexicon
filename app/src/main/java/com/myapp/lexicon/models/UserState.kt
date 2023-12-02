package com.myapp.lexicon.models

sealed class UserState {
    object Init: UserState()
    object NotRegistered: UserState()
    data class EmailValid(val flag: Boolean): UserState()
    data class PasswordValid(val flag: Boolean): UserState()
    object AlreadyExists: UserState()
    object PasswordReset: UserState()
    object SignOut: UserState()
    object AccountDeleted: UserState()
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

    fun onEmailValid(onValid: (Boolean) -> Unit) {
        if (this is EmailValid) {
            onValid.invoke(this.flag)
        }
    }

    fun onPasswordValid(onValid: (Boolean) -> Unit) {
        if (this is PasswordValid) {
            onValid.invoke(this.flag)
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

    fun onSignOut(onOut: () -> Unit) {
        if (this is SignOut) {
            onOut.invoke()
        }
    }

    fun onFailure(onFailure: (Exception) -> Unit) {
        if (this is Failure) {
            onFailure.invoke(this.error)
        }
    }

    fun onExists(onExists: () -> Unit) {
        if (this is AlreadyExists) {
            onExists.invoke()
        }
    }

    fun onPasswordReset(onReset: () -> Unit) {
        if (this is PasswordReset) {
            onReset.invoke()
        }
    }

    fun onAccountDeleted(onDeleted: () -> Unit) {
        if (this is AccountDeleted) {
            onDeleted.invoke()
        }
    }
}
