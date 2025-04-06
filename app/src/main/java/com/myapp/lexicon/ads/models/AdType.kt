package com.myapp.lexicon.ads.models

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.myapp.lexicon.helpers.printStackTraceIfDebug

enum class AdType(val type: Int) {
    NATIVE(type = 1),
    INTERSTITIAL(type = 2),
    REWARDED(type = 3),
    FEED(type = 4)
}

private val Context.adSettings: SharedPreferences
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

val Context.AD_MAIN: Int
    get() {
        return try {
            this.adSettings.getInt("AD_MAIN", AdType.NATIVE.type)
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            AdType.NATIVE.type
        }
    }

val Fragment.AD_MAIN: Int
    get() {
        return requireContext().AD_MAIN
    }

val Context.AD_SERVICE: Int
    get() {
        return try {
            this.adSettings.getInt("AD_SERVICE", AdType.NATIVE.type)
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            AdType.NATIVE.type
        }
    }

val Fragment.AD_SERVICE: Int
    get() {
        return requireContext().AD_SERVICE
    }

val Context.AD_TEST: Int
    get() {
        return try {
            this.adSettings.getInt("AD_TEST", AdType.REWARDED.type)
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            AdType.REWARDED.type
        }
    }

val Fragment.AD_TEST: Int
    get() {
        return requireContext().AD_TEST
    }

val Context.AD_TRANSLATE: Int
    get() {
        return try {
            this.adSettings.getInt("AD_TRANSLATE", AdType.INTERSTITIAL.type)
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            AdType.INTERSTITIAL.type
        }
    }

val Fragment.AD_TRANSLATE: Int
    get() {
        return requireContext().AD_TRANSLATE
    }

val Context.AD_VIDEO: Int
    get() {
        return try {
            this.adSettings.getInt("AD_VIDEO", AdType.REWARDED.type)
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            AdType.REWARDED.type
        }
    }

val Fragment.AD_VIDEO: Int
    get() {
        return requireContext().AD_VIDEO
    }