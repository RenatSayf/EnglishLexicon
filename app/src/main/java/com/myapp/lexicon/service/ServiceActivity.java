package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.splash.SplashActivity;

import java.util.Arrays;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;



@AndroidEntryPoint
public class ServiceActivity extends AppCompatActivity implements IModalFragment
{
    public static Listener listener;
    public static final String ARG_JSON = ServiceActivity.class.getCanonicalName() + ".ARG_JSON";

    @Override
    public void openApp()
    {
        finish();
        startActivity(new Intent(this, SplashActivity.class));
    }

    public interface Listener
    {
        void onStoppedByUser();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_dialog_activity);

        MainViewModel vm = new ViewModelProvider(this).get(MainViewModel.class);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ServiceActivity.this);
        String preferencesString = preferences.getString(getString(R.string.key_list_display_mode), "0");
        String displayVariantStr = preferences.getString(getString(R.string.key_display_variant), "0");
        int displayMode = 0;
        try
        {
            displayMode = Integer.parseInt(preferencesString);
        } catch (NumberFormatException e)
        {
            e.printStackTrace();
        }

        int finalDisplayMode = displayMode;

        String json = getIntent().getStringExtra(ServiceActivity.ARG_JSON);
        if (json != null)
        {
            Word[] words = StringOperations.getInstance().jsonToWord(json);
            if (words.length > 0)
            {
                String dictName = words[0].getDictName();
                int id = words[0].get_id();
                vm.getCountersById(dictName, id);
            }
            vm.getWordCounters().observe(this, counters -> {
                if (counters != null && counters.size() > 1)
                {

                    if (finalDisplayMode == 0)
                    {
                        ModalFragment modalFragment = ModalFragment.newInstance(json, counters, this);
                        modalFragment.show(getSupportFragmentManager().beginTransaction(), ModalFragment.TAG);
                        vm.goForward(Arrays.asList(words));
                    }
                    else if (finalDisplayMode == 1)
                    {
                        TestModalFragment testModalFragment = TestModalFragment.newInstance(json, counters, this);
                        testModalFragment.show(getSupportFragmentManager().beginTransaction(), TestModalFragment.TAG);
                    }
                }
            });
        }
        else finish();




    }


    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
    }


    public void stopAppService()
    {
        if (listener != null)
        {
            listener.onStoppedByUser();
            new AlarmScheduler(this).cancel(AlarmScheduler.ONE_SHOOT_ACTION);
            finish();
        }
    }


}
