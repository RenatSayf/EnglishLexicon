package com.myapp.lexicon.ads

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.myapp.lexicon.helpers.printStackTraceIfDebug

enum class BannerAdIds(val id: String) {
    BANNER_1("R-M-711878-1"),
    BANNER_2("R-M-711878-2"),
    BANNER_3("R-M-711878-3"),
    BANNER_4("R-M-711878-7"),
    BANNER_5("R-M-711878-8")
}

val BANNER_MAIN: String
    get() {
        return try {
            Firebase.remoteConfig.getString("BANNER_MAIN").trim()
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_1.id
        }
    }

val BANNER_TRANSLATE: String
    get() {
        return try {
            Firebase.remoteConfig.getString("BANNER_TRANSLATE").trim()
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_2.id
        }
    }

val BANNER_SERVICE: String
    get() {
        return try {
            Firebase.remoteConfig.getString("BANNER_SERVICE").trim()
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_1.id
        }
    }

val BANNER_EDITOR: String
    get() {
        return try {
            Firebase.remoteConfig.getString("BANNER_EDITOR").trim()
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_4.id
        }
    }

val BANNER_ACTIVITY_1: String
    get() {
        return try {
            Firebase.remoteConfig.getString("BANNER_ACTIVITY_1").trim()
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_3.id
        }
    }

val BANNER_ACTIVITY_2: String
    get() {
        return try {
            Firebase.remoteConfig.getString("BANNER_ACTIVITY_2").trim()
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_4.id
        }
    }


