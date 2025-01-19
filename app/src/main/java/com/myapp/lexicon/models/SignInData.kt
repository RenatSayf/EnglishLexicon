package com.myapp.lexicon.models

import com.myapp.lexicon.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SignInData(
    val email: String,
    val password: String,
    @SerialName("app_version")
    val appVersion: String = BuildConfig.VERSION_NAME
)