package com.myapp.lexicon.helpers

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.SnackBarTestBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.schedule.AlarmScheduler
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.service.FinishReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
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

fun Context.showToastIfDebug(message: String?) {
    if (BuildConfig.DEBUG) {
        val text = message ?: "*********** Unknown error ***************"
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}

fun Fragment.showToastIfDebug(message: String?) {
    requireContext().showToastIfDebug(message)
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

@Suppress("UnnecessaryVariable")
fun Long.toStringTime(locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
    val strDate = formatter.format(this)
    return strDate
}

fun String.toLongTime(locale: Locale = Locale.getDefault()): Long {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
    val date = try {
        formatter.parse(this)
    } catch (e: ParseException) {
        Date(0)
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

fun<T> Context.checkIsActivityShown(
    clazz: Class<T>,
    onVisible: () -> Unit = {},
    onInvisible: () -> Unit = {}
) {
    val activityManager = (this.getSystemService(Service.ACTIVITY_SERVICE)) as ActivityManager?
    val tasks = activityManager?.appTasks
    tasks?.forEach { t ->
        if (clazz.canonicalName.equals(t.taskInfo.baseActivity?.className, true)) {
            onVisible.invoke()
            return
        }
    }
    onInvisible.invoke()
}

fun Context.showDebugNotification(message: String?) {
    val notification = AppNotification(this)
    notification.create(message?: "Unknown error")
    notification.show()
}

fun View.setBackground(resId: Int) {
    this.background = ResourcesCompat.getDrawable(resources, resId, null)
}

fun printLogIfDebug(message: String) {
    if (BuildConfig.DEBUG) {
        println("*************** $message *********************")
    }
}

fun Exception.printStackTraceIfDebug() {
    if (BuildConfig.DEBUG) {
        this.printStackTrace()
    }
}

fun Throwable.printStackTraceIfDebug() {
    (this as Exception).printStackTraceIfDebug()
}

fun Exception.throwIfDebug() {
    if (BuildConfig.DEBUG) {
        throw this
    }
}

fun Throwable.throwIfDebug() {
    if (BuildConfig.DEBUG) {
        throw this
    }
}

fun Context.isNetworkAvailable(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
            when {
                capabilities != null -> {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        else -> capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    }
                }
                else -> false
            }
        }
        else -> {
            try {
                val activeNetworkInfo = manager.activeNetworkInfo
                activeNetworkInfo != null && activeNetworkInfo.isConnected
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) e.printStackTrace()
                false
            }
        }
    }
}

fun List<Word>.findItemWithoutRemainder(
    divider: Int,
    isRemainder: () -> Unit = {},
    noRemainder: () -> Unit = {}
) {
    val result = this.any {
        it._id % divider == 0
    }
    if (result) {
        noRemainder.invoke()
    }
    else {
        isRemainder.invoke()
    }
}

fun AppCompatActivity.showSignUpBenefitsDialog(
    onPositiveClick: () -> Unit,
    onCancel: () -> Unit = {}
) {
    val explainMessage = Firebase.remoteConfig.getString("sign_up_benefits_message")
    if (explainMessage.isNotEmpty()) {
        ConfirmDialog.newInstance(onLaunch = { dialog, binding ->
            with(binding) {
                dialog.isCancelable = false
                ivIcon.visibility = View.INVISIBLE
                tvEmoji2.visibility = View.GONE
                tvEmoji.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.coins_bag)
                }
                tvMessage.text = explainMessage
                btnCancel.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        dialog.dismiss()
                        onCancel.invoke()
                    }
                }
                btnOk.apply {
                    text = this@showSignUpBenefitsDialog.getString(R.string.text_go_over)
                }.setOnClickListener {
                    dialog.dismiss()
                    onPositiveClick.invoke()
                }
            }
        }).show(this.supportFragmentManager, ConfirmDialog.TAG)
    }
}

fun List<Word>.checkSorting(
    onASC: () -> Unit = {},
    onDESC: () -> Unit = {},
    onNotSorted: () -> Unit = {}
) {
    var res = this.zipWithNext { a, b ->
        a.english.lowercase() <= b.english.lowercase()
    }.all { it }
    if (res) {
        onASC.invoke()
        return
    }

    res = this.zipWithNext { a, b ->
        a.english.lowercase() >= b.english.lowercase()
    }.all { it }
    if (res) {
        onDESC.invoke()
        return
    }
    onNotSorted.invoke()
}

fun List<Word>.checkSorting(): Int {
    var result = -1
    this.checkSorting(
        onASC = {
            result = 0
        },
        onDESC = {
            result = 1
        },
        onNotSorted = {
            result = 2
        }
    )
    return result
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerFinishReceiver(receiver: FinishReceiver) {

    val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
    }
    else {
        this.registerReceiver(receiver, intentFilter)
    }
}

val Float.toPx get() = this * Resources.getSystem().displayMetrics.density

val Float.toDp get() = this / Resources.getSystem().displayMetrics.density

val Int.toPx get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.toDp get() = (this / Resources.getSystem().displayMetrics.density).toInt()






















