package com.myapp.lexicon.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.IBinder
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.database.DatabaseHelper
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.schedule.TimerReceiver
import com.myapp.lexicon.service.ServiceActivity.IStopServiceByUser
import com.myapp.lexicon.settings.AppSettings
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*

class LexiconService : Service(), IStopServiceByUser
{
    companion object
    {
        @JvmField
        var stopedByUser = false
    }

    private var oldLocale: Locale? = null
    //private var receiver: PhoneUnlockedReceiver? = null
    private var receiver: TimerReceiver? = null
    private var startId = 0
    private val composite = CompositeDisposable()

    override fun onBind(intent: Intent): IBinder?
    {
        return null
    }

    override fun onCreate()
    {
        super.onCreate()
        oldLocale = resources.configuration.locale
        //receiver = PhoneUnlockedReceiver()
        receiver = TimerReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_USER_PRESENT)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(receiver, filter)
        ServiceActivity.setStoppedByUserListener(this)

        val appSettings = AppSettings(this)
        val playList = appSettings.getPlayList(true)
        val nDict: String = playList[appSettings.dictNumber]
        val nWord: Int = appSettings.wordNumber

        val db = AppDB(DatabaseHelper(this))
        composite.add(
                db.getEntriesAndCountersAsync(nDict, nWord, "ASC", 2)
                        .observeOn(Schedulers.computation())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ pairs: Pair<MutableMap<String, Int>, MutableList<DataBaseEntry>> ->
                            appSettings.goForward(pairs.second)
                            if (pairs.second.isNotEmpty() && !MainActivity.isActivityRunning)
                            {
                                val json = Gson().toJson(pairs)
                                json?.let {
                                    val appNotification = AppNotification(this).create(json)
                                    startForeground(AppNotification.NOTIFICATION_ID, appNotification)
                                }
                                composite.dispose()
                                composite.clear()
                            }
                            if (pairs.first.isEmpty() && pairs.second.isEmpty())
                            {
                                composite.dispose()
                                composite.clear()
                            }
                        }, { throwable: Throwable ->
                            composite.dispose()
                            composite.clear()
                            throwable.printStackTrace()
                        }))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        this.startId = startId
        return START_STICKY
    }

    override fun onDestroy()
    {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent)
    {
        super.onTaskRemoved(rootIntent)
        onCreate()
    }

    override fun onConfigurationChanged(newConfig: Configuration)
    {
        super.onConfigurationChanged(newConfig)
        val newLocale = newConfig.locale
        if (oldLocale!!.language != newLocale.language)
        {
            val appSettings = AppSettings(this)
            appSettings.cleanPlayList()
            stopSelf(startId)
        }
    }

    override fun onStoppedByUser()
    {
        stopSelf(startId)
        Toast.makeText(this, this.getString(R.string.text_app_is_closed) + " " + getString(R.string.app_name) + " " + this.getString(R.string.text_app_is_closed_end), Toast.LENGTH_SHORT).show()
    }





    inner class PhoneUnlockedReceiver : BroadcastReceiver()
    {
        // TODO: обработчик событий нажатия кнопки блокировки, выключения экрана....
        @SuppressLint("CheckResult")
        @Suppress("RedundantSamConstructor")
        override fun onReceive(context: Context, intent: Intent)
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0")
            val displayMode = preferences.getString(context.getString(R.string.key_list_display_mode), "0")
            val action = intent.action
            val settings = AppSettings(context)
            val orderPlay = settings.orderPlay
            val playList = settings.playList
            val dictName = playList[settings.dictNumber]
            val nWord = settings.wordNumber
            //String actionUserPresent = Intent.ACTION_USER_PRESENT;
            val actionScreenOff = Intent.ACTION_SCREEN_OFF
            if (action != null)
            {
                if (action == actionScreenOff)
                {
                    if (displayVariant == "0")
                    {
                        val intentAct = Intent(context, ServiceActivity::class.java)
                        intentAct.action = Intent.ACTION_MAIN
                        intentAct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intentAct)
                    }
                    if (displayVariant == "1")
                    {

                        val db = AppDB(DatabaseHelper(context))
                        db.getEntriesAndCountersAsync(dictName, settings.wordNumber, "ASC")
                                .subscribeOn(Schedulers.computation())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(Consumer { pair ->
                                    val json = Gson().toJson(pair)
                                    if (displayMode == "0")
                                    {
                                        if (pair.second.size > 0)
                                        {
                                            val appNotification = AppNotification(context)
                                            appNotification.create(json)
                                            appNotification.show()
                                        }
                                        settings.goForward(pair.second as LinkedList<DataBaseEntry>)
                                    }
                                    if (displayMode == "1")
                                    {
                                        if (pair.second.size > 0)
                                        {
                                            val appNotification = AppNotification(context)
                                            appNotification.create(json)
                                            appNotification.show()
                                        }
                                    }
                                }, Consumer { t ->
                                    t.printStackTrace()
                                })
                    }
                }
            }
        }
    }


}