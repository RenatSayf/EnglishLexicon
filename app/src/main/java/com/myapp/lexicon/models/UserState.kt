package com.myapp.lexicon.models

sealed class UserState {
    data object Init: UserState()
    data object NotRegistered: UserState()
    data object NotAcceptable: UserState()
    data class EmailValid(val flag: Boolean): UserState()
    data class PasswordValid(val flag: Boolean): UserState()
    data object AlreadyExists: UserState()
    data object PasswordReset: UserState()
    data object SignOut: UserState()
    data object AccountDeleted: UserState()
    data class SignUp(val user: User): UserState()
    data class SignIn(val user: User): UserState()
    data class LogUp(val tokens: Tokens): UserState()
    data class LogIn(val tokens: Tokens): UserState()
    data class TokensUpdated(val tokens: Tokens): UserState()
    data class Failure(val error: Exception): UserState()
    data class HttpFailure(val message: String?): UserState()

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

    fun onNotAcceptable(onNotAcceptable: () -> Unit) {
        if (this is NotAcceptable) {
            onNotAcceptable.invoke()
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

    fun onLogUp(onLogUp: (Tokens) -> Unit) {
        if (this is LogUp) {
            onLogUp.invoke(this.tokens)
        }
    }

    fun onLogIn(onLogIn: (Tokens) -> Unit) {
        if (this is LogIn) {
            onLogIn.invoke(this.tokens)
        }
    }

    fun onTokensUpdated(onUpdate: (Tokens) -> Unit) {
        if (this is TokensUpdated) {
            onUpdate.invoke(this.tokens)
        }
    }

    fun onFailure(onFailure: (Exception) -> Unit) {
        if (this is Failure) {
            onFailure.invoke(this.error)
        }
    }

    fun onHttpFailure(onFailure: (String?) -> Unit) {
        if (this is HttpFailure) {
            onFailure.invoke(this.message)
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
