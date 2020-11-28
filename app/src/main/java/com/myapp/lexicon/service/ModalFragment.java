package com.myapp.lexicon.service;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.GetEntriesFromDbAsync;
import com.myapp.lexicon.database.GetStudiedWordsCount;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.main.MainActivityOnStart;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import kotlin.Pair;

import static com.myapp.lexicon.main.MainActivity.serviceIntent;
import static com.myapp.lexicon.service.ServiceActivity.map;
import static com.myapp.lexicon.service.ServiceActivity.speech;


public class ModalFragment extends Fragment
{
    public static final String ARG_JSON = "ModalFragment.arg_json";

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


    static ModalFragment newInstance(@Nullable String json)
    {
        ModalFragment fragment = new ModalFragment();
        if (json != null)
        {
            Bundle bundle = new Bundle();
            bundle.putString(ARG_JSON, json);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        appSettings = new AppSettings(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View fragmentView = inflater.inflate(R.layout.s_repeat_modal_fragment, container, false);

        enTextView = fragmentView.findViewById(R.id.en_text_view);
        ruTextView = fragmentView.findViewById(R.id.ru_text_view);

        nameDictTV = fragmentView.findViewById(R.id.name_dict_tv);
        wordsNumberTV = fragmentView.findViewById(R.id.words_number_tv_modal_sv);

        FragmentActivity activity = getActivity();
        Bundle arguments = getArguments();
        if (activity != null && arguments != null)
        {
            String json = arguments.getString(ARG_JSON);
            Pair<Map<String, Integer>, List<DataBaseEntry>> pair;
            Type type = new TypeToken<Pair<Map<String, Integer>, List<DataBaseEntry>>>()
            {
            }.getType();

            try
            {
                Object obj = new Gson().fromJson(json, type);
                pair = (Pair<Map<String, Integer>, List<DataBaseEntry>>) obj;
                nameDictTV.setText(pair.getSecond().get(0).getDictName());
                enTextView.setText(pair.getSecond().get(0).getEnglish());
                ruTextView.setText(pair.getSecond().get(0).getTranslate());

                String concatText = (pair.getSecond().get(0).getRowId() + "")
                        .concat(" / ")
                        .concat(pair.getFirst().get("totalWords").toString())
                        .concat("  " + getString(R.string.text_studied) + " " + pair.getFirst().get("studiedWords").toString());
                wordsNumberTV.setText(concatText);
            } catch (JsonSyntaxException e)
            {
                e.printStackTrace();
                Toast.makeText(activity, "JSON parsing error", Toast.LENGTH_LONG).show();
                activity.finish();
            }


//            final int dictNumber = appData.getNdict();
//            if (appSettings.getPlayList() != null && appSettings.getPlayList().size() > dictNumber)
//            {
//                final String currentDict = appSettings.getPlayList().get(dictNumber);
//
//                try
//                {
//
//                    int orderPlay = appSettings.getOrderPlay();
//                    if (orderPlay == 0)
//                    {
//                        getNextWord();
//                    } else if (orderPlay == 1 && getArguments() == null)
//                    {
//                        getRandomWordsFromDB();
//                    } else if (orderPlay == 1 && getArguments() != null)
//                    {
//                        getNextWord();
//                    }
//
//                } catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }

            Button btnStop = fragmentView.findViewById(R.id.btn_stop_service);
            btnStop.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    FragmentActivity activity = getActivity();
                    if (activity != null)
                    {
                        LexiconService.stopedByUser = true;
                        EventBus.getDefault().post(new StopedServiceByUserEvent());
                        new AlarmScheduler(activity).cancel(AlarmScheduler.REQUEST_CODE, AlarmScheduler.REPEAT_SHOOT_ACTION);
                    }
                }
            });

            ImageButton btnClose = fragmentView.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
//                    if (AppData.getInstance().getDoneRepeat() >= repeatCount)
//                    {
//                        AppData.getInstance().setDoneRepeat(1);
//                    } else
//                    {
//                        AppData.getInstance().setDoneRepeat(AppData.getInstance().getDoneRepeat() + 1);
//                    }
                    if (getActivity() != null)
                    {
                        //appData.saveAllSettings(getActivity());

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
                        EventBus.getDefault().postSticky(new MainActivityOnStart(serviceIntent));
                    }
                }
            });

            ImageButton btnSound = fragmentView.findViewById(R.id.btn_sound_modal);
            btnSound_OnClick(btnSound);

            checkBoxRu = fragmentView.findViewById(R.id.check_box_ru_speak_modal);
            checkBoxRu.setChecked(appSettings.isRuSpeechInModal());
            checkBoxRu_OnCheckedChange(checkBoxRu);

            orderPlayIcon = fragmentView.findViewById(R.id.order_play_icon_iv_modal);
        }

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

