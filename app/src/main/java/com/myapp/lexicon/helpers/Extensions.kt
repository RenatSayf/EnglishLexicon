package com.myapp.lexicon.helpers

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.Surface
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.SnackBarTestBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.schedule.AlarmScheduler
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.service.FinishReceiver
import com.myapp.lexicon.service.PhoneUnlockedReceiver.Companion.getInstance
import com.myapp.lexicon.settings.checkUnLockedBroadcast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.setServiceBroadcasts() {

    this.alarmClockEnable()

    checkUnLockedBroadcast(
        onEnabled = {
            val unlockedReceiver = getInstance()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    unlockedReceiver,
                    unlockedReceiver.getFilter(),
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                registerReceiver(
                    unlockedReceiver,
                    unlockedReceiver.getFilter()
                )
            }
        }
    )
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

fun getCalendarMoscowTimeZone(): Calendar {
    return Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
}

val LOCALE_RU: Locale
    get() {
        return Locale("ru", "RU")
    }

fun Long.toStringDate(locale: Locale = LOCALE_RU): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", locale)
    val strDate = formatter.format(this)
    return strDate
}

fun Long.toStringDateDDMonthYYYY(locale: Locale = LOCALE_RU): String {
    val formatter = SimpleDateFormat("dd MMMM yyyy", locale)
    return formatter.format(this)
}

fun String.toLongDate(locale: Locale = LOCALE_RU): Long {
    val formatter = SimpleDateFormat("yyyy-MM-dd", locale)
    val date = try {
        formatter.parse(this)
    } catch (e: ParseException) {
        Date(-1)
    }
    return date.time
}

fun Long.toStringTime(locale: Locale = LOCALE_RU): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
    val strDate = formatter.format(this)
    return strDate
}

fun String.toLongTime(locale: Locale = LOCALE_RU): Long {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
    val date = try {
        formatter.parse(this)
    } catch (e: ParseException) {
        Date(0)
    }
    return date.time
}

fun String.isTodayDate(): Boolean {
    val inputTime = this.toLongDate()
    return inputTime == timeInMillisMoscowTimeZone.toStringDate().toLongDate()
}

fun String.isYesterday(): Boolean {
    val inputTime = this.toLongDate()
    return inputTime == (timeInMillisMoscowTimeZone - TimeUnit.HOURS.toMillis(24)).toStringDate().toLongDate()
}

fun String.dayOfMonthFromStrTime(): Int {
    val monthDay = try {
        MonthDay.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    } catch (e: DateTimeParseException) {
        e.printStackTraceIfDebug()
        return -1
    }
    return monthDay.dayOfMonth
}

fun Long.dayOfMonthFromLongTime(): Int {
    val stringTime = this.toStringTime()
    return stringTime.dayOfMonthFromStrTime()
}

val timeInMillisMoscowTimeZone: Long
    get() {
        return System.currentTimeMillis() + TimeUnit.HOURS.toMillis(3)
    }

fun isTodayFirstDayOfMonth(timeInMillis: Long = timeInMillisMoscowTimeZone): Boolean {
    return timeInMillis.dayOfMonthFromLongTime() == 1
}

fun String.isDateOfLastMonth(currentMonth: Int = timeInMillisMoscowTimeZone.getMonthFromLongTime()): Boolean {
    val yearMonth = try {
        YearMonth.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    } catch (e: DateTimeParseException) {
        e.printStackTraceIfDebug()
        return true
    }
    val inputMonth = yearMonth.monthValue
    return inputMonth < currentMonth
}

fun Long.getMonthFromLongTime(): Int {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
    return dateTime.monthValue
}

fun FragmentActivity.reserveRewardPaymentForMonth(
    user: User,
    onSuccess: (sum: Int, remainder: Double) -> Unit
) {
    val isRewardUpdateOfLastMonth = user.rewardUpdateAt.isDateOfLastMonth()
    val isLastMonth = user.reservedPaymentDate.isDateOfLastMonth()
    if (isRewardUpdateOfLastMonth && isLastMonth) {
        val userVM = ViewModelProvider(this)[UserViewModel::class.java]

        val paymentThreshold: Double = if (!BuildConfig.DEBUG)
            Firebase.remoteConfig.getDouble("payment_threshold") else 0.1
        userVM.updatePayoutDataIntoCloud(
            threshold = (paymentThreshold * user.currencyRate).toInt(),
            reward = user.userReward,
            userMap = mapOf(

            ),
            onStart = {
                this.orientationLock()
            },
            onSuccess = { sum: Int, remainder: Double ->
                onSuccess.invoke(sum, remainder)
            },
            onNotEnough = {},
            onInvalidToken = {},
            onComplete = {exception: Exception? ->
                exception?.printStackTraceIfDebug()
                this.orientationUnLock()
            }
        )
    }
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

fun String.logIfDebug() {
    if (BuildConfig.DEBUG) {
        println("*************** $this *********************")
    }
}

fun Exception.printStackTraceIfDebug() {
    if (BuildConfig.DEBUG) {
        this.printStackTrace()
    }
}

fun Throwable.printStackTraceIfDebug() {
    if (BuildConfig.DEBUG) {
        this.printStackTrace()
    }
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

fun Context.isNetworkAvailable(
    onAvailable: () -> Unit = {},
    onNotAvailable: () -> Unit = {}
): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
            when {
                capabilities != null -> {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            onAvailable.invoke()
                            true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            onAvailable.invoke()
                            true
                        }
                        else -> {

                            val hasTransport = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                            if (hasTransport) onAvailable.invoke() else onNotAvailable.invoke()
                            hasTransport
                        }
                    }
                }
                else -> false
            }
        }
        else -> {
            try {
                val activeNetworkInfo = manager.activeNetworkInfo
                val result = activeNetworkInfo != null && activeNetworkInfo.isConnected
                if (result) onAvailable.invoke() else onNotAvailable.invoke()
                result
            } catch (e: Exception) {
                e.printStackTraceIfDebug()
                onNotAvailable.invoke()
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

fun Context.checkOrientation(
    onLandscape: () -> Unit,
    onPortrait: () -> Unit
) {
    when (this.resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            onLandscape.invoke()
        }
        Configuration.ORIENTATION_PORTRAIT -> {
            onPortrait.invoke()
        }
        else -> {}
    }
}

@SuppressLint("SourceLockedOrientationActivity")
fun FragmentActivity.orientationLock() {
    var rotation = this.windowManager.defaultDisplay.rotation
    when (this.resources.configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_180) {
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        } else {
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        Configuration.ORIENTATION_LANDSCAPE -> {
            rotation = this.windowManager.defaultDisplay.rotation
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                this.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
        }

        Configuration.ORIENTATION_UNDEFINED -> {}
        else -> {}
    }
}

fun FragmentActivity.orientationUnLock() {
    this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

fun String.firstCap() = this.lowercase().replaceFirstChar {
    it.uppercase()
}

fun String.checkIfAllDigits(): Boolean {
    return this.all {
        it.code in 33..64
    }
}
























