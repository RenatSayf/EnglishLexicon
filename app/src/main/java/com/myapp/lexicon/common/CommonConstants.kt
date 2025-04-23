package com.myapp.lexicon.common

import android.net.Uri
import androidx.core.net.toUri
import com.myapp.lexicon.BuildConfig

const val KEY_APP_STORE_LINK = "link"

enum class AdsSource {
    TEST_AD,
    ALIVE_AD,
    LOCAL_HOST
}

val BASE_URL: String
    get() {
        return BuildConfig.BASE_URL
    }

val API_KEY: String
    get() {
        return BuildConfig.API_KEY
    }

val APP_TIME_ZONE: String
    get() {
        return "Europe/Moscow"
    }

val APP_VERSION: String
    get() {
        return "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }


val SELF_EMPLOYED_PACKAGE: String
    get() {
        return "com.gnivts.selfemployed"
    }

val SELF_EMPLOYED_RU_STORE: Uri
    get() {
        return "https://www.rustore.ru/catalog/app/$SELF_EMPLOYED_PACKAGE".toUri()
    }

val SELF_EMPLOYED_MARKET: Uri
    get() {
        return "market://details?id=$SELF_EMPLOYED_PACKAGE".toUri()
    }

