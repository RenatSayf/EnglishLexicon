package com.myapp.lexicon.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.myapp.lexicon.helpers.printStackTraceIfDebug


private val Context.rSettings: SharedPreferences
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

val Context.feedAdIdFromPref: String
    get() {
        return try {
            rSettings.getString("FEED_AD_ID", "R-M-711878-18")!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            "R-M-711878-18"
        }
    }

val Context.nativeAdIdFromPref: String
    get() {
        return try {
            rSettings.getString("NATIVE_AD_ID", "R-M-711878-15")!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            "R-M-711878-15"
        }
    }

val Context.interstitialAdIdFromPref: String
    get() {
        return try {
            rSettings.getString("INTERSTITIAL_AD_ID", "R-M-711878-4")!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            "R-M-711878-4"
        }
    }

val Context.rewardedAdIdFromPref: String
    get() {
        return try {
            rSettings.getString("REWARDED_AD_ID", "R-M-711878-10")!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            "R-M-711878-10"
        }
    }

