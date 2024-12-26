package com.myapp.lexicon.models

import com.myapp.lexicon.common.APP_VERSION
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserProfile(
    val email: String,

    val phone: String?,

    @SerialName("first_name")
    val firstName: String?,

    @SerialName("second_name")
    val secondName: String?,

    @SerialName("last_name")
    val lastName: String?,

    @SerialName("bank_card")
    val bankCard: String?,

    @SerialName("bank_name")
    val bankName: String?,

    @SerialName("app_version")
    val appVersion: String = APP_VERSION,

    @SerialName("role_code")
    val roleCode: Int = 0
)