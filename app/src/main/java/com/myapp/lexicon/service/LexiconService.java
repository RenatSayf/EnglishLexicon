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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppSettings;

import java.util.Locale;

public class LexiconService extends Service implements ServiceDialog.IStopServiceByUser
{
    public static boolean stopedByUser = false;
    private Locale oldLocale;
    private PhoneUnlockedReceiver receiver;
    private int displayVariant = 0;
    private int startId;

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

        ServiceDialog.setStoppedByUserListener(this);
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

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LexiconService.this);
            String preferencesString = preferences.getString(getString(R.string.key_on_unbloking_screen), "0");
            String contentText = "";
            if (preferencesString != null)
            {
                displayVariant = Integer.parseInt(preferencesString);
            }
            Intent intent;
            PendingIntent pendingIntent = null;
            switch (displayVariant)
            {
                case 0:
                    intent = new Intent(this, SplashScreenActivity.class);
                    pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
                    contentText = getString(R.string.notify_content_text);
                    break;
                case 1:
                    intent = new Intent("android.intent.action.MAIN");
                    intent.setClass(LexiconService.this, ServiceDialog.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
                    contentText = "Нажмите что бы узнать новое слово";
                    break;
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_lexicon_notify)
                    .setContentIntent(pendingIntent)

                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(contentText)
                    .setColor(Color.GREEN)
                    .build();
            int appId = 542390;
            startForeground(appId, notification); //TODO запуск сервиса на переднем плане, чтобы сервис не убивала система
        }
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
            if (intent != null)
            {
                String action = intent.getAction();
                String actionUserPresent = Intent.ACTION_USER_PRESENT;
                String actionScreenOn = Intent.ACTION_SCREEN_ON;

                if (action != null)
                {
                    if ((action.equals(actionScreenOn) || action.equals(actionUserPresent)) && displayVariant == 0)
                    {
                        Intent intentAct = new Intent("android.intent.action.MAIN");
                        intentAct.setClass(LexiconService.this, ServiceDialog.class);
                        intentAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentAct);
                    }
                }
            }
        }
    }
}
