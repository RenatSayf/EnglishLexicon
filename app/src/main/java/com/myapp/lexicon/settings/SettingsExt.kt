@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.myapp.lexicon.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.models.Word

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
    if (adsToken == null || cloudToken == null) {
        listener.onInit()
        return
    }
    if (adsToken.isEmpty()) {
        listener.onAdsTokenEmpty()
    }
    if (adsToken.isNotEmpty()) {
        listener.onAdsTokenExists()
    }
    if (cloudToken.isEmpty()) {
        listener.onCloudTokenEmpty()
    }
    if (cloudToken.isNotEmpty()) {
        listener.onCloudTokenExists()
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

fun Context.checkOnStartSpeech(onEnabled: () -> Unit, onDisabled: () -> Unit) {
    val isSpeech = appSettings.getBoolean(getString(R.string.KEY_ON_START_SPEECH), true)
    when(isSpeech) {
        true -> onEnabled.invoke()
        false -> onDisabled.invoke()
    }
}

fun Context.saveWordToPref(word: Word) {
    val json = Gson().toJson(word)
    appSettings.edit().putString("KEY_CURRENT_WORD", json).apply()
}

fun Context.getWordFromPref(onInit: () -> Unit, onSuccess: (Word) -> Unit, onFailure: (Exception) -> Unit) {
    val string = appSettings.getString("KEY_CURRENT_WORD", null)
    string?.let {
        try {
            val word = Gson().fromJson(it, Word::class.java)
            onSuccess.invoke(word)
        } catch (e: Exception) {
            onFailure.invoke(e)
        }
    }?: run {
        onInit.invoke()
    }
}

fun Context.checkUnLockedBroadcast(onEnabled: () -> Unit, onDisabled: () -> Unit) {
    val isEnabled = appSettings.getBoolean(getString(R.string.key_service), true)
    when(isEnabled) {
        true -> onEnabled.invoke()
        false -> onDisabled.invoke()
    }
}

var Context.isRequireCloudSync: Boolean
    get() {
        return appSettings.getBoolean("KEY_CLOUD_SYNC", false)
    }
    set(value) {
        appSettings.edit().putBoolean("KEY_CLOUD_SYNC", value).apply()
    }













