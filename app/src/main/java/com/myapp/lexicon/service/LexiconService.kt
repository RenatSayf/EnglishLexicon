package com.myapp.lexicon.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.*
import android.content.res.Configuration
import android.os.IBinder
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDB
import com.myapp.lexicon.database.DatabaseHelper
import com.myapp.lexicon.schedule.AppNotification
import com.myapp.lexicon.service.ServiceActivity.IStopServiceByUser
import com.myapp.lexicon.settings.AppData
import com.myapp.lexicon.settings.AppSettings
import io.reactivex.android.schedulers.AndroidSchedulers
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
    private var receiver: PhoneUnlockedReceiver? = null
    private var startId = 0

    override fun onBind(intent: Intent): IBinder?
    {
        return null
    }

    override fun onCreate()
    {
        super.onCreate()
        oldLocale = resources.configuration.locale
        receiver = PhoneUnlockedReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_USER_PRESENT)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(receiver, filter)
        ServiceActivity.setStoppedByUserListener(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        this.startId = startId
        val json = intent.getStringExtra(ModalFragment.ARG_JSON)
        json?.let {
            val appNotification = AppNotification(this).create(json)
            startForeground(AppNotification.NOTIFICATION_ID, appNotification)
        }

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
                        val appData = AppData.getInstance()
                        val playList = appData.playList
                        val dictName = playList[appData.ndict]
                        val db = AppDB(DatabaseHelper(context))
                        db.getEntriesAndAmountAsync(dictName, appData.nword, "ASC")
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
                                        if (pair.second.size > 1)
                                        {
                                            appData.nword = pair.second[1].rowId
                                        }
                                        if (pair.second.size == 1)
                                        {
                                            appData.nword = 1
                                            if (appData.ndict >= 0 && appData.ndict <= playList.size - 2)
                                            {
                                                appData.ndict = appData.ndict + 1
                                            }
                                            else appData.ndict = 0
                                        }
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