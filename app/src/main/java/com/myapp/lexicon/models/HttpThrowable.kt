package com.myapp.lexicon.models

class HttpThrowable(override val message: String?, val errorCode: Int): Throwable(message)