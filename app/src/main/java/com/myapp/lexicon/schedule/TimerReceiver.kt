package com.myapp.lexicon.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.DatabaseHelper
import com.myapp.lexicon.service.ServiceDialog
import com.myapp.lexicon.settings.AppData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@Suppress("DEPRECATION")
class TimerReceiver : BroadcastReceiver()
{

    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (context != null && intent != null)
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            when(intent.action)
            {
                AlarmScheduler.REPEAT_SHOOT_ACTION ->
                {

                    val displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0")
                    println("********************* displayVariant = $displayVariant *********************")
                    when (displayVariant!!.toInt())
                    {
                        0 ->
                        {
                            val intentAct = Intent(context, ServiceDialog::class.java).apply {
                                action = Intent.ACTION_MAIN
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                            context.startActivity(intentAct)
                        }
                        1 ->
                        {
                            val appData = AppData.getInstance()
                            val playList = appData.playList
                            val dictName = playList[appData.ndict]
                            val displayMode = preferences.getString(context.getString(R.string.key_list_display_mode), "0")
                            val db = AppDB(DatabaseHelper(context))
                            when (displayMode)
                            {
                                "0" ->
                                {
                                    db.getEntriesFromDbAsync(dictName, appData.nword)
                                            .observeOn(Schedulers.computation())
                                            .subscribeOn(AndroidSchedulers.mainThread())
                                            .subscribe({ entry ->
                                                if (entry.size > 0)
                                                {
                                                    AppNotification(context).apply {
                                                        create(entry[0])
                                                        show()
                                                    }
                                                }
                                                if (entry.size > 1)
                                                {
                                                    appData.nword = entry[1].rowId
                                                }
                                                if (entry.size == 1)
                                                {
                                                    appData.nword = 1
                                                    if (appData.ndict in 0..playList.size - 2)
                                                    {
                                                        appData.ndict++
                                                    }
                                                    else appData.ndict = 0
                                                }
                                            }, { t ->
                                                t.printStackTrace()
                                            })
                                }
                                "1" ->
                                {
                                    db.getEntriesFromDbAsync(dictName, appData.nword)
                                            .observeOn(Schedulers.computation())
                                            .subscribeOn(AndroidSchedulers.mainThread())
                                            .subscribe({ entry ->
                                                if (entry.size > 0)
                                                {
                                                    AppNotification(context).apply {
                                                        create(entry[0])
                                                        show()
                                                    }
                                                }
                                            }, { t ->
                                                t.printStackTrace()
                                            })

                                    db.getEntriesAndAmountAsync(dictName, appData.nword)
                                            .observeOn(Schedulers.computation())
                                            .subscribeOn(AndroidSchedulers.mainThread())
                                            .subscribe({

                                            },{ t ->
                                                t.printStackTrace()
                                            })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}