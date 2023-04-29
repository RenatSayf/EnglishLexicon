package com.myapp.lexicon.helpers

import android.content.Context
import android.os.CountDownTimer
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.myapp.lexicon.R
import com.myapp.lexicon.schedule.AlarmScheduler
import java.io.File
import java.util.zip.CRC32

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

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
    requireContext().showToast(message, duration)
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

fun ByteArray.getCRC32CheckSum(): Long {
    val crC32 = CRC32()
    crC32.update(this)
    return crC32.value
}

fun String.checkOnlyLetterAndFirstNotDigit(): Int {
    if (this.isNotEmpty() && this[0].isDigit()) {
        return 0
    }
    return try {
        val first = this.first {
            !it.isLetterOrDigit() && !it.isWhitespace()
        }
        val indexOf = this.indexOf(first)
        //return this.replace(Regex("^[0-9]|\\W]*"), "")
        indexOf
    } catch (e: Exception) {
        -1
    }
}

private val Context.inputManager: InputMethodManager
    get() {
        return this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

fun View.showKeyboard() {
    this.context.inputManager.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    this.context.inputManager.hideSoftInputFromWindow(this.windowToken, 0)
}