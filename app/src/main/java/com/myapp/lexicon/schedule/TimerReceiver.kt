package com.myapp.lexicon.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.service.ServiceActivity
import com.myapp.lexicon.settings.AppSettings
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
    private var wordsCounter = 1

    @Inject
    lateinit var repository: DataRepositoryImpl

    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (context != null && intent != null)
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val strInterval = preferences.getString(context.getString(R.string.key_show_intervals), "0")
            strInterval?.toLong()?.let {
                AlarmScheduler(context).scheduleOne(it * 60 * 1000)
            }

            if (intent.action == AlarmScheduler.ONE_SHOOT_ACTION || intent.action == Intent.ACTION_SCREEN_OFF)
            {
                val displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0")!!
                preferences.getString(context.getString(R.string.key_list_display_mode), "0")
                val settings = AppSettings(context)
                val orderPlay = settings.orderPlay

                context.getWordFromPref(
                    onInit = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val word = repository.getFirstEntryAsync().await()
                            handleAlarm(context, orderPlay, displayVariant, word)
                        }
                    },
                    onSuccess = { word ->
                        handleAlarm(context, orderPlay, displayVariant, word)
                    },
                    onFailure = {
                        it.printStackTrace()
                    }
                )
            }
        }
    }

    private fun handleAlarm(context: Context, order: Int, displayVariant: String, word: Word) {

        when (order) {
            0 ->  {
                CoroutineScope(Dispatchers.IO).launch {
                    val words = repository.getEntriesByDictNameAsync(word.dictName, id = word._id.toLong(), limit = 2).await()
                    if (words.isNotEmpty()) {
                        when (words.size) {
                            2 -> {
                                context.saveWordToPref(words[1])
                                wordsCounter++
                            }
                            1 -> {
                                val list = repository.getEntriesByDictNameAsync(words[0].dictName,1, limit = 1).await()
                                context.saveWordToPref(list[0])
                                wordsCounter = 1
                            }
                        }
                        val json = Gson().toJson(words)
                        when (displayVariant) {
                            "0" -> {
                                val intentAct = Intent(context, ServiceActivity::class.java).apply {
                                    action = Intent.ACTION_MAIN
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    putExtra(ServiceActivity.ARG_JSON, json)
                                }
                                context.startActivity(intentAct)
                            }
                            "1" -> {
                                AppNotification(context).apply {
                                    create(json)
                                    show()
                                }
                            }
                        }
                    }
                }
            }
            1 -> {}
        }
    }


}