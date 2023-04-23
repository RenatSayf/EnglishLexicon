package com.myapp.lexicon.helpers

import android.content.Context
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
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

fun File.fileToBytes(): ByteArray {
    return this.readBytes()
}

fun<T: DialogFragment> AppCompatActivity.showDialogAsSingleton(dialog: T, tag: String) {
    val fragment = this.supportFragmentManager.findFragmentByTag(dialog.tag)
    if (fragment == null) {
        dialog.show(this.supportFragmentManager, tag)
    }
}

fun startTimer(
    time: Long,
    interval: Long = time,
    onTick: () -> Unit = {},
    onFinish: () -> Unit
): CountDownTimer {

    return object : CountDownTimer(time, interval) {
        override fun onTick(millisUntilFinished: Long) {
            onTick.invoke()
        }

        override fun onFinish() {
            onFinish.invoke()
        }
    }.start()
}



