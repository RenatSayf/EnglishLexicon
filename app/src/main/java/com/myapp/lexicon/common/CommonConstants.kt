package com.myapp.lexicon.common

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.ads.models.AdType
import com.myapp.lexicon.helpers.printStackTraceIfDebug

const val KEY_APP_STORE_LINK = "link"

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

val IS_ADS_ENABLED: Boolean
    get() {
        return try {
            Firebase.remoteConfig.getBoolean("is_ads_enabled")
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            true
        }
    }

val IS_REWARD_ACCESSIBLE: Boolean
    get() {
        return try {
            Firebase.remoteConfig.getBoolean("IS_REWARD_ACCESSIBLE")
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            true
        }
    }

val MESSAGE_TO_USER: String
    get() {
        return try {
            Firebase.remoteConfig.getString("MESSAGE_TO_USER")
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            "Unknown error"
        }
    }

val APP_TIME_ZONE: String
    get() {
        return try {
            Firebase.remoteConfig.getString("APP_TIME_ZONE")
        } catch (e: Exception) {
            "Europe/Moscow"
        }
    }

