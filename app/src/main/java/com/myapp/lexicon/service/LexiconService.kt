package com.myapp.lexicon.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.database.DatabaseHelper
import com.myapp.lexicon.main.MainViewModel
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.schedule.TimerReceiver
import com.myapp.lexicon.service.ServiceActivity.IStopServiceByUser
import com.myapp.lexicon.settings.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class LexiconService : Service(), IStopServiceByUser, LifecycleOwner
{
    companion object
    {
        @JvmField
        var stoppedByUser = false
    }

    //private var oldLocale: Locale? = null
    private var timeReceiver: TimerReceiver? = null
    private var blockReceiver: PhoneUnlockedReceiver? = null
    private var startId = 0
    private lateinit var vm: MainViewModel
    private val composite = CompositeDisposable()

    override fun onBind(intent: Intent): IBinder?
    {
        return null
    }

    override fun onCreate()
    {
        super.onCreate()

        val appDB = AppDB(DatabaseHelper(this))
        val dao = AppDataBase.getInstance(this).appDao()
        val appSettings = AppSettings(this)
        val repository = DataRepositoryImpl(appDB, dao, appSettings)
        vm = MainViewModel(repository)
        val currentWord = vm.currentWord.value
        val dictName = currentWord?.dictName ?: ""
        val wordId = currentWord?._id ?: 1

        //oldLocale = resources.configuration.locale
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isService = preferences.getBoolean(getString(R.string.key_service), false)
        if (isService)
        {
            blockReceiver = PhoneUnlockedReceiver()
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_USER_PRESENT)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            registerReceiver(blockReceiver, filter)
        }
        val timeInterval = preferences.getString(getString(R.string.key_show_intervals), "0")?.let {
            if (it.toInt() > 0)
            {
                timeReceiver = TimerReceiver()
                registerReceiver(timeReceiver, null)
            }
        }


        ServiceActivity.setStoppedByUserListener(this)


        composite.add(vm.getWordsFromDict(dictName, wordId, 2)
                .observeOn(Schedulers.computation())
                .subscribe({ words ->
                    val json = Gson().toJson(words)
                    json?.let {
                        val appNotification = AppNotification(this).create(json)
                        startForeground(AppNotification.NOTIFICATION_ID, appNotification)
                    }
                }, { t ->
                    t.printStackTrace()
                }))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        this.startId = startId
        return START_STICKY
    }

    override fun onDestroy()
    {
        super.onDestroy()
        timeReceiver?.let { unregisterReceiver(timeReceiver) }
        blockReceiver?.let { unregisterReceiver(blockReceiver) }
        composite.apply {
            dispose()
            clear()
        }
    }

    override fun onTaskRemoved(rootIntent: Intent)
    {
        super.onTaskRemoved(rootIntent)
        onCreate()
    }

    override fun onConfigurationChanged(newConfig: Configuration)
    {
        super.onConfigurationChanged(newConfig)
//        val newLocale = newConfig.locale
//        if (oldLocale!!.language != newLocale.language)
//        {
//            val appSettings = AppSettings(this)
//            appSettings.cleanPlayList()
//            stopSelf(startId)
//        }
    }

    override fun onStoppedByUser()
    {
        stopSelf(startId)
        Toast.makeText(this, this.getString(R.string.text_app_is_closed) + " " + getString(R.string.app_name) + " " + this.getString(R.string.text_app_is_closed_end), Toast.LENGTH_SHORT).show()
    }



    // TODO: обработчик событий нажатия кнопки блокировки, выключения экрана....
    class PhoneUnlockedReceiver : BroadcastReceiver()
    {
        @SuppressLint("CheckResult")
        @Suppress("RedundantSamConstructor")
        override fun onReceive(context: Context, intent: Intent)
        {
            val appDB = AppDB(DatabaseHelper(context))
            val dao = AppDataBase.getInstance(context).appDao()
            val appSettings = AppSettings(context)
            val repository = DataRepositoryImpl(appDB, dao, appSettings)

            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0")
            val displayMode = preferences.getString(context.getString(R.string.key_list_display_mode), "0")
            var action = intent.action
            val settings = AppSettings(context)

            val currentWord = settings.wordFromPref
            val dictName = currentWord?.dictName ?: "default"
            val wordId = currentWord?._id ?: -1
            //String actionUserPresent = Intent.ACTION_USER_PRESENT;
            val actionScreenOff = Intent.ACTION_SCREEN_OFF
            if (action != null)
            {
                if (action == actionScreenOff)
                {
                    repository.getEntriesFromDbByDictName(dictName, wordId, 2)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ words ->

                                if (displayVariant == "0")
                                {
                                    val intentAct = Intent(context, ServiceActivity::class.java).apply {
                                        action = Intent.ACTION_MAIN
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        val json = Gson().toJson(words)
                                        putExtra(ServiceActivity.ARG_JSON, json)
                                    }
                                    startActivity(context, intentAct, Bundle.EMPTY)
                                }
                                if (displayVariant == "1")
                                {
                                    val json = Gson().toJson(words)
                                    val appNotification = AppNotification(context)
                                    appNotification.create(json)
                                    appNotification.show()
                                    if (displayMode == "0")
                                    {
                                        settings.goForward(words)
                                    }
                                }

                            }, { t ->
                                t.printStackTrace()
                            })
                }
            }
        }
    }

    override fun getLifecycle(): Lifecycle
    {
        return ServiceLifecycleDispatcher(this).lifecycle
    }


}