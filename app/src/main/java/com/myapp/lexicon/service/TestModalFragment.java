package com.myapp.lexicon.service;


import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.database.GetStudiedWordsCount;
import com.myapp.lexicon.database.UpdateDBEntryAsync;
import com.myapp.lexicon.dialogs.WordsEndedDialog;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.main.MainActivityOnStart;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Pair;

import static com.myapp.lexicon.main.MainActivity.serviceIntent;
import static com.myapp.lexicon.service.ServiceActivity.map;
import static com.myapp.lexicon.service.ServiceActivity.speech;


@AndroidEntryPoint
public class TestModalFragment extends Fragment
{

    private AppSettings appSettings;
    //private AppData appData;
    private TextView enTextView;
    private Button ruBtn1, ruBtn2;
    private TextView nameDictTV;
    private List<DataBaseEntry> compareList;
    private boolean wordIsStudied = false;
    private boolean isWordsEnded = false;
    private WordsEndedDialog endedDialog = null;

    private MainViewModel viewModel;
    private final CompositeDisposable composite = new CompositeDisposable();

    public TestModalFragment()
    {
        // Required empty public constructor
    }

    static TestModalFragment newInstance(@Nullable String json)
    {
        TestModalFragment fragment = new TestModalFragment();
        if (json != null)
        {
            Bundle bundle = new Bundle();
            bundle.putString(AppData.ARG_JSON, json);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.s_test_modal_fragment, container, false);

        enTextView = fragmentView.findViewById(R.id.en_text_view);
        ruBtn1 = fragmentView.findViewById(R.id.ru_btn_1);
        ruBtn1.setText("");
        ruBtn1_OnClick(ruBtn1);
        ruBtn2 = fragmentView.findViewById(R.id.ru_btn_2);
        ruBtn2.setText("");
        ruBtn2_OnClick(ruBtn2);

        Bundle arguments = getArguments();
        if (arguments != null)
        {
            String json = arguments.getString(AppData.ARG_JSON);
            try
            {
                Pair<Map<String, Integer>, List<DataBaseEntry>> pair = new Gson().fromJson(json, AppData.jsonType);
                enTextView.setText(pair.getSecond().get(0).getEnglish());
                nameDictTV = fragmentView.findViewById(R.id.name_dict_tv_test_modal);
                nameDictTV.setText(pair.getSecond().get(0).getDictName());
                TextView wordsNumberTV = fragmentView.findViewById(R.id.words_number_tv_test_modal);
                if (pair.getFirst().size() == 4)
                {
                    String concatText = (pair.getSecond().get(0).getRowId() + "")
                            .concat(" / ")
                            .concat(pair.getFirst().get("totalWords").toString())
                            .concat("  " + getString(R.string.text_studied) + " " + pair.getFirst().get("studiedWords").toString());
                    wordsNumberTV.setText(concatText);
                }

                composite.add(
                        viewModel.getRandomEntries(pair.getSecond().get(0).getDictName(), pair.getSecond().get(0).getRowId())
                        .observeOn(Schedulers.computation())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(entries -> {

                            entries.add(pair.getSecond().get(0));
                            RandomNumberGenerator numberGenerator = new RandomNumberGenerator(2, (int) new Date().getTime());
                            int i = numberGenerator.generate();
                            int j = numberGenerator.generate();
                            ruBtn1.setText(entries.get(i).getTranslate());
                            ruBtn2.setText(entries.get(j).getTranslate());
                            compareList = entries;

                        }, Throwable::printStackTrace)
                );

            } catch (Exception e)
            {
                e.printStackTrace();
            }


        }

        ImageButton speakButton = fragmentView.findViewById(R.id.btn_sound_modal);
        speakButton_OnClick(speakButton);

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

        ImageView orderPlayIcon = fragmentView.findViewById(R.id.order_play_icon_iv_test_modal);
        appSettings = new AppSettings(requireContext());
        if (appSettings.getOrderPlay() == 0)
        {
            orderPlayIcon.setImageResource(R.drawable.ic_repeat_white);
        }
        if (appSettings.getOrderPlay() == 1)
        {
            orderPlayIcon.setImageResource(R.drawable.ic_shuffle_white);
        }

        return fragmentView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    private void ruBtn1_OnClick(View view)
    {
        view.setOnClickListener( v -> {
            Button button = (Button) v;
            String translate = button.getText().toString().toLowerCase();
            String english = enTextView.getText().toString().toLowerCase();
            boolean result = compareWords(compareList, english, translate);
            if (result)
            {
                rightAnswerAnim(button);
            }
            else
            {
                noRightAnswerAnim(button);
            }
        });
    }

    private void ruBtn2_OnClick(View view)
    {
        view.setOnClickListener( v -> {
            Button button = (Button) v;
            String translate = button.getText().toString().toLowerCase();
            String english = enTextView.getText().toString().toLowerCase();
            boolean result = compareWords(compareList, english, translate);
            if (result)
            {
                rightAnswerAnim(button);
            }
            else
            {
                noRightAnswerAnim(button);
            }
        });
    }

    private boolean compareWords(List<DataBaseEntry> compareList, String english, String translate)
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
                        String dict = nameDictTV.getText().toString();
                        DataBaseEntry entry = new DataBaseEntry(enTextView.getText().toString(), button.getText().toString(), "0");
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_Count_REPEAT, 0);
                        UpdateDBEntryAsync updateDBEntryAsync = new UpdateDBEntryAsync(getActivity(), dict, values, "English = ? AND Translate = ?", new String[]{entry.getEnglish(), entry.getTranslate()}, new UpdateDBEntryAsync.IUpdateDBListener()
                        {
                            @Override
                            public void updateDBEntry_OnComplete(int rows)
                            {
                                if (rows > 0 && getActivity() != null)
                                {
                                    Toast.makeText(getActivity(), R.string.text_word_is_not_show, Toast.LENGTH_LONG).show();
                                    final String currentDict = nameDictTV.getText().toString();
                                    GetStudiedWordsCount getCountWordsAsync = new GetStudiedWordsCount(getActivity(), currentDict, new GetStudiedWordsCount.GetCountListener()
                                    {
                                        @Override
                                        public void onTaskComplete(Integer[] resArray)
                                        {
                                            if (resArray.length > 1)
                                            {
                                                int notStudied = resArray[0];
                                                if (notStudied == 0)
                                                {
                                                    isWordsEnded = true;
                                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                    Fragment fragmentByTag = fragmentManager.findFragmentByTag(WordsEndedDialog.TAG);
                                                    if (fragmentByTag != null)
                                                    {
                                                        fragmentManager.beginTransaction().remove(fragmentByTag).commit();
                                                    }

                                                    endedDialog = WordsEndedDialog.getInstance(currentDict, new WordsEndedDialog.IWordEndedDialogResult()
                                                    {
                                                        @Override
                                                        public void wordEndedDialogResult(int res)
                                                        {
                                                            switch (res)
                                                            {
                                                                case 0:
                                                                    appSettings.removeItemFromPlayList(currentDict);
                                                                    if (appSettings.getPlayList() == null || appSettings.getPlayList().size() == 0)
                                                                    {
                                                                        getActivity().stopService(serviceIntent);
                                                                    }
                                                                    getActivity().finish();
                                                                    break;
                                                                case 1:
                                                                    ContentValues values = new ContentValues();
                                                                    values.put(DatabaseHelper.COLUMN_Count_REPEAT, 1);
                                                                    UpdateDBEntryAsync updateDBEntryAsync = new UpdateDBEntryAsync(getActivity(), currentDict, values, null, null, new UpdateDBEntryAsync.IUpdateDBListener()
                                                                    {
                                                                        @Override
                                                                        public void updateDBEntry_OnComplete(int rows)
                                                                        {
                                                                            if (getActivity() != null)
                                                                            {
                                                                                getActivity().finish();
                                                                            }
                                                                        }
                                                                    });
                                                                    if (updateDBEntryAsync.getStatus() != AsyncTask.Status.RUNNING)
                                                                    {
                                                                        updateDBEntryAsync.execute();
                                                                    }
                                                                    break;
                                                            }

                                                        }
                                                    });
                                                    fragmentManager.beginTransaction().add(endedDialog, WordsEndedDialog.TAG).commit();
                                                }
                                            }
                                        }
                                    });
                                    if (getCountWordsAsync.getStatus() != AsyncTask.Status.RUNNING)
                                    {
                                        getCountWordsAsync.execute();
                                    }
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
                    if (getActivity() != null && !isWordsEnded)
                    {
                        //appData.saveAllSettings(getActivity());
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
                if (speech == null || speech.isSpeaking())
                {
                    return;
                }
                String enText = enTextView.getText().toString();
                if (!enText.equals(""))
                {
                    speech.speak(enText, TextToSpeech.QUEUE_ADD, map);
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
                    EventBus.getDefault().postSticky(new MainActivityOnStart(serviceIntent));
                }
            }
        });
    }

    private void btnStopService_OnClick(Button button)
    {
        button.setOnClickListener( view ->
        {
            ServiceActivity activity = (ServiceActivity)requireActivity();
            if (activity != null)
            {
                activity.stopAppService();
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        composite.dispose();
        composite.clear();
    }
}
