package com.myapp.lexicon.service;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
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
                    int endId;

                    RandomNumberGenerator numberGenerator = new RandomNumberGenerator(1, wordsCount, (int) new Date().getTime());
                    endId = numberGenerator.generate();
                    while (wordNumber == endId)
                    {
                        endId = numberGenerator.generate();
                    }
                    int[] idArray = {wordNumber, endId};
                    GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), currentDict, idArray, new GetEntriesFromDbAsync.GetEntriesListener()
                    {
                        @Override
                        public void getEntriesListener(ArrayList<DataBaseEntry> entries)
                        {
                            if (entries != null && entries.size() > 0)
                            {
                                compareList = new ArrayList<>();
                                compareList.add(entries.get(0));
                                compareList.add(entries.get(entries.size() - 1));
                                if (compareList.size() > 0 && wordNumber > 0)
                                {
                                    RandomNumberGenerator numberGenerator = new RandomNumberGenerator(compareList.size(), (int) new Date().getTime());
                                    int i = numberGenerator.generate();
                                    int j = numberGenerator.generate();
                                    enTextView.setText(compareList.get(0).getEnglish());
                                    ruBtn1.setText(compareList.get(i).getTranslate());
                                    ruBtn2.setText(compareList.get(j).getTranslate());
                                }
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

        ImageButton btnClose = fragmentView.findViewById(R.id.modal_btn_close);
        btnClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (getActivity() != null)
                {
                    getActivity().finish();
                }
            }
        });

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
                boolean result = compareWords(compareList, english, trnslate);
                if (result)
                {
                    rightAnswerAnim(button);
                }
                else
                {
                    noRightAnswerAnim(button);
                }
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
                boolean result = compareWords(compareList, english, trnslate);
                if (result)
                {
                    rightAnswerAnim(button);
                }
                else
                {
                    noRightAnswerAnim(button);
                }
            }
        });
    }

    private boolean compareWords(ArrayList<DataBaseEntry> compareList, String english, String translate)
    {
        boolean result = false;
        if (compareList != null && compareList.size() > 0)
        {
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
        }
        else
        {
            result = true;
        }
        return result;
    }

    private void nextWord()
    {
        int nextWord = appData.getNword() + 1;
        if (appSettings.getPlayList().size() == 1)
        {
            if (nextWord > wordsCount)
            {
                appData.setNword(1);
            } else if (nextWord <= wordsCount)
            {
                appData.setNword(nextWord);
            }
        }
        if (appSettings.getPlayList().size() > 1)
        {
            int dictNumber = appData.getNdict();
            if (nextWord > wordsCount)
            {
                appData.setNword(1);
                appData.setNdict(dictNumber + 1);
                if (appData.getNdict() > appSettings.getPlayList().size() - 1)
                {
                    appData.setNdict(0);
                }
            } else if (nextWord <= wordsCount)
            {
                appData.setNword(nextWord);
                appData.setNdict(dictNumber);
            }
        }
        if (getActivity() != null)
        {
            appData.saveAllSettings(getActivity());
            getActivity().finish();
        } else
        {
            onDestroy();
            onDetach();
        }
    }

    private void rightAnswerAnim(final Button button)
    {
        Animation animRight = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_right);
        animRight.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                if (getActivity() != null)
                {
                    button.setBackgroundResource(R.drawable.btn_for_test_modal_green);
                    button.setTextColor(getActivity().getResources().getColor(R.color.colorWhite));
                }
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                if (getActivity() != null)
                {
                    button.setBackgroundResource(R.drawable.btn_for_test_modal_transp);
                    button.setTextColor(getActivity().getResources().getColor(R.color.colorLightGreen));
                    nextWord();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        button.startAnimation(animRight);
    }

    private void noRightAnswerAnim(final Button button)
    {
        Animation animNotRight = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_not_right);
        animNotRight.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                if (getActivity() != null)
                {
                    button.setBackgroundResource(R.drawable.btn_for_test_modal_red);
                    button.setTextColor(getActivity().getResources().getColor(R.color.colorWhite));
                }
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                if (getActivity() != null)
                {
                    button.setBackgroundResource(R.drawable.btn_for_test_modal_transp);
                    button.setTextColor(getActivity().getResources().getColor(R.color.colorLightGreen));
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        button.startAnimation(animNotRight);
    }
}
