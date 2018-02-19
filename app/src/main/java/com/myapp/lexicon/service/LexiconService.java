package com.myapp.lexicon.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class LexiconService extends Service
{
    private static Timer timer;
    Handler handler;

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

        handler = new ServiceHandler(this);
        startService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handler.sendEmptyMessage(0);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        timer.cancel();
        Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show();
        if (handler != null)
        {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void startService()
    {
        timer = new Timer();
        timer.schedule(new timerTask(), 0, 15000);
    }

    private class timerTask extends TimerTask
    {
        public void run()
        {
            handler.sendEmptyMessage(0);
        }
    }
}
