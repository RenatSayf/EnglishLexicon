package com.myapp.lexicon.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.myapp.lexicon.main.MainActivity

private const val APP_SETTINGS = "APP_SETTINGS"
private const val PURCHASED_TOKEN = "PURCHASED_TOKEN"

val Context.appPref: SharedPreferences
    get() {
        return this.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
    }

val Fragment.appPref: SharedPreferences
    get() {
        return requireContext().appPref
    }

fun Context.saveToken(token: String) {
    appPref.edit().putString(PURCHASED_TOKEN, token).apply()
}

fun Fragment.saveToken(token: String) {
    requireContext().saveToken(token)
}

val Context.purchasedToken: String
    get() {
        return appPref.getString(PURCHASED_TOKEN, null)?: ""
    }

val Fragment.purchasedToken: String
    get() {
        return requireContext().purchasedToken
    }
