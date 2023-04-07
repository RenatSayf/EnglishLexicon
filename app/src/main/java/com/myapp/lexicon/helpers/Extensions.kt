package com.myapp.lexicon.helpers

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.myapp.lexicon.R
import com.myapp.lexicon.schedule.AlarmScheduler
import java.io.File

private const val APP_SETTINGS = "APP_SETTINGS"

val Context.appPref: SharedPreferences
    get() {
        return this.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
    }

val Fragment.appPref: SharedPreferences
    get() {
        return requireContext().appPref
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

fun Fragment.fileToBytes(file: File): ByteArray {
    return file.readBytes()
}

