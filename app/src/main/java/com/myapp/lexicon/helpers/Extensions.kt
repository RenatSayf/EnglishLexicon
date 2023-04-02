package com.myapp.lexicon.helpers

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.myapp.lexicon.R
import com.myapp.lexicon.schedule.AlarmScheduler

private const val APP_SETTINGS = "APP_SETTINGS"
private const val NO_ADS_TOKEN = "NO_ADS_TOKEN"
private const val CLOUD_TOKEN = "CLOUD_TOKEN"

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

val Context.noAdsToken: String?
    get() {
        return appPref.getString(NO_ADS_TOKEN, null)
    }

val Fragment.noAdsToken: String?
    get() {
        return requireContext().noAdsToken
    }

fun Context.checkAdsToken(
    init: () -> Unit = {},
    noToken: () -> Unit,
    hasToken: () -> Unit = {}
) {
    noAdsToken?.let { token ->
        if (token.isEmpty()) noToken.invoke()
        else hasToken.invoke()
    }?: run {
        init.invoke()
    }
}

fun Fragment.checkAdsToken(init: () -> Unit = {}, noToken: () -> Unit, hasToken: () -> Unit = {}) {
    requireContext().checkAdsToken(init, noToken, hasToken)
}

fun Context.saveCloudToken(token: String) {
    appPref.edit().putString(CLOUD_TOKEN, token).apply()
}

fun Fragment.saveCloudToken(token: String) {
    requireContext().saveCloudToken(token)
}

fun Context.checkCloudToken(hasToken: () -> Unit, noToken: () -> Unit) {
    val token = appPref.getString(CLOUD_TOKEN, null)
    token?.let {
        if (it.isNotEmpty()) {
            hasToken.invoke()
        }
        else noToken.invoke()
    }?: run {
        noToken.invoke()
    }
}

fun Fragment.checkCloudToken(hasToken: () -> Unit, noToken: () -> Unit) {
    requireContext().checkCloudToken(hasToken, noToken)
}

fun Context.alarmClockEnable() {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    val interval = preferences.getString(getString(R.string.key_show_intervals), "0")
    val parseInt = interval!!.toInt()
    if (parseInt > 0) {
        AlarmScheduler(this).scheduleOne(parseInt.toLong() * 60 * 1000)
    }
}

fun Fragment.alarmClockEnable() {
    requireContext().alarmClockEnable()
}

fun View.showSnackBar(message: String, duration: Int = Snackbar.LENGTH_LONG): Snackbar {
    return Snackbar.make(this, message, duration).apply {
        show()
    }
}

fun Fragment.showSnackBar(message: String, duration: Int = Snackbar.LENGTH_LONG): Snackbar {
    return requireView().showSnackBar(message, duration)
}

