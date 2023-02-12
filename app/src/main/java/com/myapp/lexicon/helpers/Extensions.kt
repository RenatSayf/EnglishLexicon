package com.myapp.lexicon.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment

private const val APP_SETTINGS = "APP_SETTINGS"

val Context.appPref: SharedPreferences
    get() {
        return this.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
    }

val Fragment.appPref: SharedPreferences
    get() {
        return requireContext().appPref
    }