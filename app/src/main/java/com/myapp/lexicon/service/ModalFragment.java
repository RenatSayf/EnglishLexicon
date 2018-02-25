package com.myapp.lexicon.service;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.GetCountWordsAsync;
import com.myapp.lexicon.database.GetEntriesFromDbAsync;
import com.myapp.lexicon.main.MainActivity;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class ModalFragment extends Fragment
{
    private AppSettings appSettings;
    private AppData appData;
    private TextView enTextView;
    private TextView ruTextView;
    private TextToSpeech speech;
    private HashMap<String, String> map = new HashMap<>();

    public ModalFragment()
    {
        // Required empty public constructor
    }

    public static ModalFragment newInstance()
    {
        return new ModalFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        appSettings = new AppSettings(getContext());
        appData = AppData.getInstance();
        appData.initAllSettings(getActivity());

        speech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS)
                {
                    int resultEn = speech.isLanguageAvailable(Locale.US);
                    if (resultEn != TextToSpeech.LANG_AVAILABLE)
                    {
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.US.getDisplayLanguage());
                        speech.setLanguage(Locale.US);
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View fragmentView = inflater.inflate(R.layout.s_fragment_modal, container, false);

        enTextView = fragmentView.findViewById(R.id.en_text_view);
        ruTextView = fragmentView.findViewById(R.id.ru_text_view);

        int wordNumber = appData.getNword();
        int dictNumber = appData.getNdict();
        String currentDict = appSettings.getPlayList().get(dictNumber);

        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), currentDict, wordNumber, wordNumber, new GetEntriesFromDbAsync.GetEntriesListener()
        {
            @Override
            public void getEntriesListener(ArrayList<DataBaseEntry> entries)
            {
                if (entries.size() > 0)
                {
                    enTextView.setText(entries.get(0).getEnglish());
                    ruTextView.setText(entries.get(0).getTranslate());
                }
            }
        });
        if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getEntriesFromDbAsync.execute();
        }

        Button btnStop = fragmentView.findViewById(R.id.btn_stop_service);
        btnStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FragmentActivity activity = getActivity();
                if (activity != null)
                {
                    LexiconService.isStop = true;
                    activity.stopService(MainActivity.serviceIntent);
                    activity.finish();
                }
            }
        });

        ImageButton btnClose = fragmentView.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final int nextWord = appData.getNword() + 1;
                String currentDict = appSettings.getPlayList().get(appData.getNdict());
                GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), currentDict, new GetCountWordsAsync.GetCountListener()
                {
                    @Override
                    public void onTaskComplete(int count)
                    {
                        if (appSettings.getPlayList().size() == 1)
                        {
                            if (nextWord > count)
                            {
                                appData.setNword(1);
                            }
                            else if (nextWord <= count)
                            {
                                appData.setNword(nextWord);
                            }
                        }
                        if (appSettings.getPlayList().size() > 1)
                        {
                            int dictNumber = appData.getNdict();
                            if (nextWord > count)
                            {
                                appData.setNword(1);
                                appData.setNdict(dictNumber + 1);
                                if (appData.getNdict() > appSettings.getPlayList().size() - 1)
                                {
                                    appData.setNdict(0);
                                }
                            }
                            else if (nextWord <= count)
                            {
                                appData.setNword(nextWord);
                                appData.setNdict(dictNumber);
                            }
                        }
                        appData.saveAllSettings(getActivity());
                        getActivity().finish();
                    }
                });
                if (getCountWordsAsync.getStatus() != AsyncTask.Status.RUNNING)
                {
                    getCountWordsAsync.execute();
                }
            }
        });

        Button btnOpenApp = fragmentView.findViewById(R.id.btn_open_app);
        btnOpenApp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getActivity().startActivity(new Intent(getContext(), SplashScreenActivity.class));
                getActivity().finish();
            }
        });

        ImageButton btnSound = fragmentView.findViewById(R.id.btn_sound_modal);
        btnSound_OnClick(btnSound);

        return fragmentView;
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        speech.shutdown();
    }

    public void btnSound_OnClick(ImageButton button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String enText = enTextView.getText().toString();
                if (!enText.equals(""))
                {
                    speech.speak(enText, TextToSpeech.QUEUE_ADD, map);
                }
            }
        });

    }
}
