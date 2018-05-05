package com.myapp.lexicon.service;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.GetCountWordsAsync;
import com.myapp.lexicon.database.GetEntriesFromDbAsync;
import com.myapp.lexicon.database.UpdateDBEntryAsync;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.main.MainActivity;
import com.myapp.lexicon.main.SplashScreenActivity;
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

    private TextView wordsNumberTV;
    private int wordsCount;
    private ArrayList<DataBaseEntry> compareList;
    private int repeatCount;
    private boolean wordIsStudied = false;

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

        ImageButton speakButton = fragmentView.findViewById(R.id.btn_sound_modal);
        speakButton_OnClick(speakButton);

        final String currentDict = appSettings.getPlayList().get(appData.getNdict());

        nameDictTV.setText(currentDict);

        //getWordsFromDB(currentDict);
        getWordsFromDB2(currentDict);

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

        Button btnOpenApp = fragmentView.findViewById(R.id.btn_open_app);
        btnOpenApp_OnClick(btnOpenApp);

        Button btnStopService = fragmentView.findViewById(R.id.btn_stop_service);
        btnStopService_OnClick(btnStopService);

        checkStudied_OnCheckedChange((CheckBox) fragmentView.findViewById(R.id.check_box_studied));

        return fragmentView;
    }

    private void getWordsFromDB(final String currentDict)
    {
        GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), currentDict, new GetCountWordsAsync.GetCountListener()
        {
            int wordNumber = appData.getNword();
            @Override
            public void onTaskComplete(int count)
            {
                wordsCount = count;
                try
                {
                    wordsNumberTV.setText((wordNumber + "").concat(" / ").concat(Integer.toString(wordsCount)));
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
                                try
                                {
                                    repeatCount = Integer.parseInt(entries.get(0).getCountRepeat());
                                } catch (NumberFormatException e)
                                {
                                    repeatCount = 1;
                                }
                                if (repeatCount == 0)
                                {
                                    nextWord();
                                    getWordsFromDB(currentDict);
                                    return;
                                }

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
    }

    private void getWordsFromDB2(final String currentDict)
    {
        GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), currentDict, false, new GetCountWordsAsync.GetCountListener()
        {
            int firstId = appData.getNword();

            @Override
            public void onTaskComplete(final int count)
            {
                if (count == 0 && getActivity() != null)
                {
                    getActivity().finish();
                }
                int randomId;
                try
                {
                    wordsNumberTV.setText((firstId + "").concat(" / ").concat(Integer.toString(count)));
                    RandomNumberGenerator numberGenerator = new RandomNumberGenerator(1, count, (int) new Date().getTime());
                    randomId = numberGenerator.generate();
                    if (count >= 2)
                    {
                        while (firstId == randomId)
                        {
                            randomId = numberGenerator.generate();
                        }
                    }
                    else if (count > 0)
                    {
                        randomId = numberGenerator.generate();
                    }

                    if (count > 0)
                    {
                        final int finalRandomId = randomId;
                        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), currentDict, firstId, randomId, true, new GetEntriesFromDbAsync.GetEntriesListener()
                        {
                            @Override
                            public void getEntriesListener(ArrayList<DataBaseEntry> entries)
                            {
                                compareList = entries;
                                if (entries.size() == 1 && !entries.get(0).getCountRepeat().equals("0"))
                                {
                                    enTextView.setText(entries.get(0).getEnglish());
                                    ruBtn1.setText(entries.get(0).getTranslate());
                                    ruBtn2.setText(entries.get(0).getTranslate());
                                    if (appData.getNword() + 1 <= count)
                                    {
                                        appData.setNword(appData.getNword() + 1);
                                    }
                                    else
                                    {
                                        appData.setNword(1);
                                        if (appData.getNdict() + 1 > appSettings.getPlayList().size() - 1)
                                        {
                                            appData.setNdict(0);
                                        }
                                        else if (appData.getNdict() >= 0 && appData.getNdict() <  appSettings.getPlayList().size() - 1)
                                        {
                                            appData.setNdict(appData.getNdict() + 1);
                                        }
                                        else
                                        {
                                            appData.setNdict(appData.getNdict());
                                        }
                                    }
                                }
                                if (entries.size() == 2)
                                {
                                    RandomNumberGenerator numberGenerator = new RandomNumberGenerator(2, (int) new Date().getTime());
                                    int i = numberGenerator.generate();
                                    int j = numberGenerator.generate();
                                    for (DataBaseEntry item : entries)
                                    {
                                        if (item.getRowId() == firstId) enTextView.setText(item.getEnglish());
                                    }
                                    ruBtn1.setText(entries.get(i).getTranslate());
                                    ruBtn2.setText(entries.get(j).getTranslate());

                                    if (appData.getNword() + 1 <= count)
                                    {
                                        appData.setNword(appData.getNword() + 1);
                                    }
                                    else
                                    {
                                        appData.setNword(1);
                                        if (appData.getNdict() + 1 > appSettings.getPlayList().size() - 1)
                                        {
                                            appData.setNdict(0);
                                        }
                                        else if (appData.getNdict() >= 0 && appData.getNdict() <  appSettings.getPlayList().size() - 1)
                                        {
                                            appData.setNdict(appData.getNdict() + 1);
                                        }
                                        else
                                        {
                                            appData.setNdict(appData.getNdict());
                                        }
                                    }
                                }
                                if (entries.size() == 3)
                                {
                                    RandomNumberGenerator numberGenerator = new RandomNumberGenerator(2, (int) new Date().getTime());
                                    int i = numberGenerator.generate();
                                    int j = numberGenerator.generate();
                                    for (DataBaseEntry item : entries)
                                    {
                                        if (item.getRowId() == firstId)
                                        {
                                            enTextView.setText(item.getEnglish());
                                            if (i == 0 && j == 1)
                                                ruBtn1.setText(item.getTranslate());
                                            else
                                                ruBtn2.setText(item.getTranslate());
                                        }
                                        if (item.getRowId() == finalRandomId)
                                        {
                                            if (i == 0 && j == 1)
                                            {
                                                ruBtn2.setText(item.getTranslate());
                                            }
                                            else
                                            {
                                                ruBtn1.setText(item.getTranslate());
                                            }
                                        }
                                        if (item.getRowId() > firstId && item.getRowId() != finalRandomId && appSettings.getOrderPlay() == 0)
                                        {
                                            appData.setNword(item.getRowId());
                                        }
                                    }
                                }
                            }
                        });
                        if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
                        {
                            getEntriesFromDbAsync.execute();
                        }
                    }
                }
                catch (Exception e)
                {
                    wordsNumberTV.setText("???");
                }
            }
        });
        if (getCountWordsAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getCountWordsAsync.execute();
        }
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
                    if (wordIsStudied)
                    {
                        String dict = appSettings.getPlayList().get(AppData.getInstance().getNdict());
                        DataBaseEntry entry = new DataBaseEntry(enTextView.getText().toString(), button.getText().toString(), "0");
                        UpdateDBEntryAsync updateDBEntryAsync = new UpdateDBEntryAsync(getActivity(), dict, entry, new UpdateDBEntryAsync.IUpdateDBListener()
                        {
                            @Override
                            public void updateDBEntry_OnComplete(int rows)
                            {
                                if (rows > 0 && getActivity() != null)
                                {
                                    Toast.makeText(getActivity(), R.string.text_word_is_not_show, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        if (updateDBEntryAsync.getStatus() != AsyncTask.Status.RUNNING)
                        {
                            updateDBEntryAsync.execute();
                        }
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                if (getActivity() != null)
                {
                    button.setBackgroundResource(R.drawable.btn_for_test_modal_transp);
                    button.setTextColor(getActivity().getResources().getColor(R.color.colorLightGreen));

                    if (AppData.getInstance().getDoneRepeat() >= repeatCount)
                    {
                        AppData.getInstance().setDoneRepeat(1);
                    }
                    else
                    {
                        AppData.getInstance().setDoneRepeat(AppData.getInstance().getDoneRepeat() + 1);
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
                    wordIsStudied = false;
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

    private void speakButton_OnClick(View view)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (LexiconService.speech.isSpeaking())
                {
                    return;
                }
                String enText = enTextView.getText().toString();
                if (!enText.equals(""))
                {
                    LexiconService.speech.speak(enText, TextToSpeech.QUEUE_ADD, LexiconService.map);
                }
            }
        });
    }

    private void btnOpenApp_OnClick(View view)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (getActivity() != null)
                {
                    getActivity().startActivity(new Intent(getContext(), SplashScreenActivity.class));
                    getActivity().finish();
                }
            }
        });
    }

    private void btnStopService_OnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
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
    }

    private void checkStudied_OnCheckedChange(CheckBox checkBox)
    {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                wordIsStudied = isChecked;
            }
        });
    }



}
