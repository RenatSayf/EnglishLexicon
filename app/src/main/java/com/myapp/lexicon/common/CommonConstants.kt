package com.myapp.lexicon.common

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.helpers.printStackTraceIfDebug

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

val PAYMENT_THRESHOLD: Double
    get() {
        return try {
            if (BuildConfig.ADS_SOURCE != AdsSource.LOCAL_HOST.name) {
                if (!BuildConfig.DEBUG)
                    Firebase.remoteConfig.getDouble("payment_threshold") else 0.1
            }
            else {
                1.0
            }
        }
        catch (e: Exception) {
            1.0
        }
    }

val APP_TIME_ZONE: String
    get() {
        return try {
            if (BuildConfig.ADS_SOURCE != AdsSource.LOCAL_HOST.name) {
                Firebase.remoteConfig.getString("APP_TIME_ZONE")
            }
            else {
                "Europe/Moscow"
            }
        } catch (e: Exception) {
            "Europe/Moscow"
        }
    }

val APP_VERSION: String
    get() {
        return "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }

val PAYMENT_CODE: String
    get() {
        return try {
            if (!BuildConfig.DEBUG)
                Firebase.remoteConfig.getString("PAYMENT_CODE").trim() else BuildConfig.PAYMENT_CODE.trim()
        } catch (e: Exception) {
            BuildConfig.PAYMENT_CODE.trim()
        }
    }

val PAYMENT_DAYS: Int
    get() {
        return try {
            Firebase.remoteConfig.getDouble("payment_days").toInt()
        } catch (e: Exception) {
            3
        }
    }

val EXPLAIN_MESSAGE: String
    get() = try {
        Firebase.remoteConfig.getString("reward_explain_message")
    } catch (e: Exception) {
        ""
    }

val IS_BANK_CARD_REQUIRED: Boolean
    get() = try {
        Firebase.remoteConfig.getBoolean("is_bank_card_required")
    } catch (e: Exception) {
        false
    }


val SELF_EMPLOYED_THRESHOLD: Int
    get() {
        return try {
            if (!BuildConfig.DEBUG) {
                Firebase.remoteConfig.getDouble("SELF_EMPLOYED_THRESHOLD").toInt()
            } else 20
        } catch (e: Exception) {
            1000
        }
    }

val SELF_EMPLOYED_PACKAGE: String
    get() {
        return "com.gnivts.selfemployed"
    }

val SELF_EMPLOYED_RU_STORE: Uri
    get() {
        return Uri.parse("https://www.rustore.ru/catalog/app/$SELF_EMPLOYED_PACKAGE")
    }

val SELF_EMPLOYED_MARKET: Uri
    get() {
        return Uri.parse("market://details?id=$SELF_EMPLOYED_PACKAGE")
    }

val PAYMENT_CHECK_PATTERN: String
    get() {
        return try {
            Firebase.remoteConfig.getString("PAYMENT_CHECK_PATTERN")
        } catch (e: Exception) {
            "^https://lknpd\\.nalog\\.ru/api/v1/receipt/\\d+/[a-zA-Z0-9]+/print$"
        }
    }

val AD_SHOWING_INTERVAL_IN_SEC: Long
    get() {
        return try {
            Firebase.remoteConfig.getLong("AD_SHOWING_INTERVAL_IN_SEC")
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            60L
        }
    }
