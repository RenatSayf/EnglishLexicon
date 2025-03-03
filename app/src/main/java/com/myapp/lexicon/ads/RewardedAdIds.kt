package com.myapp.lexicon.ads

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

enum class RewardedAdIds(val id: String) {
    REWARDED_1("R-M-711878-10"),
    REWARDED_2("R-M-711878-11"),
    REWARDED_3("R-M-711878-12")
}

val REWARDED_MAIN_ID: String
    get() {
        return try {
            Firebase.remoteConfig.getString("REWARDED_MAIN_ID")
        } catch (e: Exception) {
            RewardedAdIds.REWARDED_1.id
        }
    }

val REWARDED_TRANSLATE_ID: String
    get() {
        return try {
            Firebase.remoteConfig.getString("REWARDED_TRANSLATE_ID")
        } catch (e: Exception) {
            RewardedAdIds.REWARDED_3.id
        }
    }

val REWARDED_TEST_ID: String
    get() {
        return try {
            Firebase.remoteConfig.getString("REWARDED_TEST_ID")
        } catch (e: Exception) {
            RewardedAdIds.REWARDED_2.id
        }
    }

val REWARDED_VIDEO_ID: String
    get() {
        return try {
            Firebase.remoteConfig.getString("REWARDED_VIDEO_ID")
        } catch (e: Exception) {
            RewardedAdIds.REWARDED_1.id
        }
    }

val REWARDED_SERVICE_ID: String
    get() {
        return try {
            Firebase.remoteConfig.getString("REWARDED_SERVICE_ID")
        } catch (e: Exception) {
            RewardedAdIds.REWARDED_1.id
        }
    }