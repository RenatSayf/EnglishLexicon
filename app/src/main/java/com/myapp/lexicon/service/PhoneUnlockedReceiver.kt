package com.myapp.lexicon.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.helpers.checkIsActivityShown
import com.myapp.lexicon.helpers.goAsync
import com.myapp.lexicon.helpers.showDebugNotification
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.models.toWordsString
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.settings.AppSettings
import com.myapp.lexicon.settings.checkUnLockedBroadcast
import com.myapp.lexicon.settings.getNotificationMode
import com.myapp.lexicon.settings.getOrderPlay
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

    @SuppressLint("CheckResult")
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

        this.goAsync(CoroutineScope(Dispatchers.IO), block = { scope ->
            val dao = AppDataBase.buildDataBase(context).appDao()
            val appSettings = AppSettings(context)
            repository = DataRepositoryImpl(dao, appSettings)

            val action = intent.action

            //String actionUserPresent = Intent.ACTION_USER_PRESENT;
            val actionScreenOff = Intent.ACTION_SCREEN_OFF
            if (action != null && action == actionScreenOff)
            {
                context.getWordFromPref(
                    onInit = {
                        scope.launch {
                            try {
                                val word = repository.getFirstEntryAsync().await()
                                handleBroadCast(context, scope, word)
                            } catch (e: Exception) {
                                context.showDebugNotification(e.message)
                            }
                        }
                    },
                    onSuccess = { word ->

                        context.getOrderPlay(
                            onCycle = {
                                handleBroadCast(context, scope, word)
                            },
                            onRandom = {
                                val randomWord = word.apply { _id = -1 }
                                handleBroadCast(context, scope, randomWord)
                            }
                        )
                    },
                    onFailure = {
                        it.printStackTrace()
                        context.showDebugNotification(it.message)
                    }
                )
            }
        })

    }

    private fun handleBroadCast(context: Context, scope: CoroutineScope, word: Word) {

        scope.launch {
            try {
                val words = if (word._id > 0) {
                    repository.getEntriesByDictNameAsync(word.dictName, id = word._id.toLong(), limit = 2).await()
                }
                else {
                    val randomWord = repository.getRandomEntriesFromDB(word.dictName, -1).await()
                    listOf(randomWord, randomWord)
                }

                if (words.isNotEmpty()) {
                    val notification = AppNotification(context)
                    val text = words.toWordsString()
                    context.getNotificationMode(
                        onRepeatMode = {
                            saveCurrentStep(context, scope, words)
                            context.checkIsActivityShown(
                                ServiceActivity::class.java,
                                onInvisible = {
                                    notification.apply {
                                        create(text)
                                        show()
                                    }
                                }
                            )
                        },
                        onTestMode = {
                            context.checkIsActivityShown(
                                ServiceActivity::class.java,
                                onInvisible = {
                                    if (!notification.isVisible()) {
                                        notification.apply {
                                            create(text)
                                            show()
                                        }
                                    }
                                }
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                context.showDebugNotification(e.message)
                e.printStackTrace()
            }
        }
    }

    private fun saveCurrentStep(context: Context, scope: CoroutineScope, words: List<Word>) {

        when(words.size) {
            2 -> {
                context.saveWordToPref(words[1])
            }
            1 -> {
                scope.launch {
                    val list = repository.getEntriesByDictNameAsync(dict = words[0].dictName, id = 1, limit = 1).await()
                    context.saveWordToPref(list[0])
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