package com.myapp.lexicon.ads.rewarded

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.myapp.lexicon.helpers.printStackTraceIfDebug

enum class RewardedAdIds(val id: String) {
    REWARDED_1("R-M-711878-10"),
    REWARDED_2("R-M-711878-11"),
    REWARDED_3("R-M-711878-12")
}

private val Context.pref: SharedPreferences
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

val Context.REWARDED_MAIN: String
    get() {
        return try {
            pref.getString("REWARDED_MAIN", RewardedAdIds.REWARDED_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            RewardedAdIds.REWARDED_1.id
        }
    }

val Context.REWARDED_TRANSLATE: String
    get() {
        return try {
            this.pref.getString("REWARDED_TRANSLATE", RewardedAdIds.REWARDED_2.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            RewardedAdIds.REWARDED_2.id
        }
    }

val Context.REWARDED_SERVICE: String
    get() {
        return try {
            this.pref.getString("REWARDED_SERVICE", RewardedAdIds.REWARDED_2.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            RewardedAdIds.REWARDED_2.id
        }
    }

val Context.REWARDED_TEST: String
    get() {
        return try {
            this.pref.getString("REWARDED_TEST", RewardedAdIds.REWARDED_3.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            RewardedAdIds.REWARDED_3.id
        }
    }

val Context.REWARDED_VIDEO: String
    get() {
        return try {
            this.pref.getString("REWARDED_VIDEO", RewardedAdIds.REWARDED_3.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            RewardedAdIds.REWARDED_3.id
        }
    }
