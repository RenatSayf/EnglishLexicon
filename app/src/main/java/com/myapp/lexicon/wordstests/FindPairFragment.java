package com.myapp.lexicon.wordstests;


import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class FindPairFragment extends Fragment implements DialogTestComplete.IDialogComplete_Result
{
    public static final String TAG = "tag_find_pair";

    public final int ROWS = 5;

    private static RelativeLayout.LayoutParams saveTopPanelParams;

    private LinearLayout topPanel;
    private RelativeLayout.LayoutParams topPanelParams;
    private float touchDown = 0, touchUp = 0;
    private Button topPanelButtonOK;
    private Button topPanelButtonFinish;
    private ImageButton topPanelButtonThreePoints;
    private long duration = 1000;
    private static boolean isOpen = false;

    private LinearLayout btnLayoutLeft, btnLayoutRight;

    private Spinner spinnListDict;
    private int spinnSelectedIndex = -1;
    private String spinnSelectedItem;

    private ProgressBar progressBar;
    private ImageView backImageView;

    private LockOrientation lockOrientation;
    private int wordIndex = 1;
    private int wordsCount;
    private int counterRightAnswer = 0;

    private static ArrayList<String> textArrayleft = new ArrayList<>();
    private static ArrayList<String> textArrayRight = new ArrayList<>();
    private ArrayList<String> arrStudiedDict;
    private ArrayList<DataBaseEntry> controlList;
    private ArrayList<DataBaseEntry> additionalList;
    private ArrayList<String> storedListDict;

    private int controlListSize = 0;
    private DisplayMetrics metrics;
    private String enWord = null;
    private String ruWord = null;
    private Button tempButtonLeft;
    private Button tempButtonRight;
    private Button btnNoRight;

    private static boolean isAnimStart = false;
    private DialogTestComplete dialogTestComplete;
    private TestResults testResults;
    private FragmentManager fragmentManager;
    private AppSettings appSettings;
    private AppData appData;

    private final String KEY_CONTROL_LIST_SIZE = "key_control_list_size";
    private final String KEY_WORDS_COUNT = "key_words_count";
    private final String KEY_SPINN_SELECT_ITEM = "key_spinn_select_item";
    private final String KEY_SPINN_SELECT_INDEX = "key_spinn_select_index";
    private final String KEY_PROGRESS = "key_progress";
    private final String KEY_PROGRESS_MAX = "key_progress_max";
    private final String KEY_WORD_INDEX = "key_word_index";
    private final String KEY_COUNTER_RIGHT_ANSWER = "key_counter_right_answer";
    private final String KEY_ARRAY_STUDIED_DICT = "key_array_studied_dict";
    private final String KEY_CONTROL_LIST = "key_control_list";
    private final String KEY_ADDITIONAL_LIST = "key_additional_list";
    private final String KEY_STORED_DICT_LIST = "key_stored_dict_list";


    public FindPairFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);

        storedListDict = new ArrayList<>();
        arrStudiedDict = new ArrayList<>();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SPINN_SELECT_ITEM, spinnSelectedItem);
        outState.putInt(KEY_SPINN_SELECT_INDEX, spinnSelectedIndex);
        outState.putInt(KEY_PROGRESS, progressBar.getProgress());
        outState.putInt(KEY_PROGRESS_MAX, progressBar.getMax());
        outState.putInt(KEY_WORD_INDEX, wordIndex);
        outState.putInt(KEY_WORDS_COUNT, wordsCount);
        outState.putInt(KEY_CONTROL_LIST_SIZE, controlListSize);
        outState.putInt(KEY_COUNTER_RIGHT_ANSWER, counterRightAnswer);
        outState.putStringArrayList(KEY_ARRAY_STUDIED_DICT, arrStudiedDict);
        outState.putParcelableArrayList(KEY_CONTROL_LIST, controlList);
        outState.putParcelableArrayList(KEY_ADDITIONAL_LIST, additionalList);
        outState.putStringArrayList(KEY_STORED_DICT_LIST, storedListDict);

        if (isOpen)
        {
            saveTopPanelParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            saveTopPanelParams.setMargins(topPanelParams.leftMargin, (int) topPanel.getY(), topPanelParams.rightMargin, topPanelParams.height);
        }

        saveButtonsLayoutState();
    }

    private void saveButtonsLayoutState()
    {
        textArrayleft.clear();
        textArrayRight.clear();
        for (int i = 0; i < btnLayoutLeft.getChildCount() && i < btnLayoutRight.getChildCount(); i++)
        {
            Button buttonLeft = (Button) btnLayoutLeft.getChildAt(i);
            Button buttonRight = (Button) btnLayoutRight.getChildAt(i);
            if (!buttonLeft.getText().toString().equals(""))
            {
                textArrayleft.add(buttonLeft.getText().toString());
            }
            if (!buttonRight.getText().toString().equals(""))
            {
                textArrayRight.add(buttonRight.getText().toString());
            }
        }
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

        if (isRemoving() && wordIndex -1 < wordsCount && counterRightAnswer > 1 && wordsCount >= ROWS)
        {
            spinnSelectedIndex = -1;
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
                        bundle.putString(appSettings.KEY_SPINN_SELECT_ITEM, spinnSelectedItem);
                        bundle.putInt(appSettings.KEY_WORD_INDEX, wordIndex);
                        bundle.putInt(appSettings.KEY_COUNTER_RIGHT_ANSWER, counterRightAnswer);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        appSettings = new AppSettings(getActivity());
        appData = AppData.getInstance();
        lockOrientation = new LockOrientation(getActivity());
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        View fragment_view = inflater.inflate(R.layout.t_find_pair_fragment, container, false);

        backImageView = (ImageView) fragment_view.findViewById(R.id.back_image_view1);

        topPanel = (LinearLayout) fragment_view.findViewById(R.id.top_panel);
        topPanelParams = (RelativeLayout.LayoutParams) topPanel.getLayoutParams();
        LinearLayout linLayout = (LinearLayout) fragment_view.findViewById(R.id.lin_layout_find_pair);
        TextView headerTopPanel = (TextView) fragment_view.findViewById(R.id.header_top_panel);
        headerTopPanel.setText(R.string.text_find_pair_words);
        topPanelButtonOK = (Button) fragment_view.findViewById(R.id.btn_ok);
        topPanelButtonFinish = (Button) fragment_view.findViewById(R.id.btn_complete);
        topPanelButtonThreePoints = (ImageButton) fragment_view.findViewById(R.id.btn_more_horiz);
        topPanelButtons_OnClick();
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

                topPanelVisible(touchDown, touchUp, isOpen);

                return true;
            }
        });

        spinnListDict = (Spinner) fragment_view.findViewById(R.id.spinner_dict);

        progressBar = (ProgressBar) fragment_view.findViewById(R.id.prog_bar_find_pair);

        btnLayoutLeft = (LinearLayout) fragment_view.findViewById(R.id.btn_layout_left);
        btnLayoutRight = (LinearLayout) fragment_view.findViewById(R.id.btn_layout_right);

        if (savedInstanceState == null)
        {
            hideWordButtons();
        }

        if (savedInstanceState != null && isOpen)
        {
            topPanel.setLayoutParams(saveTopPanelParams);
        }

        if (savedInstanceState != null)
        {
            spinnSelectedItem = savedInstanceState.getString(KEY_SPINN_SELECT_ITEM);
            spinnSelectedIndex = savedInstanceState.getInt(KEY_SPINN_SELECT_INDEX);
            progressBar.setProgress(savedInstanceState.getInt(KEY_PROGRESS));
            progressBar.setMax(savedInstanceState.getInt(KEY_PROGRESS_MAX));
            wordIndex = savedInstanceState.getInt(KEY_WORD_INDEX);
            wordsCount = savedInstanceState.getInt(KEY_WORDS_COUNT);
            controlListSize = savedInstanceState.getInt(KEY_CONTROL_LIST_SIZE);
            counterRightAnswer = savedInstanceState.getInt(KEY_COUNTER_RIGHT_ANSWER);
            arrStudiedDict = savedInstanceState.getStringArrayList(KEY_ARRAY_STUDIED_DICT);
            controlList = savedInstanceState.getParcelableArrayList(KEY_CONTROL_LIST);
            additionalList = savedInstanceState.getParcelableArrayList(KEY_ADDITIONAL_LIST);
            storedListDict = savedInstanceState.getStringArrayList(KEY_STORED_DICT_LIST);
        }

        if (savedInstanceState != null)
        {
            if (textArrayleft.size() <= btnLayoutLeft.getChildCount() && textArrayRight.size() <= btnLayoutRight.getChildCount())
            {
                for (int i = 0; i < btnLayoutLeft.getChildCount() && i < btnLayoutRight.getChildCount(); i++)
                {
                    Button buttonLeft = (Button) btnLayoutLeft.getChildAt(i);
                    Button buttonRight = (Button) btnLayoutRight.getChildAt(i);
                    try
                    {
                        if (!textArrayleft.get(i).equals(""))
                        {
                            buttonLeft.setText(textArrayleft.get(i));
                        }

                    } catch (Exception e)
                    {
                        buttonLeft.setVisibility(View.INVISIBLE);
                        buttonLeft.setText(null);
                    }

                    try
                    {
                        if (!textArrayRight.get(i).equals(""))
                        {
                            buttonRight.setText(textArrayRight.get(i));
                        }
                    }catch (Exception e)
                    {
                        buttonRight.setVisibility(View.INVISIBLE);
                        buttonRight.setText(null);
                    }
                    btnLeft_OnClick(buttonLeft);
                    btnRight_OnClick(buttonRight);
                }
            }
        }

        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();

        testResults = new TestResults(getActivity());

        fragmentManager = getFragmentManager();
        dialogTestComplete = new DialogTestComplete();
        dialogTestComplete.setIDialogCompleteResult(this);

        return fragment_view;
    }

    private void spinnListDict_OnItemSelectedListener()
    {
        spinnListDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (position == spinnSelectedIndex) return;
                spinnSelectedIndex = position;
                startTest();
                topPanelVisible(1, 0, isOpen);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    private void setItemsToSpinnListDict()
    {
        if (storedListDict.size() > 0)
        {
            ArrayAdapter<String> spinnAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.my_content_spinner_layout, storedListDict);
            spinnListDict.setAdapter(spinnAdapter);
            return;
        }

        GetTableListAsync getTableListAsync = new GetTableListAsync(getActivity(), new GetTableListAsync.GetTableListListener()
        {
            @Override
            public void getTableListListener(ArrayList<String> arrayList)
            {
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.my_content_spinner_layout, arrayList);
                spinnListDict.setAdapter(adapterSpinner);
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
                    storedListDict.add(spinnListDict.getAdapter().getItem(i).toString());
                }
            }
        });
        if (getTableListAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getTableListAsync.execute();
        }
    }

    private void startTest()
    {
        wordIndex = 1;
        spinnSelectedItem = spinnListDict.getSelectedItem().toString();
        counterRightAnswer = 0;

        Bundle arguments = getArguments();
        if (arguments != null)
        {
            if (arguments.containsKey(appSettings.KEY_SPINN_SELECT_ITEM) && arguments.containsKey(appSettings.KEY_WORD_INDEX) && arguments.containsKey(appSettings.KEY_COUNTER_RIGHT_ANSWER))
            {
                if (arguments.getString(appSettings.KEY_SPINN_SELECT_ITEM) != null && arguments.getInt(appSettings.KEY_COUNTER_RIGHT_ANSWER) > 0)
                {
                    spinnSelectedItem = arguments.getString(appSettings.KEY_SPINN_SELECT_ITEM);
                    wordIndex = arguments.getInt(appSettings.KEY_WORD_INDEX);
                    counterRightAnswer = arguments.getInt(appSettings.KEY_COUNTER_RIGHT_ANSWER);
                }
            }
            getArguments().clear();
        }

        GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), spinnSelectedItem, new GetCountWordsAsync.GetCountListener()
        {
            @Override
            public void onTaskComplete(int count)
            {
                wordsCount = count;
                progressBar.setMax(count);
                progressBar.setProgress(wordIndex - 1);
                hideWordButtons();
                if (wordsCount < ROWS)
                {
                    fillButtonsLayout(spinnSelectedItem, wordIndex, wordsCount);
                }
                else
                {
                    fillButtonsLayout(spinnSelectedItem, wordIndex, wordIndex -1 + ROWS);
                }
            }
        });
        if (getCountWordsAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getCountWordsAsync.execute();
        }
    }

    private void fillButtonsLayout(String dictName, int start, int end)
    {
        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), dictName, start, end, new GetEntriesFromDbAsync.GetEntriesListener()
        {
            @Override
            public void getEntriesListener(ArrayList<DataBaseEntry> entries)
            {
                controlList = entries;
                additionalList = new ArrayList<>();
                for (DataBaseEntry entry : entries)
                {
                    additionalList.add(entry);
                }
                controlListSize = controlList.size();
                Date date = new Date();
                RandomNumberGenerator randGenRight = new RandomNumberGenerator(controlListSize, (int) date.getTime());
                long delay = 0;
                for (int i = 0; i < controlList.size(); i++)
                {
                    Button btnLeft = (Button) btnLayoutLeft.getChildAt(i);
                    Button btnRight = (Button) btnLayoutRight.getChildAt(i);

                    btnLeft.setScaleX(1.0f);
                    btnLeft.setScaleY(1.0f);
                    btnRight.setScaleX(1.0f);
                    btnRight.setScaleY(1.0f);
                    btnLeft.setText(controlList.get(i).getEnglish());
                    btnRight.setText(controlList.get(randGenRight.generate()).getTranslate());
                    btnLeft.setX(-metrics.widthPixels);
                    btnLeft.setTranslationY(0);
                    btnRight.setX(metrics.widthPixels);
                    btnRight.setTranslationY(0);
                    btnLeft.setVisibility(View.VISIBLE);
                    btnRight.setVisibility(View.VISIBLE);
                    btnLeft.animate().translationX(0).translationY(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null).setStartDelay(delay);
                    btnRight.animate().translationX(0).translationY(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null).setStartDelay(delay);
                    delay += 70;
                    btnLeft_OnClick(btnLeft);
                    btnRight_OnClick(btnRight);
                }
                if (textArrayleft.size() == 0 && textArrayRight.size() == 0)
                {
                    for (int i = 0; i < btnLayoutLeft.getChildCount() && i < btnLayoutRight.getChildCount(); i++)
                    {
                        Button buttonLeft = (Button) btnLayoutLeft.getChildAt(i);
                        textArrayleft.add(buttonLeft.getText().toString());
                        Button buttonRight = (Button) btnLayoutRight.getChildAt(i);
                        textArrayRight.add(buttonRight.getText().toString());
                    }
                }
            }
        });
        if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getEntriesFromDbAsync.execute();
        }
    }

    private void btnRight_OnClick(final Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (isAnimStart)
                {
                    return;
                }
                tempButtonRight = (Button) view;
                ruWord = tempButtonRight.getText().toString();
                btnNoRight = tempButtonRight;
                compareWords(enWord, ruWord);
            }
        });
    }

    private void btnLeft_OnClick(final Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (isAnimStart)
                {
                    return;
                }
                tempButtonLeft = (Button) view;
                enWord = tempButtonLeft.getText().toString();
                btnNoRight = tempButtonLeft;
                compareWords(enWord, ruWord);
            }
        });
    }

    private void compareWords(final String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;

        boolean isFind = false;
        for (int i = 0; i < controlList.size(); i++)
        {
            if (controlList.get(i).getEnglish().equals(enword) && controlList.get(i).getTranslate().equals(ruword))
            {
                isFind = true;
                break;
            }
        }

        if (isFind)
        {
            wordIndex++;
            progressBar.setProgress(progressBar.getProgress()+1);
            tempButtonLeft.setBackgroundResource(R.drawable.text_btn_for_test_green);
            tempButtonRight.setBackgroundResource(R.drawable.text_btn_for_test_green);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "find_pair_fragm");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                SplashScreenActivity.speech.speak(enword, TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
            }
            else
            {
                SplashScreenActivity.speech.speak(enword, TextToSpeech.QUEUE_ADD, hashMap);
            }

            ViewPropertyAnimator animScale = tempButtonLeft.animate().scaleX(0).scaleY(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setStartDelay(0);
            animScale_Listener(animScale);
            tempButtonRight.animate().scaleX(0).scaleY(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setStartDelay(0);

            counterRightAnswer++;
        }
        else
        {
            counterRightAnswer--;
            Animation animNotRight = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.anim_not_right);
            animNotRight.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation)
                {

                }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                    btnNoRight.setBackgroundResource(R.drawable.text_button_for_test);
                    lockOrientation.unLock();
                }

                @Override
                public void onAnimationRepeat(Animation animation)
                {

                }
            });
            if (btnNoRight != null)
            {
                btnNoRight.setBackgroundResource(R.drawable.text_btn_for_test_red);
                btnNoRight.startAnimation(animNotRight);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        RandomNumberGenerator generator = new RandomNumberGenerator(BackgroundFragm.imagesId.length, (int) new Date().getTime());
        backImageView.setImageResource(BackgroundFragm.imagesId[generator.generate()]);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        boolean isFill = true;
        for (int i = 0; i < btnLayoutLeft.getChildCount(); i++)
        {
            Button button = (Button) btnLayoutLeft.getChildAt(i);
            if (!button.getText().equals(""))
            {
                isFill = false;
                break;
            }
        }
        if (isFill && spinnSelectedItem != null)
        {
            fillButtonsLayout(spinnSelectedItem, wordIndex, wordIndex - 1 + ROWS);
        }
    }

    private void animScale_Listener(ViewPropertyAnimator animScale)
    {
        if (animScale == null) return;
        animScale.setListener(new android.animation.Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(android.animation.Animator animation)
            {
                isAnimStart = true;
                lockOrientation.lock();
            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation)
            {
                isAnimStart = false;
                lockOrientation.unLock();
                if (tempButtonLeft != null && tempButtonRight != null)
                {
                    tempButtonLeft.setBackgroundResource(R.drawable.text_button_for_test);
                    tempButtonRight.setBackgroundResource(R.drawable.text_button_for_test);
                    enWord = null;
                    ruWord = null;
                    tempButtonLeft.setText(null);
                    tempButtonRight.setText(null);
                    tempButtonLeft.setVisibility(View.INVISIBLE);
                    tempButtonRight.setVisibility(View.INVISIBLE);

                    if (wordIndex - 1 < wordsCount)
                    {
                        boolean isFill = true;
                        for (int i = 0; i < btnLayoutLeft.getChildCount(); i++)
                        {
                            Button button = (Button) btnLayoutLeft.getChildAt(i);
                            if (!button.getText().equals(""))
                            {
                                isFill = false;
                                break;
                            }
                        }
                        if (isFill)
                        {
                            fillButtonsLayout(spinnSelectedItem, wordIndex, wordIndex - 1 + ROWS);
                            return;
                        }
                        buttonsToDown(btnLayoutLeft, tempButtonLeft.getX(), tempButtonLeft.getY(), false);
                        buttonsToDown(btnLayoutRight, tempButtonRight.getX(), tempButtonRight.getY(), true);
                    }
                    if (wordIndex - 1 == wordsCount)
                    {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(testResults.getOverallResult(counterRightAnswer, wordsCount));
                        list.add(counterRightAnswer + " из " + wordsCount);

                        Bundle bundle = new Bundle();
                        bundle.putString(dialogTestComplete.KEY_RESULT, list.get(0));
                        bundle.putString(dialogTestComplete.KEY_ERRORS, list.get(1));
                        dialogTestComplete.setArguments(bundle);
                        dialogTestComplete.setCancelable(false);
                        dialogTestComplete.show(fragmentManager, "dialog_complete_find_pair");
                    }
                }
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation)
            {
                //lockOrientation.unLock();
            }

            @Override
            public void onAnimationRepeat(android.animation.Animator animation)
            {

            }
        });
    }

    public void buttonsToDown(LinearLayout layout, float x, float y, final boolean isListen)
    {
        ViewPropertyAnimator animator;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getChildAt(0).getLayoutParams();
        int topMargin = layoutParams.topMargin;
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            Button button = (Button) layout.getChildAt(i);
            float X = button.getX();
            float Y = button.getY();
            if (Y < y && x == X)
            {
                animator = button.animate()
                        .translationYBy(button.getHeight() + topMargin)
                        .setDuration(300)
                        .setStartDelay(0)
                        .setInterpolator(new AccelerateInterpolator()).setListener(null);
                if (i == 0 && isListen)
                {
                    animator.setListener(new android.animation.Animator.AnimatorListener()
                    {
                        @Override
                        public void onAnimationStart(android.animation.Animator animation)
                        {
                            isAnimStart = true;
                        }

                        @Override
                        public void onAnimationEnd(android.animation.Animator animation)
                        {
                            isAnimStart = false;
                        }

                        @Override
                        public void onAnimationCancel(android.animation.Animator animation)
                        {
                            //lockOrientation.unLock();
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

    @Override
    public void dialogCompleteResult(int res)
    {
        appSettings.saveTestFragmentState(TAG, null);
        if (res == 0)
        {
            addToStudiedList();
            spinnSelectedIndex = spinnListDict.getSelectedItemPosition();
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
            spinnSelectedIndex = -1;
            getActivity().onBackPressed();
            if (arrStudiedDict.size() > 0)
            {
                DialogChangePlayList dialogChangePlayList = new DialogChangePlayList();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(dialogChangePlayList.KEY_LIST_DICT, arrStudiedDict);
                dialogChangePlayList.setArguments(bundle);
                dialogChangePlayList.setCancelable(false);
                dialogChangePlayList.show(getFragmentManager(), dialogChangePlayList.TAG);
            }
        }
    }

    private void addToStudiedList()
    {
        ArrayList<String> playList = appSettings.getPlayList();

        boolean containsInPlayList = playList.contains(spinnListDict.getSelectedItem().toString());
        boolean contains = arrStudiedDict.contains(spinnListDict.getSelectedItem().toString());
        if (counterRightAnswer == wordsCount && !contains && containsInPlayList)
        {
            arrStudiedDict.add(spinnListDict.getSelectedItem().toString());
            counterRightAnswer = 0;
        }
    }

    private void hideWordButtons()
    {
        for (int i = 0; i < btnLayoutLeft.getChildCount(); i++)
        {
            Button btnLeft = (Button) btnLayoutLeft.getChildAt(i);
            Button btnRight = (Button) btnLayoutRight.getChildAt(i);
            btnLeft.setVisibility(View.INVISIBLE);
            btnLeft.setText(null);
            btnRight.setVisibility(View.INVISIBLE);
            btnRight.setText(null);
        }
    }

    private void topPanelVisible(float touchDown, float touchUp, boolean isOpen)
    {
        if (touchDown < touchUp && !isOpen)
        {
            topPanel.animate().y(topPanelParams.bottomMargin).setDuration(1000).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new android.animation.Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(android.animation.Animator animator)
                {

                }

                @Override
                public void onAnimationEnd(android.animation.Animator animator)
                {
                    FindPairFragment.isOpen = true;
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animator)
                {

                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animator)
                {

                }
            });
        }
        else if (touchDown > touchUp && isOpen)
        {
            topPanel.animate().y(topPanelParams.topMargin).setDuration(1000).setListener(new android.animation.Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(android.animation.Animator animator)
                {

                }

                @Override
                public void onAnimationEnd(android.animation.Animator animator)
                {
                    FindPairFragment.isOpen = false;
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animator)
                {

                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animator)
                {

                }
            });
        }
    }
    private void topPanelButtons_OnClick()
    {
        topPanelButtonOK.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                topPanelVisible(1, 0, isOpen);
            }
        });
        topPanelButtonFinish.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                topPanelVisible(1, 0, isOpen);
                spinnListDict.setSelection(-1);
                spinnSelectedIndex = -1;
                getActivity().onBackPressed();
            }
        });
        topPanelButtonThreePoints.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                topPanelVisible(0, 1, isOpen);
            }
        });
    }


}
