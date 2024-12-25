package com.myapp.lexicon.models

import kotlinx.serialization.Serializable


@Serializable
data class SignInData(
    val email: String,
    val password: String
)