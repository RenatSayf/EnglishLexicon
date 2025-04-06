package com.myapp.lexicon.ads.native_ad

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.myapp.lexicon.helpers.printStackTraceIfDebug

enum class NativeAdIds(val id: String) {
    NATIVE_1(id = "R-M-711878-14"),
    NATIVE_2(id = "R-M-711878-15")
}

private val Context.pref: SharedPreferences
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

val Context.NATIVE_MAIN: String
    get() {
        return try {
            this.pref.getString("NATIVE_MAIN", NativeAdIds.NATIVE_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            NativeAdIds.NATIVE_1.id
        }
    }

val Context.NATIVE_TRANSLATE: String
    get() {
        return try {
            this.pref.getString("NATIVE_TRANSLATE", NativeAdIds.NATIVE_2.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            NativeAdIds.NATIVE_2.id
        }
    }

val Context.NATIVE_TEST: String
    get() {
        return try {
            this.pref.getString("NATIVE_TEST", NativeAdIds.NATIVE_2.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            NativeAdIds.NATIVE_2.id
        }
    }

val Context.NATIVE_VIDEO: String
    get() {
        return try {
            this.pref.getString("NATIVE_VIDEO", NativeAdIds.NATIVE_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            NativeAdIds.NATIVE_1.id
        }
    }

val Context.NATIVE_SERVICE: String
    get() {
        return try {
            this.pref.getString("NATIVE_SERVICE", NativeAdIds.NATIVE_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            NativeAdIds.NATIVE_1.id
        }
    }