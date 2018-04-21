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
import com.myapp.lexicon.database.GetEntriesFromDbAsync;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.Date;


public class TestModalFragment extends Fragment
{
    private AppSettings appSettings;
    private AppData appData;
    private TextView enTextView;
    private Button ruBtn1, ruBtn2;

    private CheckBox checkBoxRu;
    private TextView wordsNumberTV;
    private int wordsCount;
    private ArrayList<DataBaseEntry> compareList;

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
        ruBtn1_OnClick(ruBtn1);
        ruBtn2 = fragmentView.findViewById(R.id.ru_btn_2);
        ruBtn2_OnClick(ruBtn2);

        TextView nameDictTV = fragmentView.findViewById(R.id.name_dict_tv);
        wordsNumberTV = fragmentView.findViewById(R.id.words_number_tv_modal_sv);

        final int wordNumber = appData.getNword();
        final int dictNumber = appData.getNdict();
        final String currentDict = appSettings.getPlayList().get(dictNumber);

        nameDictTV.setText(currentDict);

        GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), currentDict, new GetCountWordsAsync.GetCountListener()
        {
            @Override
            public void onTaskComplete(int count)
            {
                wordsCount = count;
                try
                {
                    wordsNumberTV.setText(Integer.toString(wordNumber).concat(" / ").concat(Integer.toString(wordsCount)));

                    int endId = wordNumber + 5;
                    if (endId > wordsCount)
                    {
                        endId = wordsCount - wordNumber;
                    }
                    GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), currentDict, wordNumber, endId, new GetEntriesFromDbAsync.GetEntriesListener()
                    {
                        @Override
                        public void getEntriesListener(ArrayList<DataBaseEntry> entries)
                        {
                            compareList = new ArrayList<>();
                            compareList.add(entries.get(0));
                            compareList.add(entries.get(entries.size() - 1));
                            if (compareList.size() > 0)
                            {
                                RandomNumberGenerator numberGenerator = new RandomNumberGenerator(compareList.size(), (int) new Date().getTime());
                                int i = numberGenerator.generate();
                                int j = numberGenerator.generate();
                                enTextView.setText(entries.get(0).getEnglish());
                                ruBtn1.setText(compareList.get(i).getTranslate());
                                ruBtn2.setText(compareList.get(j).getTranslate());
                            }
                        }
                    });
                    if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
                    {
                        getEntriesFromDbAsync.execute();
                    }

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


    public void ruBtn1_OnClick(View view)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Button button = (Button) view;
                String trnslate = button.getText().toString().toLowerCase();
                String english = enTextView.getText().toString().toLowerCase();
                compareWords(compareList, english, trnslate);

            }
        });
    }

    public void ruBtn2_OnClick(View view)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Button button = (Button) view;
                String trnslate = button.getText().toString().toLowerCase();
                String english = enTextView.getText().toString().toLowerCase();
                compareWords(compareList, english, trnslate);
            }
        });
    }

    private boolean compareWords(ArrayList<DataBaseEntry> compareList, String english, String translate)
    {
        boolean result = false;
        for (int i = 0; i < compareList.size(); i++)
        {
            String enText = compareList.get(i).getEnglish().toLowerCase();
            String ruText = compareList.get(i).getTranslate().toLowerCase();
            if (enText.equals(english.toLowerCase()) && ruText.equals(translate.toLowerCase()))
            {
                result = true;
                break;
            }
        }
        return result;
    }
}
