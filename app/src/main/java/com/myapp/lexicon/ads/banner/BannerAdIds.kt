package com.myapp.lexicon.ads.banner

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.myapp.lexicon.helpers.printStackTraceIfDebug

enum class BannerAdIds(val id: String) {
    BANNER_1("R-M-711878-1"),
    BANNER_2("R-M-711878-2"),
    BANNER_3("R-M-711878-3"),
    BANNER_4("R-M-711878-7"),
    BANNER_5("R-M-711878-8")
}

private val Context.pref: SharedPreferences
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

val Context.BANNER_MAIN: String
    get() {
        return try {
            pref.getString("MAIN_BANNER_ID", BannerAdIds.BANNER_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_1.id
        }
    }

val Context.BANNER_TRANSLATE: String
    get() {
        return try {
            this.pref.getString("BANNER_TRANSLATE", BannerAdIds.BANNER_2.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_2.id
        }
    }

val Context.BANNER_SERVICE: String
    get() {
        return try {
            this.pref.getString("BANNER_SERVICE", BannerAdIds.BANNER_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_1.id
        }
    }

val Context.BANNER_EDITOR: String
    get() {
        return try {
            this.pref.getString("BANNER_EDITOR", BannerAdIds.BANNER_3.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_3.id
        }
    }


