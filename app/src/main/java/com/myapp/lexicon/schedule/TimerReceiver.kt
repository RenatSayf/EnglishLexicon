package com.myapp.lexicon.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.database.DatabaseHelper
import com.myapp.lexicon.service.ServiceActivity
import com.myapp.lexicon.settings.AppData
import com.myapp.lexicon.settings.AppSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*


class TimerReceiver : BroadcastReceiver()
{

    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (context != null && intent != null)
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (intent.action == AlarmScheduler.REPEAT_SHOOT_ACTION || intent.action == Intent.ACTION_SCREEN_OFF)
            {
                println("**************** REPEAT_SHOOT_ACTION *********************")
                val displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0")
                preferences.getString(context.getString(R.string.key_list_display_mode), "0")
                val settings = AppSettings(context)
                val orderPlay = settings.orderPlay
                val playList = settings.playList
                val dictName = playList[settings.dictNumber]
                val nWord = settings.wordNumber

                val db = AppDB(DatabaseHelper(context))

                when (orderPlay)
                {
                    0 ->
                    {
                        db.getEntriesAndCountersAsync(dictName, nWord)
                                .observeOn(Schedulers.computation())
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe({ pair ->

                                    if (pair.second.size > 0)
                                    {
                                        settings.goForward(pair.second as LinkedList<DataBaseEntry>)
                                        val json = Gson().toJson(pair)
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
                                                    putExtra(AppData.ARG_JSON, json)
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