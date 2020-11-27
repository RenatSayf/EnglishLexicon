package com.myapp.lexicon.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.AppDB;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.schedule.AppNotification;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kotlin.Pair;

public class LexiconService extends Service implements ServiceDialog.IStopServiceByUser
{
    public static boolean stopedByUser = false;
    private Locale oldLocale;
    private PhoneUnlockedReceiver receiver;
    private int displayVariant = 0;
    private int startId;
    private final int appId = 542390;
    NotificationManager manager;

    public LexiconService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        oldLocale = getResources().getConfiguration().locale;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LexiconService.this);
        String preferencesString = preferences.getString(getString(R.string.key_display_variant), "0");
        displayVariant = Integer.parseInt(preferencesString);

        String contentText = "";
        Intent intent;
        PendingIntent pendingIntent = null;
        Notification appNotification;
        switch (displayVariant)
        {
            case 0:
                contentText = getString(R.string.notify_content_text);
                break;
            case 1:
                contentText = getString(R.string.notify_new_word_text);
                break;
        }

        appNotification = new AppNotification(this).create(new DataBaseEntry(contentText, ""));
        startForeground(AppNotification.NOTIFICATION_ID, appNotification);

        receiver = new PhoneUnlockedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);

        ServiceDialog.setStoppedByUserListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        this.startId = startId;
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);
        onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        Locale newLocale = newConfig.locale;
        if (!oldLocale.getLanguage().equals(newLocale.getLanguage()))
        {
            AppSettings appSettings = new AppSettings(this);
            appSettings.cleanPlayList();
            stopSelf(startId);
        }
    }

    @Override
    public void onStoppedByUser()
    {
        stopSelf(startId);
        Toast.makeText(this, this.getString(R.string.text_app_is_closed) + " " + getString(R.string.app_name) + " " + this.getString(R.string.text_app_is_closed_end), Toast.LENGTH_SHORT).show();
    }




    public class PhoneUnlockedReceiver extends BroadcastReceiver
    {
        // TODO: обработчик событий нажатия кнопки блокировки, выключения экрана....
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (context != null && intent != null)
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String displayVariant = preferences.getString(context.getString(R.string.key_display_variant), "0");
                String displayMode = preferences.getString(context.getString(R.string.key_list_display_mode), "0");

                String action = intent.getAction();
                //String actionUserPresent = Intent.ACTION_USER_PRESENT;
                String actionScreenOff = Intent.ACTION_SCREEN_OFF;

                if (action != null)
                {
                    if ((action.equals(actionScreenOff) /*|| action.equals(actionUserPresent)*/))
                    {
                        if (displayVariant.equals("0"))
                        {
                            Intent intentAct = new Intent(context, ServiceDialog.class);
                            intentAct.setAction(Intent.ACTION_MAIN);
                            intentAct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intentAct);
                        }
                        if (displayVariant.equals("1"))
                        {
                            AppData appData = AppData.getInstance();
                            ArrayList<String> playList = appData.getPlayList();
                            String dictName = playList.get(appData.getNdict());
                            AppDB db = new AppDB(new DatabaseHelper(context));

                            db.getEntriesAndAmountAsync(dictName, appData.getNword(), "ASC")
                                    .subscribeOn(Schedulers.computation())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(item -> {
                                        Pair<Map<String, Integer>, List<DataBaseEntry>> pairs = item;
                                        if (displayMode.equals("0"))
                                        {
                                            if (pairs.getSecond().size() > 0)
                                            {
                                                AppNotification appNotification = new AppNotification(context);
                                                appNotification.create(pairs.getSecond().get(0));
                                                appNotification.show();
                                            }
                                            if (pairs.getSecond().size() > 1)
                                            {
                                                appData.setNword(pairs.getSecond().get(1).getRowId());
                                            }
                                            if (pairs.getSecond().size() == 1)
                                            {
                                                appData.setNword(1);
                                                if (appData.getNdict() >=0 && appData.getNdict() <= playList.size() - 2)
                                                {
                                                    appData.setNdict(appData.getNdict() + 1);
                                                }
                                                else appData.setNdict(0);
                                            }
                                        }
                                        if (displayMode.equals("1"))
                                        {
                                            if (pairs.getSecond().size() > 0)
                                            {
                                                AppNotification appNotification = new AppNotification(context);
                                                appNotification.create(pairs.getSecond().get(0));
                                                appNotification.show();
                                            }
                                        }
                                    }, Throwable::printStackTrace);

                        }
                    }
                }
            }
        }
    }
}
