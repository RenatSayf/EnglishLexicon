package com.myapp.lexicon.common

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.ads.models.AdType
import com.myapp.lexicon.helpers.printStackTraceIfDebug

const val COLLECTION_PATH_USERS = "users"
const val KEY_APP_STORE_LINK = "link"
const val APP_STORE_LINK = "https://play.google.com/store/apps/details?id=com.myapp.lexicon"

enum class AdsSource {
    TEST_AD,
    ALIVE_AD,
    LOCAL_HOST
}

val USER_AGENT = try {
    Firebase.remoteConfig.getString("user_agent")
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
}

val IS_IMPORTANT_UPDATE: String = try {
    Firebase.remoteConfig.getString("is_important_update")
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    ""
}

val PAYMENTS_CONDITIONS: String = try {
    Firebase.remoteConfig.getString("payments_conditions")
} catch (e: Exception) {
    e.printStackTraceIfDebug()
    ""
}

val IS_MULTI_BANNER: Boolean
    get() {
        return try {
            Firebase.remoteConfig.getBoolean("is_multi_banner")
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            true
        }
    }

val AD_TYPE: AdType
    get() {
        return try {
            val value = Firebase.remoteConfig.getLong("ad_type").toInt()
            when (value) {
                1 -> AdType.BANNER
                2 -> AdType.NATIVE
                else -> AdType.INTERSTITIAL
            }
        }
        catch (e: Exception) {
            e.printStackTraceIfDebug()
            AdType.BANNER
        }
    }