//    private void getRandomWordsFromDB()
//    {
//        if (appSettings.getPlayList().size() > 0)
//        {
//            RandomNumberGenerator numberGenerator = new RandomNumberGenerator(appSettings.getPlayList().size(), (int) new Date().getTime());
//            int nDict = numberGenerator.generate();
//            final String tableName = appSettings.getPlayList().get(nDict);
//            if (tableName == null) return;
//
//            GetStudiedWordsCount getStudiedWordsCount = new GetStudiedWordsCount(getActivity(), tableName, new GetStudiedWordsCount.GetCountListener()
//            {
//                @Override
//                public void onTaskComplete(Integer[] resArray)
//                {
//                    if (resArray != null && resArray.length > 1)
//                    {
//                        final int totalWords = resArray[3];
//                        final int studiedWords = resArray[2];
//                        if (studiedWords == totalWords && getActivity() != null)
//                        {
//                            appSettings.removeItemFromPlayList(tableName);
//                            getActivity().finish();
//                        }
//
//                        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), tableName, new GetEntriesFromDbAsync.GetEntriesListener()
//                        {
//                            @Override
//                            public void getEntriesListener(ArrayList<DataBaseEntry> entries)
//                            {
//                                int wordsNumber = 0;
//                                if (entries.size() == 1)
//                                {
//                                    wordsNumber = entries.get(0).getRowId();
//                                    enTextView.setText(entries.get(0).getEnglish());
//                                    ruTextView.setText(entries.get(0).getTranslate());
//                                }
//                                if (entries.size() > 1)
//                                {
//                                    wordsNumber = entries.get(0).getRowId();
//                                    enTextView.setText(entries.get(0).getEnglish());
//                                    ruTextView.setText(entries.get(0).getTranslate());
//                                }
//                                nameDictTV.setText(tableName);
//                                try
//                                {
//                                    String concatText = (wordsNumber + "").concat(" / ").concat(Integer.toString(totalWords)).concat(" " + getString(R.string.text_studied) + " " + studiedWords);
//                                    wordsNumberTV.setText(concatText);
//                                } catch (Exception e)
//                                {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                        if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
//                        {
//                            getEntriesFromDbAsync.execute();
//                        }
//                    }
//                }
//            });
//            if (getStudiedWordsCount.getStatus() != AsyncTask.Status.RUNNING)
//            {
//                getStudiedWordsCount.execute();
//            }
//        }
//    }

//    private void getNextWord()
//    {
//        if (appSettings.getPlayList().size() > 0)
//        {
//            GetStudiedWordsCount getStudiedWordsCount = new GetStudiedWordsCount(getActivity(), appData.getPlayList().get(appData.getNdict()), new GetStudiedWordsCount.GetCountListener()
//            {
//                @Override
//                public void onTaskComplete(Integer[] resArray)
//                {
//                    if (resArray != null && resArray.length > 1)
//                    {
//                        final int studiedWords = resArray[2];
//                        final int totalWords = resArray[3];
//                        appData.getNextNword(getActivity(), new AppData.IGetWordListerner()
//                        {
//                            @Override
//                            public void getWordComplete(ArrayList<DataBaseEntry> entries, Integer[] dictSize)
//                            {
//                                if (entries.size() > 0)
//                                {
//                                    DataBaseEntry dataBaseEntry = entries.get(0);
//                                    enTextView.setText(dataBaseEntry.getEnglish());
//                                    ruTextView.setText(dataBaseEntry.getTranslate());
//                                    nameDictTV.setText(appData.getPlayList().get(appData.getNdict()));
//                                    try
//                                    {
//                                        String concatText = (dataBaseEntry.getRowId() + "").concat(" / ").concat(Integer.toString(totalWords)).concat("  " + getString(R.string.text_studied) + " " + studiedWords);
//                                        wordsNumberTV.setText(concatText);
//                                    } catch (Exception e)
//                                    {
//                                        e.printStackTrace();
//                                    }
//                                    repeatCount = Integer.parseInt(dataBaseEntry.getCountRepeat());
//                                }
//                            }
//                        });
//                    }
//                }
//            });
//            if (getStudiedWordsCount.getStatus() != AsyncTask.Status.RUNNING)
//            {
//                getStudiedWordsCount.execute();
//            }
//        }
//    }

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

    private void btnSound_OnClick(ImageButton button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (speech == null || speech.isSpeaking())
                {
                    return;
                }
                String enText = enTextView.getText().toString();
                final String ruText = ruTextView.getText().toString();
                if (!enText.equals(""))
                {
                    speech.speak(enText, TextToSpeech.QUEUE_ADD, map);
                }
                speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
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
                            int res = speech.isLanguageAvailable(Locale.getDefault());
                            if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE)
                            {
                                speech.setLanguage(Locale.getDefault());
                                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.getDefault().getDisplayLanguage());
                                speech.speak(ruText, TextToSpeech.QUEUE_ADD, map);
                            }
                        }
                        if (s.equals(Locale.getDefault().getDisplayLanguage()))
                        {
                            speech.stop();
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.US.getDisplayLanguage());
                            speech.setLanguage(Locale.US);
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
