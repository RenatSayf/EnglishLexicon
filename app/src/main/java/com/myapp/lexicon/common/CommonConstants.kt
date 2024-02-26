package com.myapp.lexicon.common

import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.helpers.printStackTraceIfDebug

const val COLLECTION_PATH_USERS = "users"
const val KEY_APP_STORE_LINK = "link"
const val APP_STORE_LINK = "https://play.google.com/store/apps/details?id=com.myapp.lexicon"

enum class AdsSource {
    TEST_AD,
    ALIVE_AD,
    LOCAL_HOST
}

const val VIDEO_BASE_URL = "https://www.youtube.com/watch?v="
val USER_AGENT = try {
    Firebase.remoteConfig.getString("user_agent")
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
}

