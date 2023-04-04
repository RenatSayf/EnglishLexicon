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
