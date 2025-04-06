package com.myapp.lexicon.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.myapp.lexicon.ads.BannerAdIds
import com.myapp.lexicon.ads.InterstitialAdIds
import com.myapp.lexicon.ads.NativeAdIds
import com.myapp.lexicon.ads.RewardedAdIds
import com.myapp.lexicon.ads.feed_ad.FeedAdIds
import com.myapp.lexicon.helpers.printStackTraceIfDebug


private val Context.rSettings: SharedPreferences
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

val Context.feedAdIdFromPref: String
    get() {
        return try {
            rSettings.getString("FEED_AD_ID", FeedAdIds.FEED_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            FeedAdIds.FEED_1.id
        }
    }

val Context.nativeAdIdFromPref: String
    get() {
        return try {
            rSettings.getString("NATIVE_AD_ID", NativeAdIds.NATIVE_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            NativeAdIds.NATIVE_1.id
        }
    }

val Context.interstitialAdIdFromPref: String
    get() {
        return try {
            rSettings.getString("INTERSTITIAL_AD_ID", InterstitialAdIds.INTERSTITIAL_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            InterstitialAdIds.INTERSTITIAL_1.id
        }
    }

val Context.rewardedAdIdFromPref: String
    get() {
        return try {
            rSettings.getString("REWARDED_AD_ID", RewardedAdIds.REWARDED_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            RewardedAdIds.REWARDED_1.id
        }
    }

val Context.mainBannerId: String
    get() {
        return try {
            rSettings.getString("MAIN_BANNER_ID", BannerAdIds.BANNER_1.id)!!
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            BannerAdIds.BANNER_1.id
        }
    }

