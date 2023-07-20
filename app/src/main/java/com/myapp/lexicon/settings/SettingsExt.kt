@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.myapp.lexicon.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.helpers.getCRC32CheckSum
import com.myapp.lexicon.models.LaunchMode
import com.myapp.lexicon.models.TestState
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.models.currency.Currency
import java.util.Locale

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
        putString("KEY_USER_ID", user.id)
        putString("KEY_EMAIL", user.email)
        putString("KEY_PASSWORD", user.password)
        putBoolean("KEY_IS_REGISTERED", true)
    }.apply()
}

fun Context.clearEmailPasswordInPref() {
    appSettings.edit().putString("KEY_USER_ID", null).apply()
    appSettings.edit().putString("KEY_EMAIL", null).apply()
    appSettings.edit().putString("KEY_PASSWORD", null).apply()
    appSettings.edit().putBoolean("KEY_IS_REGISTERED", false).apply()
}

fun Context.getAuthDataFromPref(
    onNotRegistered: () -> Unit,
    onSuccess: (id: String, email: String, password: String) -> Unit,
    onFailure: (Exception) -> Unit = {}
) {
    val id = appSettings.getString("KEY_USER_ID", null)
    val email = appSettings.getString("KEY_EMAIL", null)
    val password = appSettings.getString("KEY_PASSWORD", null)
    try {
        if (id == null || email == null || password == null) {
            onNotRegistered.invoke()
        }
        else if (id.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            onSuccess.invoke(id, email, password)
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

var Context.cloudStorageEnabled: Boolean
    get() {
        return appSettings.getBoolean(getString(R.string.KEY_CLOUD_STORAGE), false)
    }
    private set(value) {
        appSettings.edit().putBoolean(getString(R.string.KEY_CLOUD_STORAGE), value).apply()
    }

val Fragment.cloudStorageEnabled: Boolean
    get() {
        return requireContext().cloudStorageEnabled
    }

var Context.checkFirstLaunch: Boolean
    get() {
        return appSettings.getBoolean(getString(R.string.KEY_FIRST_LAUNCH), true)
    }
    set(value) {
        appSettings.edit().putBoolean(getString(R.string.KEY_FIRST_LAUNCH), value).apply()
    }

private const val KEY_ADS_TOKEN = "KEY_ADS_TOKEN_555"
private const val KEY_CLOUD_TOKEN = "KEY_CLOUD_TOKEN_777"

interface PurchasesTokenListener {
    fun onInit()
    fun onCheckComplete()
}

fun Context.checkPurchasesTokens(listener: PurchasesTokenListener) {
    val adsToken = appSettings.getString(KEY_ADS_TOKEN, null)
    val cloudToken = appSettings.getString(KEY_CLOUD_TOKEN, null)
    try {
        if (adsToken == null || cloudToken == null) {
            listener.onInit()
            return
        }
        this.adsIsEnabled = adsToken.isEmpty()
        this.cloudStorageEnabled = cloudToken.isNotEmpty()
    } finally {
        listener.onCheckComplete()
    }
}

fun Context.checkCloudToken(
    onInit: () -> Unit = {},
    onExists: (String) -> Unit,
    onEmpty: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val token = appSettings.getString(KEY_CLOUD_TOKEN, null)
    try {
        when {
            token == null -> onInit.invoke()
            token.isNotEmpty() -> onExists.invoke(token)
            else -> onEmpty.invoke()
        }
    } finally {
        onComplete.invoke()
    }
}

fun Context.setAdsSetting(token: String?) {

    when {
        token == null -> {
            appSettings.edit().apply {
                putBoolean(getString(R.string.KEY_IS_ADS_ENABLED), true)
                putString(KEY_ADS_TOKEN, null)
            }.apply()
            this.adsIsEnabled = true
        }
        token.isEmpty() -> {
            appSettings.edit().apply {
                putBoolean(getString(R.string.KEY_IS_ADS_ENABLED), true)
                putString(KEY_ADS_TOKEN, "")
            }.apply()
            this.adsIsEnabled = true
        }
        else -> {
            val tokenCheckSum = token.getCRC32CheckSum()
            appSettings.edit().apply {
                putBoolean(getString(R.string.KEY_IS_ADS_ENABLED), false)
                putString(KEY_ADS_TOKEN, tokenCheckSum.toString())
            }.apply()
            this.adsIsEnabled = false
        }
    }
}

fun Context.setCloudSetting(token: String?) {
    when {
        token == null -> {
            appSettings.edit().apply {
                putBoolean(getString(R.string.KEY_CLOUD_STORAGE), false)
                putString(KEY_CLOUD_TOKEN, null)
            }.apply()
            this.cloudStorageEnabled = false
        }
        token.isEmpty() -> {
            appSettings.edit().apply {
                putBoolean(getString(R.string.KEY_CLOUD_STORAGE), false)
                putString(KEY_CLOUD_TOKEN, "")
            }.apply()
            this.cloudStorageEnabled = false
        }
        else -> {
            appSettings.edit().apply {
                putBoolean(getString(R.string.KEY_CLOUD_STORAGE), true)
                putString(KEY_CLOUD_TOKEN, token.getCRC32CheckSum().toString())
            }.apply()
            this.cloudStorageEnabled = true
        }
    }
}

fun Context.checkOnStartSpeech(onEnabled: () -> Unit, onDisabled: () -> Unit) {
    val isSpeech = appSettings.getBoolean(getString(R.string.KEY_ON_START_SPEECH), true)
    when(isSpeech) {
        true -> onEnabled.invoke()
        false -> onDisabled.invoke()
    }
}

fun Context.saveWordToPref(word: Word) {
    val json = Gson().toJson(word)
    appSettings.edit().putString("KEY_CURRENT_WORD", json).apply()
}

fun Context.getWordFromPref(onInit: () -> Unit, onSuccess: (Word) -> Unit, onFailure: (Exception) -> Unit) {
    val string = appSettings.getString("KEY_CURRENT_WORD", null)
    string?.let {
        try {
            val word = Gson().fromJson(it, Word::class.java)
            onSuccess.invoke(word)
        } catch (e: Exception) {
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

fun Context.saveInitDbCheckSum(sum: Long) {
    val long = appSettings.getLong("KEY_INIT_DB_CHECK_SUM", 0)
    if (long == 0L) {
        appSettings.edit().putLong("KEY_INIT_DB_CHECK_SUM", sum).apply()
    }
}

val Context.initDbCheckSum: Long
    get() {
        return appSettings.getLong("KEY_INIT_DB_CHECK_SUM", 0L)
    }

fun Context.checkCloudStorage(
    userId: String,
    fileName: String = getString(R.string.data_base_name),
    onRequireUpSync: (String) -> Unit,
    onRequireDownSync: (String) -> Unit,
    onNotRequireSync: () -> Unit
) {
    val dbFile = getDatabasePath(fileName)
    val localCheckSum = dbFile.readBytes().getCRC32CheckSum()
    if (BuildConfig.DEBUG) {
        println("************** localCheckSum = $localCheckSum ****************")
        println("************** initDbCheckSum = ${this.initDbCheckSum} ****************")
    }

    val storageRef: StorageReference = Firebase.storage.reference.child("/users/$userId/${fileName}")

    storageRef.metadata.addOnSuccessListener { metadata ->

        val remoteCheckSum = metadata.getCustomMetadata("CHECK_SUM") ?: "0"
        if (BuildConfig.DEBUG) {
            println("************** remoteCheckSum = $remoteCheckSum ****************")
        }

        if (localCheckSum.toString() != remoteCheckSum && localCheckSum == this.initDbCheckSum) {
            onRequireDownSync.invoke(userId)
        }
        else if (localCheckSum.toString() != remoteCheckSum && localCheckSum != this.initDbCheckSum) {
            onRequireUpSync.invoke(userId)
        }
        else {
            onNotRequireSync.invoke()
        }
    }.addOnFailureListener { ex ->

        if (ex is StorageException) {
            if (ex.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                if (localCheckSum != this.initDbCheckSum) {
                    onRequireUpSync.invoke(userId)
                    return@addOnFailureListener
                }
                else {
                    onNotRequireSync.invoke()
                    return@addOnFailureListener
                }
            }
            else {
                if (BuildConfig.DEBUG) ex.printStackTrace()
            }
        }
        onNotRequireSync.invoke()
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

fun Context.checkBuildConfig(
    onInit: () -> Unit,
    onChangeToTest: (String) -> Unit,
    onChangeToNormal: (String) -> Unit
) {
    val key = "KEY_LAUNCH_MODE"
    val savedMode = appSettings.getString(key, null)
    val currentMode = BuildConfig.PURCHASE_MODE
    if (savedMode == null) {
        onInit.invoke()
        appSettings.edit().putString(key, currentMode).apply()
    }
    if (savedMode != currentMode) {
        when(currentMode) {
            LaunchMode.TEST.name -> {
                onChangeToTest.invoke(currentMode)
            }
            LaunchMode.NORMAL.name -> {
                onChangeToNormal.invoke(currentMode)
            }
        }
        appSettings.edit().putString(key, currentMode).apply()
    }
}

fun Context.saveExchangeRateToPref(currency: Currency) {
    appSettings.edit().putString("KEY_EXCHANGE_DATE", currency.date).apply()
    appSettings.edit().putFloat("KEY_EXCHANGE_RATE", currency.rate.toFloat()).apply()
    val instance = android.icu.util.Currency.getInstance(Locale.getDefault())
    val symbol = instance.symbol
    appSettings.edit().putString("KEY_CURRENCY_SYMBOL", symbol).apply()
}

fun Context.getExchangeRateFromPref(
    onInit: () -> Unit,
    onSuccess: (date: String, symbol: String, rate: Double) -> Unit,
    onFailure: (Exception) -> Unit = {}
) {
    try {
        val date = appSettings.getString("KEY_EXCHANGE_DATE", null)
        val symbol = appSettings.getString("KEY_CURRENCY_SYMBOL", null)
        val rate = appSettings.getFloat("KEY_EXCHANGE_RATE", -1.0F).toDouble()
        if (date == null || symbol == null || rate < 0) {
            onInit.invoke()
            return
        }
        else if (rate > 0) {
            onSuccess.invoke(date, symbol, rate)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onFailure.invoke(e)
    }
}

fun AppCompatActivity.askForPermission(
    permission: String,
    onInit: () -> Unit,
    onGranted: (Boolean) -> Unit,
    onRejected: () -> Unit,
    isRationale: Boolean = true
) {
    if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
        onGranted.invoke(true)
    }
    else if (this.shouldShowRequestPermissionRationale(permission) || isRationale) {
        onInit.invoke()
    }
    else {
        this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                onGranted.invoke(true)
            }
            else {
                onRejected.invoke()
            }
        }.launch(permission)
    }
}

fun Fragment.askForPermission(
    permission: String,
    onInit: () -> Unit = {},
    onGranted: (Boolean) -> Unit = {},
    onRejected: () -> Unit = {},
    isRationale: Boolean = true
) {
    (requireActivity() as AppCompatActivity).askForPermission(
        permission,
        onInit,
        onGranted,
        onRejected
    )
}











