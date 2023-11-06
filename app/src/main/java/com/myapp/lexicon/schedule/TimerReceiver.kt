@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.myapp.lexicon.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.myapp.lexicon.R
import com.myapp.lexicon.helpers.checkIsActivityShown
import com.myapp.lexicon.helpers.goAsync
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showDebugNotification
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.models.toWordsString
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.service.ServiceActivity
import com.myapp.lexicon.settings.getNotificationMode
import com.myapp.lexicon.settings.getOrderPlay
import com.myapp.lexicon.settings.getWordFromPref
import com.myapp.lexicon.settings.saveWordToPref
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class TimerReceiver : BroadcastReceiver()
{
    @Inject
    lateinit var repository: DataRepositoryImpl

    private var preferences: SharedPreferences? = null

    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (context != null && intent != null)
        {

            preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val strInterval = preferences?.getString(context.getString(R.string.key_show_intervals), "0")
            strInterval?.toLong()?.let {
                AlarmScheduler(context).scheduleOne(it * 60 * 1000)
            }

            if (intent.action == AlarmScheduler.ONE_SHOOT_ACTION || intent.action == Intent.ACTION_SCREEN_OFF)
            {

                this.goAsync(CoroutineScope(Dispatchers.IO), block = { scope ->
                    context.getWordFromPref(
                        onInit = {
                            scope.launch {
                                val word = repository.getFirstEntryAsync().await()
                                handleAlarm(context, scope, word)
                            }
                        },
                        onSuccess = { word ->
                            context.getOrderPlay(
                                onCycle = {
                                    handleAlarm(context, scope, word)
                                },
                                onRandom = {
                                    val randomWord = word.apply { _id = -1 }
                                    handleAlarm(context, scope, randomWord)
                                }
                            )
                        },
                        onFailure = {
                            it.printStackTraceIfDebug()
                        }
                    )
                })
            }
        }
    }

    private fun handleAlarm(context: Context, scope: CoroutineScope, word: Word) {

        try {
            scope.launch {

                val words = if (word._id > 0) {
                    repository.getEntriesByDictNameAsync(word.dictName, id = word._id.toLong(), limit = 2).await()
                }
                else {
                    val randomWord = repository.getRandomEntriesFromDB(word.dictName, -1).await()
                    listOf(randomWord, randomWord)
                }

                if (words.isNotEmpty()) {
                    val text = words.toWordsString()
                    val notification = AppNotification(context)
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
            }
        } catch (e: Exception) {
            context.showDebugNotification(e.message)
            e.printStackTrace()
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


}