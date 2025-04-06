package com.myapp.lexicon.ads.interstitial

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.myapp.lexicon.helpers.printStackTraceIfDebug

enum class InterstitialAdIds(val id: String) {
    INTERSTITIAL_1("R-M-711878-4"),
    INTERSTITIAL_2("R-M-711878-5"),
    INTERSTITIAL_3("R-M-711878-6"),
    INTERSTITIAL_4("R-M-711878-9"),
    INTERSTITIAL_5("R-M-711878-13")
}

private val Context.pref: SharedPreferences
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

val Context.INTERSTITIAL_MAIN: String
    get() {
        return try {
            this.pref.getString("INTERSTITIAL_MAIN", InterstitialAdIds.INTERSTITIAL_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            InterstitialAdIds.INTERSTITIAL_1.id
        }
    }

val Context.INTERSTITIAL_TRANSLATE: String
    get() {
        return try {
            this.pref.getString("INTERSTITIAL_TRANSLATE", InterstitialAdIds.INTERSTITIAL_2.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            InterstitialAdIds.INTERSTITIAL_2.id
        }
    }

val Context.INTERSTITIAL_VIDEO: String
    get() {
        return try {
            this.pref.getString("INTERSTITIAL_VIDEO", InterstitialAdIds.INTERSTITIAL_2.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            InterstitialAdIds.INTERSTITIAL_2.id
        }
    }

val Context.INTERSTITIAL_SERVICE: String
    get() {
        return try {
            this.pref.getString("INTERSTITIAL_SERVICE", InterstitialAdIds.INTERSTITIAL_3.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            InterstitialAdIds.INTERSTITIAL_3.id
        }
    }

val Context.INTERSTITIAL_TEST: String
    get() {
        return try {
            this.pref.getString("INTERSTITIAL_TEST", InterstitialAdIds.INTERSTITIAL_4.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            InterstitialAdIds.INTERSTITIAL_4.id
        }
    }

