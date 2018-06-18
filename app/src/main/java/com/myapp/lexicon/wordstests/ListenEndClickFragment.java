package com.myapp.lexicon.wordstests;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.GetCountWordsAsync;
import com.myapp.lexicon.database.GetEntriesFromDbAsync;
import com.myapp.lexicon.database.GetTableListAsync;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.main.BackgroundFragm;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListenEndClickFragment extends Fragment implements DialogTestComplete.IDialogComplete_Result
{
    public static final String TAG = "listen_and_click_fragment";
    public static final int ROWS = 5;

    private ImageView backImage;

    private static RelativeLayout.LayoutParams saveTopPanelParams;
    private LinearLayout topPanel;
    private RelativeLayout.LayoutParams topPanelParams;
    private float touchDown = 0, touchUp = 0;
    private Button topPanelButtonOK;
    private Button topPanelButtonFinish;
    private ImageButton topPanelButtonThreePoints;

    private Button tempButton;
    private LinearLayout buttonsLayout;
    private Spinner spinnListDict;
    private ImageButton buttonSpeech;
    private ProgressBar progressBar;
    private TextView tvProgressValue;

    private static RandomNumberGenerator randomGenerator;
    private LockOrientation lockOrientation;
    private long duration = 1000;
    private DialogTestComplete dialogTestComplete;
    private TestResults testResults;
    private DisplayMetrics displayMetrics;
    private AppSettings appSettings;
    private AppData appData;
    private Fields fields;

    private final String KEY_FIELDS = "key_fields";
    private final String KEY_PROGRESS = "key_progress";
    private final String KEY_PROGRESS_MAX = "key_progress_max";


    public ListenEndClickFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PROGRESS, progressBar.getProgress());
        outState.putInt(KEY_PROGRESS_MAX, progressBar.getMax());
        outState.putParcelable(KEY_FIELDS, fields);

        if (fields.isOpen[0])
        {
            saveTopPanelParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            saveTopPanelParams.setMargins(topPanelParams.leftMargin, (int) topPanel.getY(), topPanelParams.rightMargin, topPanelParams.height);
        }

        saveButtonsLayoutState();
    }

    private void saveButtonsLayoutState()
    {
        Map<Float, String> stringMap = new HashMap<>();
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            if (!button.getText().toString().equals(""))
            {
                stringMap.put(button.getY(), button.getText().toString());
            }
        }

        Map<Float, String> sortBtnsLayout = new TreeMap<>(new Comparator<Float>()
        {
            @Override
            public int compare(Float lhs, Float rhs)
            {
                return lhs.compareTo(rhs);
            }
        });
        sortBtnsLayout.putAll(stringMap);

        Collection<String> stringCollection = sortBtnsLayout.values();
        fields.textArray.clear();
        fields.textArray.addAll(stringCollection);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);

        if (savedInstanceState == null)
        {
            fields = new Fields();
        }
        else
        {
            fields = savedInstanceState.getParcelable(KEY_FIELDS);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getActivity() != null)
        {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
        }
        lockOrientation = new LockOrientation(getActivity());
        appSettings = new AppSettings(getActivity());
        appData = AppData.getInstance();

        View fragment_view = inflater.inflate(R.layout.t_listen_end_click_layout, container, false);

        backImage = fragment_view.findViewById(R.id.img_back_listen_layout);

        topPanel = fragment_view.findViewById(R.id.top_panel);
        topPanelParams = (RelativeLayout.LayoutParams) topPanel.getLayoutParams();
        topPanelButtonOK = fragment_view.findViewById(R.id.btn_ok);
        topPanelButtonFinish = fragment_view.findViewById(R.id.btn_complete);
        topPanelButtonThreePoints = fragment_view.findViewById(R.id.btn_more_horiz);
        topPanelButtons_OnClick();

        LinearLayout linLayout = fragment_view.findViewById(R.id.linear_layout);
        spinnListDict = fragment_view.findViewById(R.id.spinner_dict);
        buttonsLayout = fragment_view.findViewById(R.id.buttons_layout);
        buttonSpeech = fragment_view.findViewById(R.id.btn_speech);
        buttonSpeech_OnClick();
        progressBar = fragment_view.findViewById(R.id.prog_bar_listen);
        tvProgressValue = fragment_view.findViewById(R.id.tv_progress_value);
        setProgressValue(0, fields.wordsCount);

        testResults = new TestResults(getActivity());
        dialogTestComplete = new DialogTestComplete();
        dialogTestComplete.setIDialogCompleteResult(ListenEndClickFragment.this);

        if (savedInstanceState == null)
        {
            hideWordButtons();
        }

        if (savedInstanceState != null && fields.isOpen[0])
        {
            topPanel.setLayoutParams(saveTopPanelParams);
        }

        if (savedInstanceState != null)
        {
            progressBar.setProgress(savedInstanceState.getInt(KEY_PROGRESS));
            progressBar.setMax(savedInstanceState.getInt(KEY_PROGRESS_MAX));
            setProgressValue(progressBar.getProgress(), progressBar.getMax());

            for (int i = 0; i < buttonsLayout.getChildCount(); i++)
            {
                Button button = (Button) buttonsLayout.getChildAt(i);
                if (fields.textArray.size() > 0 && i < fields.textArray.size() && !fields.textArray.get(i).equals(""))
                {
                    button.setText(fields.textArray.get(i));
                    button.setVisibility(View.VISIBLE);
                    btnLeft_OnClick(button);
                }
                else
                {
                    button.setText("");
                    button.setVisibility(View.GONE);
                }
            }
        }

        linLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    touchDown = event.getY();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    touchUp = event.getY();
                }

                topPanelVisible(touchDown, touchUp, fields.isOpen[0]);
                return true;
            }

        });

        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();

        return fragment_view;
    }

    private void buttonSpeech_OnClick()
    {
        buttonSpeech.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                topPanelVisible(1, 0, fields.isOpen[0]);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "btn_speech");
                try
                {
                    SplashScreenActivity.speech.setLanguage(Locale.US);
                } catch (Exception e)
                {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    SplashScreenActivity.speech.speak(fields.textEn, TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                } else
                {
                    SplashScreenActivity.speech.speak(fields.textEn, TextToSpeech.QUEUE_ADD, hashMap);
                }
            }
        });
    }

    private void setItemsToSpinnListDict()
    {
        if (fields.storedListDict.size() > 0)
        {
            if (getActivity() != null)
            {
                ArrayAdapter<String> spinnAdapter = new ArrayAdapter<>(getActivity(), R.layout.my_content_spinner_layout, fields.storedListDict);
                spinnListDict.setAdapter(spinnAdapter);
                return;
            }
        }

        GetTableListAsync getTableListAsync = new GetTableListAsync(getActivity(), new GetTableListAsync.GetTableListListener()
        {
            @Override
            public void getTableListListener(ArrayList<String> arrayList)
            {
                if (getActivity() != null)
                {
                    ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(getActivity(), R.layout.my_content_spinner_layout, arrayList);
                    spinnListDict.setAdapter(adapterSpinner);
                }
                ArrayList<String> playList = appSettings.getPlayList();
                if (playList != null && playList.size() > 0)
                {
                    String currentDict = playList.get(appData.getNdict());
                    if (arrayList.contains(currentDict))
                    {
                        int indexOf = arrayList.indexOf(currentDict);
                        spinnListDict.setSelection(indexOf);
                    }
                }
                else
                {
                    spinnListDict.setSelection(0);
                }
                spinnListDict_OnItemSelectedListener();
                for (int i = 0; i < spinnListDict.getAdapter().getCount(); i++)
                {
                    fields.storedListDict.add(spinnListDict.getAdapter().getItem(i).toString());
                }
            }
        });
        if (getTableListAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getTableListAsync.execute();
        }
    }

    private void spinnListDict_OnItemSelectedListener()
    {
        spinnListDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id)
            {
                if (position == fields.spinnSelectedIndex) return;
                fields.spinnSelectedIndex = position;
                fields.textArray.clear();
                startTest();
                topPanelVisible(1, 0, fields.isOpen[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    private void startTest()
    {
        fields.wordIndex = 1;
        fields.spinnSelectedItem = spinnListDict.getSelectedItem().toString();
        fields.counterRightAnswer = 0;

        Bundle arguments = getArguments();
        if (arguments != null)
        {
            if (arguments.containsKey(appSettings.KEY_SPINN_SELECT_ITEM) && arguments.containsKey(appSettings.KEY_WORD_INDEX) && arguments.containsKey(appSettings.KEY_COUNTER_RIGHT_ANSWER))
            {
                if (arguments.getString(appSettings.KEY_SPINN_SELECT_ITEM) != null && arguments.getInt(appSettings.KEY_COUNTER_RIGHT_ANSWER) > 0)
                {
                    fields.spinnSelectedItem = arguments.getString(appSettings.KEY_SPINN_SELECT_ITEM);
                    fields.wordIndex = arguments.getInt(appSettings.KEY_WORD_INDEX);
                    fields.counterRightAnswer = arguments.getInt(appSettings.KEY_COUNTER_RIGHT_ANSWER);
                }
            }
            getArguments().clear();
        }

        GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), fields.spinnSelectedItem, new GetCountWordsAsync.GetCountListener()
        {
            @Override
            public void onTaskComplete(int count)
            {
                fields.wordsCount = count;
                fillLayoutLeft();
                progressBar.setMax(count);
                progressBar.setProgress(fields.wordIndex - 1);
                setProgressValue(progressBar.getProgress(), progressBar.getMax());
            }
        });
        if (getCountWordsAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getCountWordsAsync.execute();
        }
    }

    private void fillLayoutLeft()
    {
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            button.setText("");
            button.setVisibility(View.GONE);
        }

        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), fields.spinnSelectedItem, fields.wordIndex, fields.wordIndex + ROWS - 1, new GetEntriesFromDbAsync.GetEntriesListener()
        {
            @Override
            public void getEntriesListener(ArrayList<DataBaseEntry> entries)
            {
                fields.controlList = entries;
                fields.additionalList = new ArrayList<>();
                fields.additonalCount = 0;
                fields.additionalList.addAll(entries);
                fields.oldControlListSize = fields.controlList.size();
                randomGenerator = new RandomNumberGenerator(fields.oldControlListSize, (int) new Date().getTime());
                long start_delay = 0;
                for (int i = 0; i < fields.controlList.size(); i++)
                {
                    Button button = (Button) buttonsLayout.getChildAt(i);
                    button.setText(fields.controlList.get(randomGenerator.generate()).getTranslate());
                    button.setTranslationX(displayMetrics.widthPixels);
                    button.setTranslationY(0);
                    button.setVisibility(View.VISIBLE);
                    button.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null).setStartDelay(start_delay);
                    start_delay += 70;
                    btnLeft_OnClick(button);
                    fields.wordIndex++;
                }
                randomGenerator = new RandomNumberGenerator(fields.oldControlListSize, (int) new Date().getTime());
                int randIndex = randomGenerator.generate();
                fields.textEn = entries.get(randIndex).getEnglish();
            }
        });
        if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getEntriesFromDbAsync.execute();
        }
    }

    private void btnLeft_OnClick(final View view)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (fields.isStartAnim[0])
                {
                    return;
                }
                tempButton = (Button) view;
                fields.buttonY = tempButton.getY();
                fields.buttonX = tempButton.getX();
                lockOrientation.lock();
                compareWords(fields.spinnSelectedItem, fields.textEn, tempButton.getText().toString());
            }
        });
    }

    private void setProgressValue(double progressValue, double progressMax)
    {
        if (progressMax != 0)
        {
            double percentProgress = progressValue / progressMax * 100;
            String value = String.valueOf(BigDecimal.valueOf(percentProgress).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue()) + "%";
            tvProgressValue.setText(value);
        }
    }

    private void compareWords(String tableName, String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;

        boolean isRight = false;
        for (int i = 0; i < fields.controlList.size(); i++)
        {
            if (fields.controlList.get(i).getEnglish().toLowerCase().equals(enword.toLowerCase()) &&
                    fields.controlList.get(i).getTranslate().toLowerCase().equals(ruword.toLowerCase()))
            {
                isRight = true;
                fields.indexEn = i;
                break;
            }
        }

        if (isRight)
        {

            GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), tableName, fields.wordIndex, fields.wordIndex, new GetEntriesFromDbAsync.GetEntriesListener()
            {
                @Override
                public void getEntriesListener(ArrayList<DataBaseEntry> entries)
                {
                    fields.listFromDB = entries;
                    progressBar.setProgress(progressBar.getProgress()+1);
                    setProgressValue(progressBar.getProgress(), fields.wordsCount);
                    if (tempButton != null)
                    {
                        tempButton.setText(fields.textEn);
                        tempButton.setBackgroundResource(R.drawable.text_btn_for_test_green);
                    }
                    final HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "compare_words");
                    try
                    {
                        SplashScreenActivity.speech.setLanguage(Locale.US);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            SplashScreenActivity.speech.speak(fields.textEn, TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        } else
                        {
                            SplashScreenActivity.speech.speak(fields.textEn, TextToSpeech.QUEUE_ADD, hashMap);
                        }
                        SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
                        {
                            @Override
                            public void onStart(String utteranceId)
                            {

                            }

                            @Override
                            public void onDone(String utteranceId)
                            {
                                if (utteranceId.equals("compare_words"))
                                {
                                    animButtonToLeft(tempButton);
                                }
                            }

                            @Override
                            public void onError(String utteranceId)
                            {

                            }
                        });
                    } catch (Exception e)
                    {
                        animButtonToLeft(tempButton);
                        e.printStackTrace();
                    }
                    fields.counterRightAnswer++;
                }
            });
            if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
            {
                getEntriesFromDbAsync.execute();
            }
        }
        else
        {
            fields.counterRightAnswer--;
            Animation animNotRight = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_not_right);
            animNotRight.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation)
                {

                }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    tempButton.setBackgroundResource(R.drawable.text_button_for_test);
                    lockOrientation.unLock();
                }

                @Override
                public void onAnimationRepeat(Animation animation)
                {

                }
            });
            if (tempButton != null)
            {
                tempButton.setBackgroundResource(R.drawable.text_btn_for_test_red);
                tempButton.startAnimation(animNotRight);
            }
        }
    }

    public void animButtonsToDown(float x, float y)
    {
        ViewPropertyAnimator animToDown;
        boolean isListener = false;
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) button.getLayoutParams();

            float X = button.getX();
            float Y = button.getY();
            if (Y < y && x == X)
            {
                animToDown = button.animate()
                        .translationYBy(button.getHeight()+layoutParams.bottomMargin)
                        .setDuration(300)
                        .setStartDelay(0)
                        .setInterpolator(new AccelerateInterpolator());
                if (!isListener)
                {
                    isListener = true;
                    animToDown.setListener(new android.animation.Animator.AnimatorListener()
                    {

                        @Override
                        public void onAnimationStart(android.animation.Animator animation)
                        {

                        }

                        @Override
                        public void onAnimationEnd(android.animation.Animator animation)
                        {
                            animButtonFromRigth(tempButton);
                        }

                        @Override
                        public void onAnimationCancel(android.animation.Animator animation)
                        {

                        }

                        @Override
                        public void onAnimationRepeat(android.animation.Animator animation)
                        {

                        }
                    });
                }
            }
        }
    }

    private void animButtonToLeft(final Button button)
    {
        if (button == null) return;
        button.animate().translationX(-button.getWidth()-button.getLeft())
                .setDuration(duration - 500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {
                        lockOrientation.lock();
                        fields.isStartAnim[0] = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        tempButton.setY(0);
                        DisplayMetrics metrics;
                        if (getActivity() != null)
                        {
                            Display display = getActivity().getWindowManager().getDefaultDisplay();
                            metrics = new DisplayMetrics();
                            display.getMetrics(metrics);
                            button.setX(metrics.widthPixels+10);
                        }

                        if (fields.listFromDB.size() > 0)
                        {
                            fields.controlList.set(fields.indexEn, fields.listFromDB.get(0));
                            button.setText(fields.listFromDB.get(0).getTranslate());
                            if (fields.oldControlListSize != fields.controlList.size())
                            {
                                randomGenerator = new RandomNumberGenerator(fields.controlList.size(), (int) new Date().getTime());
                                fields.oldControlListSize = fields.controlList.size();
                            }
                            int randomNumber = randomGenerator.generate();
                            if (randomNumber < 0)
                            {
                                randomGenerator = new RandomNumberGenerator(fields.oldControlListSize, (int) new Date().getTime());
                                randomNumber = randomGenerator.generate();
                            }
                            fields.textEn = fields.controlList.get(randomNumber).getEnglish();
                            if (fields.buttonY > 0)
                            {
                                animButtonsToDown(fields.buttonX, fields.buttonY);
                            }
                            else
                            {
                                animButtonFromRigth(tempButton);
                            }
                        }
                        else if (fields.controlList.size() <= ROWS)
                        {
                            try
                            {
                                fields.controlList.remove(fields.indexEn);
                                button.setText(fields.additionalList.get(fields.additonalCount).getTranslate());
                                fields.additonalCount++;
                                fields.textEn = "";
                                if (fields.controlList.size() > 0)
                                {
                                    randomGenerator = new RandomNumberGenerator(fields.controlList.size(), (int) new Date().getTime());
                                    int randomNumber = randomGenerator.generate();
                                    fields.textEn = fields.controlList.get(randomNumber).getEnglish();
                                }
                            } catch (Exception e)
                            {
                                Toast.makeText(getActivity(), "Error - "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            if (fields.buttonY > 0)
                            {
                                animButtonsToDown(fields.buttonX, fields.buttonY);
                            }
                            else
                            {
                                animButtonFromRigth(tempButton);
                            }
                        }
                        fields.wordIndex++;

                    }

                    @Override
                    public void onAnimationCancel(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation)
                    {

                    }
                });
    }

    private void animButtonFromRigth(Button button)
    {
        if (button == null) return;
        final ViewPropertyAnimator _animator = button.animate().translationX(0)
                .setDuration(duration)
                .setStartDelay(0)
                .setInterpolator(new AnticipateOvershootInterpolator());
        _animator.setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                tempButton.setBackgroundResource(R.drawable.text_button_for_test);
            }

            @Override
            public void onAnimationEnd(final Animator animation)
            {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "btn_from_rigth_anim");
                try
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        SplashScreenActivity.speech.speak(fields.textEn, TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                    } else
                    {
                        SplashScreenActivity.speech.speak(fields.textEn, TextToSpeech.QUEUE_ADD, hashMap);
                    }
                    SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
                    {
                        @Override
                        public void onStart(String utteranceId)
                        {

                        }

                        @Override
                        public void onDone(String utteranceId)
                        {
                            _animator.setListener(null);
                        }

                        @Override
                        public void onError(String utteranceId)
                        {

                        }
                    });
                    lockOrientation.unLock();
                } catch (Exception e)
                {
                    lockOrientation.unLock();
                }
                fields.isStartAnim[0] = false;
                if (fields.textEn.equals(""))
                {
                    ArrayList<String> list = new ArrayList<>();
                    Bundle bundle = new Bundle();
                    if (getActivity() != null)
                    {
                        list.add(testResults.getOverallResult(fields.counterRightAnswer, fields.wordsCount));
                        list.add(fields.counterRightAnswer + getActivity().getString(R.string.text_out_of) + fields.wordsCount);

                    } else
                    {
                        list.add(testResults.getOverallResult(fields.counterRightAnswer, fields.wordsCount));
                        list.add(fields.counterRightAnswer + " / " + fields.wordsCount);
                    }
                    bundle.putString(dialogTestComplete.KEY_RESULT, list.get(0));
                    bundle.putString(dialogTestComplete.KEY_ERRORS, list.get(1));
                    dialogTestComplete.setArguments(bundle);
                    dialogTestComplete.setCancelable(false);
                    if (getFragmentManager() != null)
                    {
                        dialogTestComplete.show(getFragmentManager(), "dialog_complete_lexicon");
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });
    }

    @Override
    public void dialogCompleteResult(int res)
    {
        appSettings.saveTestFragmentState(TAG, null);
        if (res == 0)
        {
            addToStudiedList();
            fields.spinnSelectedIndex = spinnListDict.getSelectedItemPosition();
            startTest();
        }
        if (res > 0)
        {
            addToStudiedList();
            int count = spinnListDict.getAdapter().getCount();
            int position = spinnListDict.getSelectedItemPosition();
            if (position < count)
            {
                position++;
                spinnListDict.setSelection(position);
            }
            if (position == count)
            {
                position = 0;
                spinnListDict.setSelection(position);
            }
        }
        if (res < 0)
        {
            addToStudiedList();
            spinnListDict.setSelection(-1);
            fields.spinnSelectedIndex = -1;
            FragmentActivity activity = getActivity();
            if (activity != null)
            {
                getActivity().onBackPressed();
            }

            if (fields.arrStudiedDict.size() > 0)
            {
                DialogChangePlayList dialogChangePlayList = new DialogChangePlayList();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(dialogChangePlayList.KEY_LIST_DICT, fields.arrStudiedDict);
                dialogChangePlayList.setArguments(bundle);
                dialogChangePlayList.setCancelable(false);
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null)
                {
                    dialogChangePlayList.show(fragmentManager, "dialog_change_pl_lexicon");
                }
            }
        }
    }

    private void addToStudiedList()
    {
        ArrayList<String> playList = appSettings.getPlayList();

        boolean containsInPlayList = playList.contains(spinnListDict.getSelectedItem().toString());
        boolean contains = fields.arrStudiedDict.contains(spinnListDict.getSelectedItem().toString());
        if (fields.counterRightAnswer == fields.wordsCount && !contains && containsInPlayList)
        {
            fields.arrStudiedDict.add(spinnListDict.getSelectedItem().toString());
            fields.counterRightAnswer = 0;
        }
    }

    private void topPanelVisible(float touchDown, float touchUp, boolean isOpen)
    {
        if (touchDown < touchUp && !isOpen)
        {
            topPanel.animate().y(topPanelParams.bottomMargin).setDuration(1000).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animator)
                {

                }

                @Override
                public void onAnimationEnd(Animator animator)
                {
                    fields.isOpen[0] = true;
                }

                @Override
                public void onAnimationCancel(Animator animator)
                {

                }

                @Override
                public void onAnimationRepeat(Animator animator)
                {

                }
            });
        }
        else if (touchDown > touchUp && isOpen)
        {
            topPanel.animate().y(topPanelParams.topMargin).setDuration(1000).setListener(new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animator)
                {

                }

                @Override
                public void onAnimationEnd(Animator animator)
                {
                    fields.isOpen[0] = false;
                }

                @Override
                public void onAnimationCancel(Animator animator)
                {

                }

                @Override
                public void onAnimationRepeat(Animator animator)
                {

                }
            });
        }
    }

    private void hideWordButtons()
    {
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button btnLeft = (Button) buttonsLayout.getChildAt(i);
            Button btnRight = (Button) buttonsLayout.getChildAt(i);
            btnLeft.setVisibility(View.INVISIBLE);
            btnLeft.setText(null);
            btnRight.setVisibility(View.INVISIBLE);
            btnRight.setText(null);
        }
    }

    private void topPanelButtons_OnClick()
    {
        topPanelButtonOK.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                topPanelVisible(1, 0, fields.isOpen[0]);
            }
        });
        topPanelButtonFinish.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                topPanelVisible(1, 0, fields.isOpen[0]);
                spinnListDict.setSelection(-1);
                fields.spinnSelectedIndex = -1;
                if (getActivity() != null)
                {
                    getActivity().onBackPressed();
                }
            }
        });
        topPanelButtonThreePoints.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                topPanelVisible(0, 1, fields.isOpen[0]);
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        if (isRemoving() && fields.wordIndex -1 - ROWS < fields.wordsCount && fields.counterRightAnswer > 1 && fields.wordsCount >= ROWS)
        {
            fields.spinnSelectedIndex = -1;
            if (getActivity() != null)
            {
                DialogWarning dialogWarning = new DialogWarning();

                Bundle bundle = new Bundle();
                bundle.putString(dialogWarning.KEY_MESSAGE, getString(R.string.text_not_finished_test));
                bundle.putString(dialogWarning.KEY_TEXT_OK_BUTTON, getString(R.string.button_text_yes));
                bundle.putString(dialogWarning.KEY_TEXT_NO_BUTTON, getString(R.string.button_text_no));

                dialogWarning.setArguments(bundle);
                dialogWarning.setCancelable(false);
                dialogWarning.show(getActivity().getSupportFragmentManager(), dialogWarning.TAG);
                dialogWarning.setListener(new DialogWarning.IDialogResult()
                {
                    @Override
                    public void dialogListener(boolean result)
                    {
                        if (result)
                        {
                            Bundle bundle = new Bundle();
                            bundle.putString(appSettings.KEY_SPINN_SELECT_ITEM, fields.spinnSelectedItem);
                            bundle.putInt(appSettings.KEY_WORD_INDEX, fields.wordIndex - ROWS);
                            bundle.putInt(appSettings.KEY_COUNTER_RIGHT_ANSWER, fields.counterRightAnswer);
                            appSettings.saveTestFragmentState(TAG, bundle);
                        }
                        else
                        {
                            appSettings.saveTestFragmentState(TAG, null);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        RandomNumberGenerator generator = new RandomNumberGenerator(BackgroundFragm.imagesId.length, (int) new Date().getTime());
        backImage.setImageResource(BackgroundFragm.imagesId[generator.generate()]);
    }


}
