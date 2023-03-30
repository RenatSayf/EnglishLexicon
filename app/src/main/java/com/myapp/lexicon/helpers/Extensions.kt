package com.myapp.lexicon.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.myapp.lexicon.R
import com.myapp.lexicon.schedule.AlarmScheduler

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

private const val DB_REF = "DB_REF"

fun Context.saveCloudDbRefToPref(ref: String) {
    appPref.edit().putString(DB_REF, ref).apply()
}

fun Context.getCloudDbRefFromPref(onSuccess: (String) -> Unit) {
    val ref = appPref.getString(DB_REF, null)
    ref?.let {
        onSuccess.invoke(it)
    }
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

