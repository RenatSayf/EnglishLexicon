package com.myapp.lexicon.service;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;


public class TestModalFragment extends Fragment
{
    private AppSettings appSettings;
    private AppData appData;
    private TextView enTextView;

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
        appSettings = new AppSettings(getContext());
        appData = AppData.getInstance();
        appData.initAllSettings(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.s_test_modal_fragment, container, false);
        enTextView = fragmentView.findViewById(R.id.en_text_view);

        TextView nameDictTV = fragmentView.findViewById(R.id.name_dict_tv);
        wordsNumberTV = fragmentView.findViewById(R.id.words_number_tv_modal_sv);

        final int wordNumber = appData.getNword();
        final int dictNumber = appData.getNdict();
        String currentDict = appSettings.getPlayList().get(dictNumber);

        nameDictTV.setText(currentDict);

        return fragmentView;
    }

}
