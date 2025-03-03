package com.myapp.lexicon.ads

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

enum class NativeAdIds(val id: String) {
    NATIVE_1(id = "R-M-711878-14"),
    NATIVE_2(id = "R-M-711878-15")
}

val NATIVE_AD_MAIN: String
    get() {
        return try {
            Firebase.remoteConfig.getString("NATIVE_AD_MAIN").trim()
        } catch (e: Exception) {
            NativeAdIds.NATIVE_1.id
        }
    }

val NATIVE_AD_TRANS: String
    get() {
        return try {
            Firebase.remoteConfig.getString("NATIVE_AD_TRANS").trim()
        } catch (e: Exception) {
            NativeAdIds.NATIVE_1.id
        }
    }

val NATIVE_AD_TEST: String
    get() {
        return try {
            Firebase.remoteConfig.getString("NATIVE_AD_TEST").trim()
        } catch (e: Exception) {
            NativeAdIds.NATIVE_1.id
        }
    }

val NATIVE_AD_VIDEO: String
    get() {
        return try {
            Firebase.remoteConfig.getString("NATIVE_AD_VIDEO").trim()
        } catch (e: Exception) {
            NativeAdIds.NATIVE_2.id
        }
    }

val NATIVE_AD_SERVICE: String
    get() {
        return try {
            Firebase.remoteConfig.getString("NATIVE_AD_SERVICE").trim()
        } catch (e: Exception) {
            NativeAdIds.NATIVE_2.id
        }
    }