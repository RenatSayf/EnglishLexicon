package com.myapp.lexicon.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.helpers.checkIsActivityShown
import com.myapp.lexicon.helpers.goAsync
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showDebugNotification
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.models.toWordsString
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.settings.checkUnLockedBroadcast
import com.myapp.lexicon.settings.getNotificationMode
import com.myapp.lexicon.settings.getWordFromPref
import com.myapp.lexicon.settings.saveWordToPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
            val dao = AppDataBase.getDbInstance(context).appDao()
            repository = DataRepositoryImpl(dao)

            val action = intent.action

            //String actionUserPresent = Intent.ACTION_USER_PRESENT;
            val actionScreenOff = Intent.ACTION_SCREEN_OFF
            if (action != null && action == actionScreenOff)
            {
                context.getWordFromPref(
                    onSuccess = { word, _ ->
                        handleBroadCast(context, scope, word)
                    },
                    onFailure = {
                        it.printStackTraceIfDebug()
                        context.showDebugNotification(it.message)
                    }
                )
            }
        })

    }

    private fun handleBroadCast(context: Context, scope: CoroutineScope, word: Word) {

        scope.launch {
            try {
                val words = repository.getWordPairFromPlayListAsync(word._id).await()
                if (words.isNotEmpty()) {
                    val notification = AppNotification(context)
                    val text = words.toWordsString()
                    context.getNotificationMode(
                        onRepeatMode = {
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
                    saveCurrentStep(context, scope, words)
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
                context.saveWordToPref(words[1], words.indexOf(words[1]))
            }
            1 -> {
                scope.launch {
                    val list = repository.getFirstFromPlayListAsync().await()
                    context.saveWordToPref(list[0], words.indexOf(words[1]))
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