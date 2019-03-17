package com.myapp.lexicon.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppSettings;

import java.util.HashMap;
import java.util.Locale;

public class LexiconService extends Service
{
    public static boolean isStop = false;
    public static TextToSpeech speech;
    public static HashMap<String, String> map = new HashMap<>();
    private Locale oldLocale;

    private PhoneUnlockedReceiver receiver;

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

        Intent intent = new Intent(this, SplashScreenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            startLexiconOwnForeground();
        }
        else
        {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_lexicon_notify)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notify_content_text))
                    .setContentIntent(pendingIntent); // TODO: запуск MainActivity при клике на Notification
            Notification notification;
            notification = builder.build();
            int appId = 542389;
            startForeground(appId, notification); //TODO запуск сервиса на переднем плане, чтобы сервис не убивала система
        }


        receiver = new PhoneUnlockedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);

        speech = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS )
                {
                    int resultEn = speech.isLanguageAvailable(Locale.US);
                    if (resultEn == TextToSpeech.LANG_COUNTRY_AVAILABLE)
                    {
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.US.getDisplayLanguage());
                        speech.setLanguage(Locale.US);
                        speech.stop();
                    }
                }
                if (status == TextToSpeech.LANG_NOT_SUPPORTED || status == TextToSpeech.LANG_MISSING_DATA)
                {
                    stopSelf();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startLexiconOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "com.myapp.lexicon.service";
        String channelName = "Lexicon Background Service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(notificationChannel);

            Intent intent = new Intent(this, SplashScreenActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_lexicon_notify)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notify_content_text))
                    .setColor(Color.GREEN)
                    .build();
            int appId = 542390;
            startForeground(appId, notification); //TODO запуск сервиса на переднем плане, чтобы сервис не убивала система
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(receiver);
        speech.shutdown();
        if (isStop)
        {
            Toast.makeText(this, "Приложение " + getString(R.string.app_name) + " закрыто", Toast.LENGTH_SHORT).show();
            isStop = false;
        }
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
            stopSelf();
        }
    }

    public class PhoneUnlockedReceiver extends BroadcastReceiver
    {
        // TODO: обработчик событий нажатия кнопки блокировки, выключения экрана....
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent != null)
            {
                String action = intent.getAction();
                //String actionUserPresent = Intent.ACTION_USER_PRESENT;
                String actionScreenOff = Intent.ACTION_SCREEN_OFF;
                //String actionScreenOn = Intent.ACTION_SCREEN_ON;

                if (action != null)
                {
//                    if (action.equals(actionUserPresent))
//                    {
//
//                    }
                    if (action.equals(actionScreenOff))
                    {
                        Intent intentAct = new Intent("android.intent.action.MAIN");
                        intentAct.setClass(LexiconService.this, ServiceDialog.class);
                        intentAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentAct);
                    }
//                    if (action.equals(actionScreenOn))
//                    {
//
//                    }
                }
            }
        }
    }
}
