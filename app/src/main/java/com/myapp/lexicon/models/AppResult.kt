package com.myapp.lexicon.models


@Suppress("UNCHECKED_CAST")
sealed class AppResult {

    object Init: AppResult()
    class Success<T>(val data: T): AppResult()
    data class Error(val error: Throwable): AppResult()

    fun<T> onSuccess(onSuccess: (T) -> Unit) {
        if (this is Success<*>) {
            onSuccess.invoke(this.data as T)
        }
    }

    fun onError(onError: (Throwable) -> Unit) {
        if (this is Error) {
            onError.invoke(this.error)
        }
    }
}

