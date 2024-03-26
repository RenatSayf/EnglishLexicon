package com.myapp.lexicon.common

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.parse.ParseConfig

const val COLLECTION_PATH_USERS = "users"
const val KEY_APP_STORE_LINK = "link"
const val APP_STORE_LINK = "https://play.google.com/store/apps/details?id=com.myapp.lexicon"

enum class AdsSource {
    TEST_AD,
    ALIVE_AD,
    LOCAL_HOST
}

val IS_VIDEO_SECTION = try {
    val remoteKey = "is_video_section"
    var value = ParseConfig.getCurrentConfig().getBoolean("is_video_section")
    if (!value) {
        value = ParseConfig.get().getBoolean(remoteKey)
    }
    value
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    false
}

val VIDEO_URL: String = try {
    val remoteKey = "video_url"
    var value = ParseConfig.getCurrentConfig().getString(remoteKey)
    if (value == null) {
        value = ParseConfig.get().getString(remoteKey)
    }
    value
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    ""
}

val PRETTY_PRINT_URL: String = try {
    val remoteKey = "pretty_print"
    var value = ParseConfig.getCurrentConfig().getString(remoteKey)
    if (value == null) {
        value = ParseConfig.get().getString(remoteKey)
    }
    value
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    ""
}

val USER_AGENT = try {
    Firebase.remoteConfig.getString("user_agent")
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
}

