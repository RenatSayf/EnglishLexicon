package com.myapp.lexicon.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.database.DatabaseHelper
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.service.ServiceActivity.IStopServiceByUser
import com.myapp.lexicon.settings.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


@AndroidEntryPoint
class LexiconService : Service(), IStopServiceByUser//, LifecycleOwner
{
    private var blockReceiver: PhoneUnlockedReceiver? = null
    private var startId = 0
    private val composite = CompositeDisposable()

    override fun onBind(intent: Intent): IBinder?
    {
        return null
    }

    override fun onCreate()
    {
        super.onCreate()

        val dao = AppDataBase.buildDataBase(this).appDao()
        val appDB = AppDB(DatabaseHelper(this), dao)
        val appSettings = AppSettings(this)
        val repository = DataRepositoryImpl(appDB, dao, appSettings)
        val currentWord = repository.getWordFromPref()
        val dictName = currentWord.dictName
        val wordId = currentWord._id

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

        ServiceActivity.setStoppedByUserListener(this)

        composite.add(repository.getEntriesFromDbByDictName(dictName, wordId, 1, 2)
                .observeOn(Schedulers.computation())
                .subscribe({ words ->
                    if (words.isNotEmpty()) {
                        val json = Gson().toJson(words)
                        json?.let {
                            val appNotification = AppNotification(this).create(json)
                            startForeground(AppNotification.NOTIFICATION_ID, appNotification)
                        }
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

    override fun onStoppedByUser()
    {
        stopSelf(startId)
        Toast.makeText(this, this.getString(R.string.text_app_is_closed) + " " + getString(R.string.app_name) + " " + this.getString(R.string.text_app_is_closed_end), Toast.LENGTH_SHORT).show()
    }



    // TODO: обработчик событий нажатия кнопки блокировки, выключения экрана....
    @AndroidEntryPoint
    class PhoneUnlockedReceiver : BroadcastReceiver()
    {
        @Inject
        lateinit var repository: DataRepositoryImpl

        private val composite = CompositeDisposable()

        @SuppressLint("CheckResult")
        @Suppress("RedundantSamConstructor")
        override fun onReceive(context: Context, intent: Intent)
        {
            //println("*********************** ACTION_SCREEN_OFF ***************************")
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0")
            val displayMode = preferences.getString(context.getString(R.string.key_list_display_mode), "0")
            var action = intent.action

            val currentWord = repository.getWordFromPref()
            val dictName = currentWord.dictName
            val wordId = currentWord._id
            //String actionUserPresent = Intent.ACTION_USER_PRESENT;
            val actionScreenOff = Intent.ACTION_SCREEN_OFF
            if (action != null)
            {
                if (action == actionScreenOff)
                {
                    composite.add(
                        repository.getEntriesFromDbByDictName(dictName, wordId, 1, 2)
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
                                        repository.goForward(words)
                                    }
                                }
                                composite.apply {
                                    dispose()
                                    clear()
                                }

                            }, { t ->
                                t.printStackTrace()
                                composite.apply {
                                    dispose()
                                    clear()
                                }
                            })
                    )
                }
            }
        }
    }



}