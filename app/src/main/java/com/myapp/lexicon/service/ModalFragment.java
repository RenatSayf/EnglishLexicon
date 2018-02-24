package com.myapp.lexicon.service;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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


public class ModalFragment extends Fragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private AppSettings appSettings;
    private AppData appData;


    public ModalFragment()
    {
        // Required empty public constructor
    }

    public static ModalFragment newInstance(String param1, String param2)
    {
        ModalFragment fragment = new ModalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        appSettings = new AppSettings(getContext());
        appData = AppData.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.s_fragment_modal, container, false);
        final TextView enTextView = fragmentView.findViewById(R.id.en_text_view);
        final TextView ruTextView = fragmentView.findViewById(R.id.ru_text_view);

        int wordNumber = appSettings.getWordNumber();
        String currentDict = appSettings.getPlayList().get(appData.getNdict());
        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), currentDict, appSettings.getWordNumber(), appSettings.getWordNumber(), new GetEntriesFromDbAsync.GetEntriesListener()
        {
            @Override
            public void getEntriesListener(ArrayList<DataBaseEntry> entries)
            {
                enTextView.setText(entries.get(0).getEnglish());
                ruTextView.setText(entries.get(0).getTranslate());
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
                getActivity().finish();
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

        return fragmentView;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        int orderPlay = appSettings.getOrderPlay();
        int tempNextWord = 0;
        switch (orderPlay)
        {
            case 0:
                tempNextWord = appSettings.getWordNumber() + 1;
                break;
            case 1:
                tempNextWord = appSettings.getWordNumber() - 1;
                break;
            default:
                tempNextWord = appSettings.getWordNumber() + 1;
                break;
        }
        final int nextWord = tempNextWord;
        String currentDict = appSettings.getPlayList().get(appSettings.getDictNumber());

        GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), currentDict, new GetCountWordsAsync.GetCountListener()
        {
            @Override
            public void onTaskComplete(int count)
            {
                if (nextWord > count)
                {
                    appSettings.setWordNumber(1);
                }
                else if (nextWord < 1)
                {
                    appSettings.setWordNumber(count);
                }
                else
                {
                    appSettings.setWordNumber(nextWord);
                }
            }
        });
        if (getCountWordsAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getCountWordsAsync.execute();
        }
    }
}
