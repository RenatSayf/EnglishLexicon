package com.myapp.lexicon.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.settings.AppSettings
import com.myapp.lexicon.settings.checkUnLockedBroadcast
import com.myapp.lexicon.settings.getWordFromPref
import com.myapp.lexicon.settings.saveWordToPref
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PhoneUnlockedReceiver : BroadcastReceiver()
{
    companion object {
        private var instance: PhoneUnlockedReceiver? = null
        private var isBroadcast: Boolean = true

        fun getInstance(): PhoneUnlockedReceiver {

            isBroadcast = true
            if (instance == null)
                instance = PhoneUnlockedReceiver()
            return instance!!
        }

        fun disableBroadcast() {
            isBroadcast = false
        }
    }

    private lateinit var repository: DataRepositoryImpl
    private var appNotification: AppNotification? = null

    @SuppressLint("CheckResult")
    @Suppress("RedundantSamConstructor")
    override fun onReceive(context: Context, intent: Intent)
    {
        var isRegister = true
        context.checkUnLockedBroadcast(
            onEnabled = {},
            onDisabled = {
                context.unregisterReceiver(this)
                isRegister = false
            }
        )
        if (!isRegister) return

        if (!isBroadcast) {
            context.unregisterReceiver(this)
            return
        }

        val dao = AppDataBase.buildDataBase(context).appDao()
        val appSettings = AppSettings(context)
        repository = DataRepositoryImpl(dao, appSettings)

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0")!!
        val displayMode = preferences.getString(context.getString(R.string.key_list_display_mode), "0")!!
        val action = intent.action

        //String actionUserPresent = Intent.ACTION_USER_PRESENT;
        val actionScreenOff = Intent.ACTION_SCREEN_OFF
        if (action != null && action == actionScreenOff)
        {
            context.getWordFromPref(
                onInit = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val word = repository.getFirstEntryAsync().await()
                        handleBroadCast(context, word, displayVariant, displayMode)
                    }
                },
                onSuccess = { word ->
                    handleBroadCast(context, word, displayVariant, displayMode)
                },
                onFailure = {
                    it.printStackTrace()
                }
            )
        }
    }

    private fun handleBroadCast(context: Context, word: Word, displayVariant: String, displayMode: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val words = repository.getEntriesByDictNameAsync(word.dictName, id = word._id.toLong(), limit = 2).await()
            if (displayVariant == "0")
            {
                val intentAct = Intent(context, ServiceActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    val json = Gson().toJson(words)
                    putExtra(ServiceActivity.ARG_JSON, json)
                }
                ContextCompat.startActivity(context, intentAct, Bundle.EMPTY)
            }
            if (displayVariant == "1")
            {
                val json = Gson().toJson(words)

                if (appNotification == null) {
                    appNotification = AppNotification(context)
                }
                if (appNotification != null && !appNotification!!.isVisible()) {
                    appNotification!!.create(json)
                    appNotification!!.show()
                }

                if (displayMode == "0")
                {
                    when(words.size) {
                        2 -> {
                            context.saveWordToPref(words[1])
                        }
                        1 -> {
                            context.saveWordToPref(words[0])
                        }
                    }
                }
            }
        }

    }

    fun getFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
    }


}