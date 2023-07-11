package com.myapp.lexicon.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.SnackBarTestBinding
import com.myapp.lexicon.schedule.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.TemporalAccessor
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.CRC32
import kotlin.math.roundToInt


fun Context.alarmClockEnable() {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    val minutesStr = preferences.getString(getString(R.string.key_show_intervals), "0")
    val minutesLong = minutesStr!!.toLong()
    if (minutesLong > 0) {
        val millis = TimeUnit.MINUTES.toMillis(minutesLong)
        AlarmScheduler(this).scheduleOne(millis)
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

fun String.getCRC32CheckSum(): Long {
    val crC32 = CRC32()
    crC32.update(this.toByteArray())
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

fun BroadcastReceiver.goAsync(
    coroutineScope: CoroutineScope,
    block: suspend (CoroutineScope) -> Unit
) {
    val pendingResult = goAsync()
    coroutineScope.launch(coroutineScope.coroutineContext) {
        block.invoke(this)
        pendingResult.finish()
    }
}

val Context.screenWidth: Int
    get() = resources.displayMetrics.run { widthPixels / density }.roundToInt()

val Context.screenHeight: Int
    get() = resources.displayMetrics.run { heightPixels / density }.roundToInt()

val String.isItEmail: Boolean
    get() {
        return !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

@Suppress("UnnecessaryVariable")
fun Long.toStringDate(locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", locale)
    val strDate = formatter.format(this)
    return strDate
}

fun String.toLongDate(locale: Locale = Locale.getDefault()): Long {
    val formatter = SimpleDateFormat("yyyy-MM-dd", locale)
    val date = try {
        formatter.parse(this)
    } catch (e: ParseException) {
        Date(-1)
    }
    return date.time
}

fun View.showCustomSnackBar(
    message: String,
    onLaunch: (SnackBarTestBinding) -> Unit = {}
) {
    val snackBar = Snackbar.make(this, "", Snackbar.LENGTH_LONG)
    val binding = SnackBarTestBinding.inflate(LayoutInflater.from(this.context), null, false)

    snackBar.setBackgroundTint(ResourcesCompat.getColor(this.resources, R.color.colorTransparent, null))
    val layoutParams = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
    layoutParams.setMargins(16, 0, 16, 16)
    layoutParams.height = LayoutParams.WRAP_CONTENT
    snackBar.view.layoutParams = layoutParams
    binding.tvMessage.text = message

    (snackBar.view as ViewGroup).apply {
        removeAllViews()
        addView(binding.root)
    }
    onLaunch.invoke(binding)
    snackBar.show()
}























