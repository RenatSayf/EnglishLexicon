package com.myapp.lexicon.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SignUpData(
    @SerialName("app_version")
    val appVersion: String,
    val email: String,
    val password: String,
    @SerialName("role_code")
    val roleCode: Int = 0
)