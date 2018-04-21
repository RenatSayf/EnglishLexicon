package com.myapp.lexicon.service;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.GetCountWordsAsync;
import com.myapp.lexicon.database.GetEntriesAsyncFragm;
import com.myapp.lexicon.database.GetEntriesFromDbAsync;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.Date;


public class TestModalFragment extends Fragment implements GetEntriesAsyncFragm.OnGetEntriesFinishListener
{
    private AppSettings appSettings;
    private AppData appData;
    private TextView enTextView;
    private Button ruBtn1, ruBtn2;

    private CheckBox checkBoxRu;
    private TextView wordsNumberTV;
    private int wordsCount;

    public TestModalFragment()
    {
        // Required empty public constructor
    }

    public static TestModalFragment newInstance()
    {
        return new TestModalFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null)
        {
            appSettings = new AppSettings(getActivity());
            appData = AppData.getInstance();
            appData.initAllSettings(getActivity());
        } else
        {
            onDestroy();
            onDetach();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.s_test_modal_fragment, container, false);
        enTextView = fragmentView.findViewById(R.id.en_text_view);
        ruBtn1 = fragmentView.findViewById(R.id.ru_btn_1);
        ruBtn2 = fragmentView.findViewById(R.id.ru_btn_2);

        TextView nameDictTV = fragmentView.findViewById(R.id.name_dict_tv);
        wordsNumberTV = fragmentView.findViewById(R.id.words_number_tv_modal_sv);

        final int wordNumber = appData.getNword();
        final int dictNumber = appData.getNdict();
        String currentDict = appSettings.getPlayList().get(dictNumber);

        nameDictTV.setText(currentDict);

        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), currentDict, wordNumber, wordNumber + 1, new GetEntriesFromDbAsync.GetEntriesListener()
        {
            @Override
            public void getEntriesListener(ArrayList<DataBaseEntry> entries)
            {
                if (entries.size() > 0)
                {
                    RandomNumberGenerator numberGenerator = new RandomNumberGenerator(entries.size(), (int) new Date().getTime());
                    int i = numberGenerator.generate();
                    int j = numberGenerator.generate();
                    enTextView.setText(entries.get(0).getEnglish());
                    ruBtn1.setText(entries.get(i).getTranslate());
                    ruBtn2.setText(entries.get(j).getTranslate());
                }
            }
        });
        if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getEntriesFromDbAsync.execute();
        }

        GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), currentDict, new GetCountWordsAsync.GetCountListener()
        {
            @Override
            public void onTaskComplete(int count)
            {
                wordsCount = count;
                try
                {
                    wordsNumberTV.setText(Integer.toString(wordNumber).concat(" / ").concat(Integer.toString(wordsCount)));
                } catch (Exception e)
                {
                    wordsNumberTV.setText("???");
                }
            }
        });
        if (getCountWordsAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getCountWordsAsync.execute();
        }

        return fragmentView;
    }

    @Override
    public void onGetEntriesFinish(ArrayList<DataBaseEntry> arrayList)
    {
        return;
    }
}
