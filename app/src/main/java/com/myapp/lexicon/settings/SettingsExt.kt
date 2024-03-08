@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.myapp.lexicon.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.models.TestState
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.models.toWord
import kotlinx.serialization.SerializationException

val Context.appSettings: SharedPreferences
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

val Fragment.appSettings: SharedPreferences
    get() {
        return requireContext().appSettings
    }

fun Context.saveUserToPref(user: User) {
    appSettings.edit().apply {
        putString("KEY_EMAIL", user.email)
        putString("KEY_PASSWORD", user.password)
        putBoolean("KEY_IS_REGISTERED", true)
    }.apply()
}

fun Context.clearEmailPasswordInPref() {
    appSettings.edit().apply {
        putString("KEY_EMAIL", null).apply()
        putString("KEY_PASSWORD", null).apply()
        putBoolean("KEY_IS_REGISTERED", false).apply()
    }.apply()

}

fun Context.getAuthDataFromPref(
    onNotRegistered: () -> Unit = {},
    onSuccess: (email: String, password: String) -> Unit = {_,_ ->},
    onFailure: (Exception) -> Unit = {}
) {
    val email = appSettings.getString("KEY_EMAIL", null)
    val password = appSettings.getString("KEY_PASSWORD", null)
    try {
        if (email == null || password == null) {
            onNotRegistered.invoke()
        }
        else if (email.isNotEmpty() && password.isNotEmpty()) {
            onSuccess.invoke(email, password)
        }
        else {
            onNotRegistered.invoke()
        }
    } catch (e: Exception) {
        onFailure.invoke(e)
    }
}

fun Context.isUserRegistered(
    onYes: () -> Unit,
    onNotRegistered: () -> Unit = {}
) {
    val result = appSettings.getBoolean("KEY_IS_REGISTERED", false)
    if (result) {
        onYes.invoke()
    }
    else {
        onNotRegistered.invoke()
    }
}

fun Context.isFirstLogin(
    onYes: () -> Unit = {},
    onNotFirst: () -> Unit = {}
) {
    val key = "KEY_IS_FIRST_LOGIN"
    val isFirst = appSettings.getBoolean(key, true)
    if (isFirst) {
        onYes.invoke()
        appSettings.edit().putBoolean(key, false).apply()
    }
    else {
        onNotFirst.invoke()
    }
}

var Context.adsIsEnabled: Boolean
    get() {
        return appSettings.getBoolean(getString(R.string.KEY_IS_ADS_ENABLED), true)
    }
    set(value) {
        appSettings.edit().putBoolean(getString(R.string.KEY_IS_ADS_ENABLED), value).apply()
    }

val Fragment.adsIsEnabled: Boolean
    get() {
        return requireContext().adsIsEnabled
    }

var Context.checkFirstLaunch: Boolean
    get() {
        return appSettings.getBoolean(getString(R.string.KEY_FIRST_LAUNCH), true)
    }
    set(value) {
        appSettings.edit().putBoolean(getString(R.string.KEY_FIRST_LAUNCH), value).apply()
    }

fun Context.checkOnStartSpeech(onEnabled: () -> Unit, onDisabled: () -> Unit) {
    val isSpeech = appSettings.getBoolean(getString(R.string.KEY_ON_START_SPEECH), true)
    when(isSpeech) {
        true -> onEnabled.invoke()
        false -> onDisabled.invoke()
    }
}

fun Context.saveWordToPref(word: Word?, bookmark: Int) {
    if (word != null) {
        val strToSave = word.toString()
        appSettings.edit().putString("KEY_CURRENT_WORD", strToSave).apply()
        appSettings.edit().putInt("KEY_BOOKMARK", bookmark).apply()
    }
    else {
        appSettings.edit().putString("KEY_CURRENT_WORD", null).apply()
        appSettings.edit().putInt("KEY_BOOKMARK", -1).apply()
    }
}

fun Context.getWordFromPref(
    onInit: () -> Unit = {},
    onSuccess: (Word, Int) -> Unit = {_,_->},
    onFailure: (Exception) -> Unit = {}
) {
    val string = appSettings.getString("KEY_CURRENT_WORD", null)
    val bookmark = appSettings.getInt("KEY_BOOKMARK", -1)
    string?.let {
        try {
            val word = try {
                it.toWord()
            } catch (e: SerializationException) {
                null
            }
            if (word != null) {
                onSuccess.invoke(word, bookmark)
            }
            else onInit.invoke()
        }
        catch (e: Exception) {
            onFailure.invoke(e)
        }
    }?: run {
        onInit.invoke()
    }
}

fun Context.checkUnLockedBroadcast(onEnabled: () -> Unit, onDisabled: () -> Unit) {
    val isEnabled = appSettings.getBoolean(getString(R.string.key_service), false)
    when(isEnabled) {
        true -> onEnabled.invoke()
        false -> onDisabled.invoke()
    }
}


private const val LEARNING_MODE_VALUE = 5

val Context.testIntervalFromPref: Int
    get() {
        return try {
            appSettings.getString(getString(R.string.key_test_interval), LEARNING_MODE_VALUE.toString())?.toInt()?: LEARNING_MODE_VALUE
        } catch (e: NumberFormatException) {
            LEARNING_MODE_VALUE
        }
    }

