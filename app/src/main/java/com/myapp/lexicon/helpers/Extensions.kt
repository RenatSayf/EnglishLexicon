package com.myapp.lexicon.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment

private const val APP_SETTINGS = "APP_SETTINGS"
private const val NO_ADS_TOKEN = "NO_ADS_TOKEN"

val Context.appPref: SharedPreferences
    get() {
        return this.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
    }

val Fragment.appPref: SharedPreferences
    get() {
        return requireContext().appPref
    }

fun Context.saveNoAdsToken(token: String) {
    appPref.edit().putString(NO_ADS_TOKEN, token).apply()
}

fun Fragment.saveNoAdsToken(token: String) {
    requireContext().saveNoAdsToken(token)
}

val Context.noAdsToken: String
    get() {
        return appPref.getString(NO_ADS_TOKEN, null)?: ""
    }

val Fragment.noAdsToken: String
    get() {
        return requireContext().noAdsToken
    }
