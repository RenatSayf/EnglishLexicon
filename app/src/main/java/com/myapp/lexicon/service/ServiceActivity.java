package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Pair;

import com.google.gson.Gson;
import com.myapp.lexicon.R;
import com.myapp.lexicon.database.AppDB;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.main.MainActivity;
import com.myapp.lexicon.main.MainActivityOnStart;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.myapp.lexicon.main.MainActivity.serviceIntent;

/**
 * Created by Renat
 */

@AndroidEntryPoint
public class ServiceActivity extends AppCompatActivity
{
    private int displayVariant = 0;
    private boolean isServiceEnabled = false;
    public static IStopServiceByUser iStopServiceByUser;
    public static TextToSpeech speech;
    public static HashMap<String, String> map = new HashMap<>();

    private final CompositeDisposable composite = new CompositeDisposable();

    public interface IStopServiceByUser
    {
        void onStoppedByUser();
    }

    public static void setStoppedByUserListener(IStopServiceByUser listener)
    {
        iStopServiceByUser = listener;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_dialog_activity);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ServiceActivity.this);
        String preferencesString = preferences.getString(getString(R.string.key_list_display_mode), "0");
        String displayVariantStr = preferences.getString(getString(R.string.key_display_variant), "0");
        isServiceEnabled = preferences.getBoolean(getString(R.string.key_service), false);
        int displayMode = Integer.parseInt(preferencesString);
        displayVariant = Integer.parseInt(displayVariantStr);

        if (displayVariant == 1 && serviceIntent != null)
        {
            stopService(MainActivity.serviceIntent);
        }

        speech = new TextToSpeech(this, status -> {
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
                stopService(serviceIntent);
            }
        });

        if (displayMode == 0)
        {
            String json = getIntent().getStringExtra(AppData.ARG_JSON);
            ModalFragment modalFragment = ModalFragment.newInstance(json);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, modalFragment).commit();
        }
        else if (displayMode == 1)
        {
            String json = getIntent().getStringExtra(AppData.ARG_JSON);
            TestModalFragment testModalFragment = TestModalFragment.newInstance(json);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, testModalFragment).commit();
        } else return;

        AppData appData = AppData.getInstance();

        int count = appData.getUnLookPhoneCount();
        count++;
        appData.setUnLookPhoneCount(count);

        if (appData.isAdMob())
        {
            if (appData.isOnline(this) && appData.getUnLookPhoneCount() > 2)
            {
                if (savedInstanceState == null)
                {
                    appData.setUnLookPhoneCount(0);
                    ModalBannerFragment bannerFragment = new ModalBannerFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.banner_frame_service, bannerFragment).commit();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop()
    {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDetachedFromWindow()
    {
        if (isServiceEnabled)
        {
            if (displayVariant == 1 && !LexiconService.stopedByUser)
            {
                AppSettings appSettings = new AppSettings(this);
                ArrayList<String> playList = appSettings.getPlayList();
                String dictName = playList.get(appSettings.getDictNumber());
                AppDB db = new AppDB(new DatabaseHelper(this));

                composite.add(db.getEntriesAndCountersAsync(dictName, appSettings.getWordNumber(), "ASC")
                        .observeOn(Schedulers.computation())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {

                            if (pair.getSecond().size() > 0)
                            {
                                appSettings.goForward((LinkedList<DataBaseEntry>) pair.getSecond());
                                String json = new Gson().toJson(new Pair(pair.getFirst(), pair.getSecond()));
                                if (MainActivity.serviceIntent == null)
                                {
                                    MainActivity.serviceIntent = new Intent(this, LexiconService.class);
                                }
                                serviceIntent.putExtra(AppData.ARG_JSON, json);
                                startService(MainActivity.serviceIntent);
                                composite.dispose();
                                composite.clear();
                            }
                            if (pair == null || (pair.getFirst().size() == 0 && pair.getSecond().size() == 0))
                            {
                                composite.dispose();
                                composite.clear();
                            }
                        }, throwable -> {
                            composite.dispose();
                            composite.clear();
                            throwable.printStackTrace();
                        }));

            }
            if (LexiconService.stopedByUser)
            {
                if (serviceIntent == null)
                {
                    MainActivity.serviceIntent = new Intent(this, LexiconService.class);
                }
                LexiconService.stopedByUser = false;
            }
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDestroy()
    {
        if (speech != null)
        {
            speech.shutdown();
        }
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainActivityStart(MainActivityOnStart event)
    {
        if (displayVariant == 1 && !LexiconService.stopedByUser)
        {
            Intent serviceIntent = event.intent;
            if (serviceIntent != null)
            {
                stopService(serviceIntent);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStoppedServiceByUser(StopedServiceByUserEvent event)
    {
        if (iStopServiceByUser != null)
        {
            iStopServiceByUser.onStoppedByUser();
        }
        finish();
    }

}