fun Context.disablePassiveWordsRepeat(
    onDisabled: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        appSettings.edit().putString(getString(R.string.key_show_intervals), "0").apply()
        appSettings.edit().putBoolean(getString(R.string.key_service), false).apply()
        onDisabled.invoke()
    } catch (e: Exception) {
        onError.invoke(e.message?: "Unknown error")
        e.printStackTrace()
    }
}

fun Fragment.disablePassiveWordsRepeat(
    onDisabled: () -> Unit,
    onError: (String) -> Unit
) {
    requireContext().disablePassiveWordsRepeat(onDisabled, onError)
}

fun Fragment.saveTestStateToPref(state: TestState?) {
    if (state != null) {
        appSettings.edit().apply {
            putString(TestState.KEY_DICTIONARY, state.dict)
            putInt(TestState.KEY_WORD_ID, state.wordId)
            putInt(TestState.KEY_PROGRESS, state.progress)
            putInt(TestState.KEY_PROGRESS_MAX, state.progressMax)
            putInt(TestState.KEY_RIGHT_ANSWERS, state.rightAnswers)
            val stringIds = state.studiedWordIds.map { item ->
                item.toString()
            }.toSet()
            putStringSet(TestState.KEY_STUDIED_WORD_IDS, stringIds)
        }.apply()
    } else {
        appSettings.edit().apply {
            putString(TestState.KEY_DICTIONARY, null)
            putInt(TestState.KEY_WORD_ID, 0)
            putInt(TestState.KEY_PROGRESS, 0)
            putInt(TestState.KEY_PROGRESS_MAX, Int.MAX_VALUE)
            putInt(TestState.KEY_RIGHT_ANSWERS, 0)
            putStringSet(TestState.KEY_STUDIED_WORD_IDS, null)
        }.apply()
    }
}

fun Fragment.getTestStateFromPref(
    onInit: () -> Unit = {},
    onSuccess: (TestState) -> Unit,
    onError: (String?) -> Unit = {}
    ) {

    val dict = appSettings.getString(TestState.KEY_DICTIONARY, null)
    val stringSet = appSettings.getStringSet(TestState.KEY_STUDIED_WORD_IDS, null)

    if (dict == null || stringSet == null) {
        onInit.invoke()
        return
    }
    else {
        try {
            val studiedIds = stringSet.map {
                it.toInt()
            }
            val testState = TestState(
                dict = dict,
                wordId = appSettings.getInt(TestState.KEY_WORD_ID, 0),
                progress = appSettings.getInt(TestState.KEY_PROGRESS, 0),
                progressMax = appSettings.getInt(TestState.KEY_PROGRESS_MAX, Int.MAX_VALUE),
                rightAnswers = appSettings.getInt(TestState.KEY_RIGHT_ANSWERS, 0),
                studiedWordIds = studiedIds.toMutableList()
            )
            onSuccess.invoke(testState)
        } catch (e: Exception) {
            onError.invoke(e.message)
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }
}

fun AppCompatActivity.askForPermission(
    permission: String,
    onInit: () -> Unit,
    onGranted: () -> Unit
) {
    val isShould = this.shouldShowRequestPermissionRationale(permission)
    val selfPermission = ContextCompat.checkSelfPermission(this, permission)
    if (selfPermission == PackageManager.PERMISSION_GRANTED) {
        onGranted.invoke()
    }
    else if (selfPermission == PackageManager.PERMISSION_DENIED || isShould) {
        onInit.invoke()
    }
}

fun Fragment.askForPermission(
    permission: String,
    onInit: () -> Unit = {},
    onGranted: () -> Unit = {}
) {
    (requireActivity() as AppCompatActivity).askForPermission(
        permission,
        onInit,
        onGranted
    )
}

fun Context.getNotificationMode(
    onRepeatMode: () -> Unit = {},
    onTestMode: () -> Unit = {}
) {
    val mode = appSettings.getString(getString(R.string.key_list_display_mode), "0")
    when(mode) {
        "0" -> onRepeatMode.invoke()
        "1" -> onTestMode.invoke()
    }
}

fun Context.checkPassiveModeEnabled(): Boolean {
    val interval = appSettings.getString(getString(R.string.key_show_intervals), "10")
    val isUnlockedScreen = appSettings.getBoolean(getString(R.string.key_service), false)
    return interval != "0" || isUnlockedScreen
}

fun Context.goToAppStore() {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(this.getString(R.string.app_link).plus(this.packageName))
    startActivity(intent)
}

fun Context.saveOrderPlay(order: Int) {
    appSettings.edit().putInt("KEY_ORDER_PLAY", order).apply()
}

fun Context.getOrderPlay(
    onCycle: (Int) -> Unit = {},
    onRandom: () -> Unit = {}
): Int {
    val value = appSettings.getInt("KEY_ORDER_PLAY", 0)
    if (value == 0 || value == 1) {
        onCycle.invoke(value)
    }
    else {
        onRandom.invoke()
    }
    return value
}
val Context.orderPlayFromPref: Int
    get() {
        return this.getOrderPlay()
    }

var Context.isEngSpeech: Boolean
    get() {
        return appSettings.getBoolean("KEY_ENG_SPEECH", true)
    }
    set(value) {
        appSettings.edit().putBoolean("KEY_ENG_SPEECH", value).apply()
    }

var Context.isRuSpeech: Boolean
    get() {
        return appSettings.getBoolean("KEY_RUS_SPEECH", false)
    }
    set(value) {
        appSettings.edit().putBoolean("KEY_RUS_SPEECH", value).apply()
    }












