package com.myapp.lexicon.service;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.GetEntriesFromDbAsync;
import com.myapp.lexicon.database.GetStudiedWordsCount;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.main.MainActivity;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class ModalFragment extends Fragment
{
    private AppSettings appSettings;
    private AppData appData;
    private TextView enTextView;
    private TextView ruTextView;
    private CheckBox checkBoxRu;
    private TextView wordsNumberTV;
    private TextView nameDictTV;
    private ImageView orderPlayIcon;
    private int repeatCount;

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
        final View fragmentView = inflater.inflate(R.layout.s_repeat_modal_fragment, container, false);

        enTextView = fragmentView.findViewById(R.id.en_text_view);
        ruTextView = fragmentView.findViewById(R.id.ru_text_view);

        nameDictTV = fragmentView.findViewById(R.id.name_dict_tv);
        wordsNumberTV = fragmentView.findViewById(R.id.words_number_tv_modal_sv);

        final int dictNumber = appData.getNdict();
        if (appSettings.getPlayList() != null && appSettings.getPlayList().size() > dictNumber)
        {
            final String currentDict = appSettings.getPlayList().get(dictNumber);

            nameDictTV.setText(currentDict);

            try
            {
                nameDictTV.setText(currentDict);
                int orderPlay = appSettings.getOrderPlay();
                switch (orderPlay)
                {
                    case 0:
                        getNextWord();
                        break;
                    case 1:
                        getRandomWordsFromDB();
                        break;
                }

            } catch (Exception e)
            {
                e.printStackTrace();
            }
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
                }
            }
        });

        Button btnOpenApp = fragmentView.findViewById(R.id.btn_open_app);
        btnOpenApp.setOnClickListener(new View.OnClickListener()
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

        ImageButton btnSound = fragmentView.findViewById(R.id.btn_sound_modal);
        btnSound_OnClick(btnSound);

        checkBoxRu = fragmentView.findViewById(R.id.check_box_ru_speak_modal);
        checkBoxRu.setChecked(appSettings.isRuSpeechInModal());
        checkBoxRu_OnCheckedChange(checkBoxRu);

        orderPlayIcon = fragmentView.findViewById(R.id.order_play_icon_iv_modal);

        return fragmentView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (appSettings.getOrderPlay() == 0)
        {
            orderPlayIcon.setImageResource(R.drawable.ic_repeat_white);
        }
        if (appSettings.getOrderPlay() == 1)
        {
            orderPlayIcon.setImageResource(R.drawable.ic_shuffle_white);
        }
    }

    private void getRandomWordsFromDB()
    {
        if (appSettings.getPlayList().size() > 0)
        {
            RandomNumberGenerator numberGenerator = new RandomNumberGenerator(appSettings.getPlayList().size(), (int) new Date().getTime());
            int nDict = numberGenerator.generate();
            final String tableName = appSettings.getPlayList().get(nDict);
            if (tableName == null) return;

            GetStudiedWordsCount getStudiedWordsCount = new GetStudiedWordsCount(getActivity(), tableName, new GetStudiedWordsCount.GetCountListener()
            {
                @Override
                public void onTaskComplete(Integer[] resArray)
                {
                    if (resArray != null && resArray.length > 1)
                    {
                        final int totalWords = resArray[3];
                        final int studiedWords = resArray[2];
                        if (studiedWords == totalWords && getActivity() != null)
                        {
                            appSettings.removeItemFromPlayList(tableName);
                            getActivity().finish();
                        }

                        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), tableName, new GetEntriesFromDbAsync.GetEntriesListener()
                        {
                            @Override
                            public void getEntriesListener(ArrayList<DataBaseEntry> entries)
                            {
                                int wordsNumber = 0;
                                if (entries.size() == 1)
                                {
                                    wordsNumber = entries.get(0).getRowId();
                                    enTextView.setText(entries.get(0).getEnglish());
                                    ruTextView.setText(entries.get(0).getTranslate());
                                }
                                if (entries.size() > 1)
                                {
                                    wordsNumber = entries.get(0).getRowId();
                                    enTextView.setText(entries.get(0).getEnglish());
                                    ruTextView.setText(entries.get(0).getTranslate());
                                }
                                nameDictTV.setText(tableName);
                                wordsNumberTV.setText((wordsNumber + "").concat(" / ").concat(Integer.toString(totalWords)).concat(" " + getString(R.string.text_studied ) + " " + studiedWords));
                            }
                        });
                        if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
                        {
                            getEntriesFromDbAsync.execute();
                        }
                    }
                }
            });
            if (getStudiedWordsCount.getStatus() != AsyncTask.Status.RUNNING)
            {
                getStudiedWordsCount.execute();
            }
        }
    }

    private void getNextWord()
    {
        if (appSettings.getPlayList().size() > 0)
        {
            GetStudiedWordsCount getStudiedWordsCount = new GetStudiedWordsCount(getActivity(), appData.getPlayList().get(appData.getNdict()), new GetStudiedWordsCount.GetCountListener()
            {
                @Override
                public void onTaskComplete(Integer[] resArray)
                {
                    if (resArray != null && resArray.length > 1)
                    {
                        final int studiedWords = resArray[2];
                        final int totalWords = resArray[3];
                        appData.getNextNword(getActivity(), new AppData.IGetWordListerner()
                        {
                            @Override
                            public void getWordComplete(ArrayList<DataBaseEntry> entries, Integer[] dictSize)
                            {
                                if (entries.size() > 0)
                                {
                                    DataBaseEntry dataBaseEntry = entries.get(0);
                                    enTextView.setText(dataBaseEntry.getEnglish());
                                    ruTextView.setText(dataBaseEntry.getTranslate());
                                    nameDictTV.setText(appData.getPlayList().get(appData.getNdict()));
                                    String concatText = (dataBaseEntry.getRowId() + "").concat(" / ").concat(Integer.toString(totalWords)).concat("  " + getString(R.string.text_studied) + " " + studiedWords);
                                    wordsNumberTV.setText(concatText);
                                    repeatCount = Integer.parseInt(dataBaseEntry.getCountRepeat());
                                }
                            }
                        });
                    }
                }
            });
            if (getStudiedWordsCount.getStatus() != AsyncTask.Status.RUNNING)
            {
                getStudiedWordsCount.execute();
            }
        }
    }

    private void checkBoxRu_OnCheckedChange(CheckBox checkBoxRu)
    {
        checkBoxRu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                appSettings.setRuSpeechInModal(isChecked);
            }
        });
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
    }

    public void btnSound_OnClick(ImageButton button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (LexiconService.speech.isSpeaking())
                {
                    return;
                }
                String enText = enTextView.getText().toString();
                final String ruText = ruTextView.getText().toString();
                if (!enText.equals(""))
                {
                    LexiconService.speech.speak(enText, TextToSpeech.QUEUE_ADD, LexiconService.map);
                }
                LexiconService.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
                {
                    @Override
                    public void onStart(String s)
                    {

                    }

                    @Override
                    public void onDone(String s)
                    {
                        if (checkBoxRu.isChecked() && !ruText.equals("") && s.equals(Locale.US.getDisplayLanguage()))
                        {
                            int res = LexiconService.speech.isLanguageAvailable(Locale.getDefault());
                            if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE)
                            {
                                LexiconService.speech.setLanguage(Locale.getDefault());
                                LexiconService.map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.getDefault().getDisplayLanguage());
                                LexiconService.speech.speak(ruText, TextToSpeech.QUEUE_ADD, LexiconService.map);
                            }
                        }
                        if (s.equals(Locale.getDefault().getDisplayLanguage()))
                        {
                            LexiconService.speech.stop();
                            LexiconService.map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.US.getDisplayLanguage());
                            LexiconService.speech.setLanguage(Locale.US);
                        }
                    }

                    @Override
                    public void onError(String s)
                    {

                    }
                });
            }
        });
    }
}
