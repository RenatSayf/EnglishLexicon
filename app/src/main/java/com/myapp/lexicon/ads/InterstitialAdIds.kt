package com.myapp.lexicon.ads

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

enum class InterstitialAdIds(val id: String) {
    INTERSTITIAL_1("R-M-711878-4"),
    INTERSTITIAL_2("R-M-711878-5"),
    INTERSTITIAL_3("R-M-711878-6"),
    INTERSTITIAL_4("R-M-711878-9"),
    INTERSTITIAL_5("R-M-711878-13")
}

val INTERSTITIAL_MAIN: String
    get() {
        return try {
            Firebase.remoteConfig.getString("INTERSTITIAL_MAIN").trim()
        } catch (e: Exception) {
            InterstitialAdIds.INTERSTITIAL_1.id
        }
    }

val INTERSTITIAL_TRANSLATE: String
    get() {
        return try {
            Firebase.remoteConfig.getString("INTERSTITIAL_TRANSLATE").trim()
        } catch (e: Exception) {
            InterstitialAdIds.INTERSTITIAL_2.id
        }
    }

val INTERSTITIAL_VIDEO: String
    get() {
        return try {
            Firebase.remoteConfig.getString("INTERSTITIAL_VIDEO").trim()
        } catch (e: Exception) {
            InterstitialAdIds.INTERSTITIAL_2.id
        }
    }

val INTERSTITIAL_SERVICE: String
    get() {
        return try {
            Firebase.remoteConfig.getString("INTERSTITIAL_SERVICE").trim()
        } catch (e: Exception) {
            InterstitialAdIds.INTERSTITIAL_3.id
        }
    }

val INTERSTITIAL_TEST: String
    get() {
        return try {
            Firebase.remoteConfig.getString("INTERSTITIAL_TEST").trim()
        } catch (e: Exception) {
            InterstitialAdIds.INTERSTITIAL_4.id
        }
    }

