package com.myapp.lexicon.ads.models

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

enum class AdType(val type: Int) {
    BANNER(type = 1),
    NATIVE(type = 2),
    INTERSTITIAL(type = 3),
    REWARDED(type = 4)
}

val AD_MAIN: Int
    get() {
        return try {
            Firebase.remoteConfig.getLong("AD_MAIN").toInt()
        } catch (e: Exception) {
            2
        }
    }

val AD_SERVICE: Int
    get() {
        return try {
            Firebase.remoteConfig.getLong("AD_SERVICE").toInt()
        } catch (e: Exception) {
            2
        }
    }

val AD_TEST: Int
    get() {
        return try {
            Firebase.remoteConfig.getLong("AD_TEST").toInt()
        } catch (e: Exception) {
            4
        }
    }

val AD_TRANSLATE: Int
    get() {
        return try {
            Firebase.remoteConfig.getLong("AD_TRANSLATE").toInt()
        } catch (e: Exception) {
            2
        }
    }

val AD_VIDEO: Int
    get() {
        return try {
            Firebase.remoteConfig.getLong("AD_VIDEO").toInt()
        } catch (e: Exception) {
            4
        }
    }