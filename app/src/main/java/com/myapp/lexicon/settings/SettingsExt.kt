package com.myapp.lexicon.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.myapp.lexicon.R

val Context.appSettings: SharedPreferences
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

val Fragment.appSettings: SharedPreferences
    get() {
        return requireContext().appSettings
    }

var Context.adsIsEnabled: Boolean
    get() {
        return appSettings.getBoolean(getString(R.string.KEY_IS_ADS_ENABLED), true)
    }
    set(value) {
        appSettings.edit().putBoolean(getString(R.string.KEY_IS_ADS_ENABLED), value).apply()
    }

var Fragment.adsIsEnabled: Boolean
    get() {
        return requireContext().adsIsEnabled
    }
    set(value) {
        requireContext().adsIsEnabled = value
    }

var Context.cloudStorageEnabled: Boolean
    get() {
        return appSettings.getBoolean(getString(R.string.KEY_CLOUD_STORAGE), false)
    }
    set(value) {
        appSettings.edit().putBoolean(getString(R.string.KEY_CLOUD_STORAGE), value).apply()
    }

var Fragment.cloudStorageEnabled: Boolean
    get() {
        return requireContext().cloudStorageEnabled
    }
    set(value) {
        requireContext().cloudStorageEnabled = value
    }

var Context.checkFirstLaunch: Boolean
    get() {
        return appSettings.getBoolean(getString(R.string.KEY_FIRST_LAUNCH), true)
    }
    set(value) {
        appSettings.edit().putBoolean(getString(R.string.KEY_FIRST_LAUNCH), value).apply()
    }

private const val KEY_ADS_TOKEN = "KEY_ADS_TOKEN_555"
private const val KEY_CLOUD_TOKEN = "KEY_CLOUD_TOKEN_777"

interface PurchasesTokenListener {
    fun onInit()
    fun onAdsTokenExists()
    fun onAdsTokenEmpty()
    fun onCloudTokenExists()
    fun onCloudTokenEmpty()
}

fun Context.checkPurchasesTokens(listener: PurchasesTokenListener) {
    val adsToken = appSettings.getString(KEY_ADS_TOKEN, null)
    val cloudToken = appSettings.getString(KEY_CLOUD_TOKEN, null)
    when {
        adsToken == null || cloudToken == null -> listener.onInit()
        adsToken.isEmpty() -> listener.onAdsTokenEmpty()
        adsToken.isNotEmpty() -> listener.onAdsTokenExists() // здесь выходит, и до след. ветки не доходит
        cloudToken.isEmpty() -> listener.onCloudTokenEmpty()
        cloudToken.isNotEmpty() -> listener.onCloudTokenExists()
    }
}

fun Context.setAdsSetting(token: String) {
    if (token.isNotEmpty()) {
        appSettings.edit().apply {
            putBoolean(getString(R.string.KEY_IS_ADS_ENABLED), false)
            putString(KEY_ADS_TOKEN, token)
        }.apply()
    }
    else {
        appSettings.edit().apply {
            putBoolean(getString(R.string.KEY_IS_ADS_ENABLED), true)
            putString(KEY_ADS_TOKEN, "")
        }.apply()
    }
}

fun Context.setCloudSetting(token: String) {
    if (token.isNotEmpty()) {
        appSettings.edit().apply {
            putBoolean(getString(R.string.KEY_CLOUD_STORAGE), true)
            putString(KEY_CLOUD_TOKEN, token)
        }.apply()
    }
    else {
        appSettings.edit().apply {
            putBoolean(getString(R.string.KEY_CLOUD_STORAGE), false)
            putString(KEY_CLOUD_TOKEN, "")
        }.apply()
    }
}













