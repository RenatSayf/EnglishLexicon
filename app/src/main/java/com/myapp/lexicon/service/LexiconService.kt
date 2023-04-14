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
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.repository.DataRepositoryImpl
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.service.ServiceActivity.Listener
import com.myapp.lexicon.settings.AppSettings
import com.myapp.lexicon.settings.getWordFromPref
import com.myapp.lexicon.settings.saveWordToPref
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LexiconService : Service(), Listener
{
    private var blockReceiver: PhoneUnlockedReceiver? = null
    private var startId = 0
    private val composite = CompositeDisposable()
    private lateinit var repository: DataRepositoryImpl

    override fun onBind(intent: Intent): IBinder?
    {
        return null
    }

    override fun onCreate()
    {
        super.onCreate()

        val dao = AppDataBase.buildDataBase(this).appDao()
        val appSettings = AppSettings(this)
        repository = DataRepositoryImpl(dao, appSettings)

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

        this.getWordFromPref(
            onInit = {
                CoroutineScope(Dispatchers.IO).launch {
                    val word = repository.getFirstEntryAsync().await()
                    showAppNotification(word)
                }
            },
            onSuccess = { word ->
                showAppNotification(word)
            },
            onFailure = {
                stopSelf()
            }
        )

    }

    private fun showAppNotification(word: Word) {
        CoroutineScope(Dispatchers.IO).launch {
            val words = repository.getEntriesByDictNameAsync(word.dictName, word._id.toLong(), 1, 2).await()
            if (words.isNotEmpty()) {
                val json = Gson().toJson(words)
                json?.let {
                    val appNotification = AppNotification(this@LexiconService).create(json)
                    //startForeground(AppNotification.NOTIFICATION_ID, appNotification)

                }
            }
        }
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
        private lateinit var repository: DataRepositoryImpl

        @SuppressLint("CheckResult")
        @Suppress("RedundantSamConstructor")
        override fun onReceive(context: Context, intent: Intent)
        {
            val dao = AppDataBase.buildDataBase(context).appDao()
            val appSettings = AppSettings(context)
            repository = DataRepositoryImpl(dao, appSettings)

            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0")!!
            val displayMode = preferences.getString(context.getString(R.string.key_list_display_mode), "0")!!
            val action = intent.action

            //String actionUserPresent = Intent.ACTION_USER_PRESENT;
            val actionScreenOff = Intent.ACTION_SCREEN_OFF
            if (action != null && action == actionScreenOff)
            {
                context.getWordFromPref(
                    onInit = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val word = repository.getFirstEntryAsync().await()
                            handleBroadCast(context, word, displayVariant, displayMode)
                        }
                    },
                    onSuccess = { word ->
                        handleBroadCast(context, word, displayVariant, displayMode)
                    },
                    onFailure = {
                        it.printStackTrace()
                    }
                )
            }
        }

        private fun handleBroadCast(context: Context, word: Word, displayVariant: String, displayMode: String) {

            CoroutineScope(Dispatchers.IO).launch {
                val words = repository.getEntriesByDictNameAsync(word.dictName, id = word._id.toLong(), limit = 2).await()
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
                        when(words.size) {
                            2 -> {
                                context.saveWordToPref(words[1])
                            }
                            1 -> {
                                context.saveWordToPref(words[0])
                            }
                        }
                    }
                }
            }

        }

    }



}