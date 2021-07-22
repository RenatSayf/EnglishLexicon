package com.myapp.lexicon.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.database.DatabaseHelper
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.service.ServiceActivity
import com.myapp.lexicon.settings.AppSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class TimerReceiver : BroadcastReceiver()
{
    private var isWordsEnded = false
    private var wordsCounter = 0

    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (context != null && intent != null)
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (intent.action == AlarmScheduler.REPEAT_SHOOT_ACTION || intent.action == Intent.ACTION_SCREEN_OFF)
            {
                //println("**************** REPEAT_SHOOT_ACTION *********************")

                val dao = AppDataBase.buildDataBase(context).appDao()
                val appDB = AppDB(DatabaseHelper(context), dao)
                val appSettings = AppSettings(context)
                val repository = DataRepositoryImpl(appDB, dao, appSettings)

                val displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0")
                preferences.getString(context.getString(R.string.key_list_display_mode), "0")
                val settings = AppSettings(context)
                val orderPlay = settings.orderPlay
                val currentWord = settings.wordFromPref
                val dictName = currentWord.dictName
                val wordId = currentWord._id

                if (isWordsEnded)
                {
                    wordsCounter = 0
                }
                else wordsCounter++

                when (orderPlay)
                {
                    0 ->
                    {
                        repository.getEntriesFromDbByDictName(dictName, wordId, 1, 2)
                                .subscribeOn(Schedulers.computation())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ words ->

                                    if (words.size > 0)
                                    {
                                        when (words.size)
                                        {
                                            2 -> isWordsEnded = false
                                            1 -> isWordsEnded = true
                                        }
                                        repository.goForward(words)
                                        val json = Gson().toJson(words)
                                        when (displayVariant)
                                        {
                                            "0" ->
                                            {
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
                                            "1" ->
                                            {
                                                AppNotification(context).apply {
                                                    create(json)
                                                    show()
                                                }
                                            }
                                        }
                                    }

                                }, { t ->
                                    t.printStackTrace()
                                })
                    }
                    1 ->
                    {

                    }
                }


            }
        }
    }

}