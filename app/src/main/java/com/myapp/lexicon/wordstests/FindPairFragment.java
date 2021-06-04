package com.myapp.lexicon.wordstests;


import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.Word;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.main.BackgroundFragm;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;


@AndroidEntryPoint
public class FindPairFragment extends Fragment implements DialogTestComplete.IDialogComplete_Result
{
    public static final String TAG = "tag_find_pair";
    private final int ROWS = 5;

    private static RelativeLayout.LayoutParams saveTopPanelParams;

    private LinearLayout topPanel;
    private RelativeLayout.LayoutParams topPanelParams;
    private float touchDown = 0, touchUp = 0;
    private Button topPanelButtonOK;
    private Button topPanelButtonFinish;
    private ImageButton topPanelButtonThreePoints;
    private final long duration = 1000;
    private static boolean isOpen = false;

    private LinearLayout btnLayoutLeft, btnLayoutRight;

    private Spinner spinnListDict;
    private int spinnSelectedIndex = -1;
    private String spinnSelectedItem;

    private ProgressBar progressBar;
    private ImageView backImageView;
    private TextView tvProgressValue;

    private LockOrientation lockOrientation;
    private int wordIndex = 0;
    //private int wordsCount;
    private int counterRightAnswer = 0;

    private List<Word> wordList;
    private static final ArrayList<String> textArrayleft = new ArrayList<>();
    private static final ArrayList<String> textArrayRight = new ArrayList<>();
    private ArrayList<String> arrStudiedDict;
    private ArrayList<Word> controlList = new ArrayList<>();
    private ArrayList<Word> additionalList;
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

    private TestViewModel testVM;
    private final CompositeDisposable composite = new CompositeDisposable();


    public FindPairFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);

        testVM = new ViewModelProvider(this).get(TestViewModel.class);

        storedListDict = new ArrayList<>();
        arrStudiedDict = new ArrayList<>();
        if (getActivity() != null)
        {
            appSettings = new AppSettings(getActivity());
        }


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SPINN_SELECT_ITEM, spinnSelectedItem);
        outState.putInt(KEY_SPINN_SELECT_INDEX, spinnSelectedIndex);
        outState.putInt(KEY_PROGRESS, progressBar.getProgress());
        outState.putInt(KEY_PROGRESS_MAX, progressBar.getMax());
        outState.putInt(KEY_WORD_INDEX, wordIndex);
        outState.putInt(KEY_WORDS_COUNT, wordList.size());
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

        if (getActivity() != null)
        {
            if (isRemoving() && wordIndex -1 < wordList.size() && counterRightAnswer > 1 && wordList.size() >= ROWS)
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
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        appData = AppData.getInstance();
        lockOrientation = new LockOrientation(getActivity());
        if (getActivity() != null)
        {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            metrics = new DisplayMetrics();
            display.getMetrics(metrics);
        }

        View fragment_view = inflater.inflate(R.layout.t_find_pair_fragment, container, false);

        backImageView = fragment_view.findViewById(R.id.back_image_view1);

        topPanel = fragment_view.findViewById(R.id.top_panel);
        topPanelParams = (RelativeLayout.LayoutParams) topPanel.getLayoutParams();
        LinearLayout linLayout = fragment_view.findViewById(R.id.lin_layout_find_pair);
        TextView headerTopPanel = fragment_view.findViewById(R.id.header_top_panel);
        headerTopPanel.setText(R.string.text_find_pair_words);
        topPanelButtonOK = fragment_view.findViewById(R.id.btn_ok);
        topPanelButtonFinish = fragment_view.findViewById(R.id.btn_complete);
        topPanelButtonThreePoints = fragment_view.findViewById(R.id.btn_more_horiz);
        topPanelButtons_OnClick();
        linLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    touchDown = event.getY();
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    touchUp = event.getY();
                }
                topPanelVisible(touchDown, touchUp, isOpen);
                return true;
            }
        });

        spinnListDict = fragment_view.findViewById(R.id.spinner_dict);

        progressBar = fragment_view.findViewById(R.id.prog_bar_find_pair);
        tvProgressValue = fragment_view.findViewById(R.id.tv_progress_value);
        //setProgressValue(0, wordsCount);

        btnLayoutLeft = fragment_view.findViewById(R.id.btn_layout_left);
        btnLayoutRight = fragment_view.findViewById(R.id.btn_layout_right);

        if (savedInstanceState == null)
        {
            //hideWordButtons();
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
            //wordsCount = savedInstanceState.getInt(KEY_WORDS_COUNT);
            controlListSize = savedInstanceState.getInt(KEY_CONTROL_LIST_SIZE);
            counterRightAnswer = savedInstanceState.getInt(KEY_COUNTER_RIGHT_ANSWER);
            arrStudiedDict = savedInstanceState.getStringArrayList(KEY_ARRAY_STUDIED_DICT);
            controlList = savedInstanceState.getParcelableArrayList(KEY_CONTROL_LIST);
            additionalList = savedInstanceState.getParcelableArrayList(KEY_ADDITIONAL_LIST);
            storedListDict = savedInstanceState.getStringArrayList(KEY_STORED_DICT_LIST);
            setProgressValue(progressBar.getProgress(), progressBar.getMax());
        }

//        if (savedInstanceState != null)
//        {
//            if (textArrayleft.size() <= btnLayoutLeft.getChildCount() && textArrayRight.size() <= btnLayoutRight.getChildCount())
//            {
//                for (int i = 0; i < btnLayoutLeft.getChildCount() && i < btnLayoutRight.getChildCount(); i++)
//                {
//                    Button buttonLeft = (Button) btnLayoutLeft.getChildAt(i);
//                    Button buttonRight = (Button) btnLayoutRight.getChildAt(i);
//                    try
//                    {
//                        if (!textArrayleft.get(i).equals(""))
//                        {
//                            buttonLeft.setText(textArrayleft.get(i));
//                        }
//
//                    } catch (Exception e)
//                    {
//                        buttonLeft.setVisibility(View.INVISIBLE);
//                        buttonLeft.setText(null);
//                    }
//
//                    try
//                    {
//                        if (!textArrayRight.get(i).equals(""))
//                        {
//                            buttonRight.setText(textArrayRight.get(i));
//                        }
//                    }catch (Exception e)
//                    {
//                        buttonRight.setVisibility(View.INVISIBLE);
//                        buttonRight.setText(null);
//                    }
//                    btnLeft_OnClick(buttonLeft);
//                    btnRight_OnClick(buttonRight);
//                }
//            }
//        }

        testVM.getDictList().observe(getViewLifecycleOwner(), list -> {
            Word currentWord = testVM.getCurrentWord().getValue();
            ArrayAdapter<String> spinnAdapter = new ArrayAdapter<>(getActivity(), R.layout.my_content_spinner_layout, list);
            spinnListDict.setAdapter(spinnAdapter);
            spinnListDict_OnItemSelectedListener();
            if (currentWord != null && list.contains(currentWord.getDictName()))
            {
                int position = spinnAdapter.getPosition(currentWord.getDictName());
                spinnListDict.setSelection(position);
                testVM.getWordsByDictName(currentWord.getDictName());
            }
        });

        testVM.getWordsList().observe(getViewLifecycleOwner(), words -> {
            if (words.size() > 0)
            {
                //wordsCount = words.size();
                wordList = words;
                setProgressValue(0, words.size());
                startTest();
                //return;
            }
        });


        //setItemsToSpinnListDict();

        testResults = new TestResults(getActivity());

        fragmentManager = getActivity().getSupportFragmentManager();
        dialogTestComplete = new DialogTestComplete();
        dialogTestComplete.setListener(this);

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
                String selectedDict = spinnListDict.getSelectedItem().toString();
                testVM.getWordsByDictName(selectedDict);
                //startTest();
                topPanelVisible(1, 0, isOpen);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    private void startTest()
    {
        wordIndex = 0;
        counterRightAnswer = 0;
        if (wordList != null)
        {
            int wordsCount = wordList.size();
            progressBar.setMax(wordsCount);
            progressBar.setProgress(wordIndex);
            setProgressValue(progressBar.getProgress(), progressBar.getMax());
            hideWordButtons();

            if (wordsCount < ROWS)
            {
                fillButtonsLayout(wordIndex, wordsCount);
            }
            else
            {
                fillButtonsLayout(wordIndex, wordIndex + ROWS);
            }
        }
    }

    private void fillButtonsLayout(int start, int end)
    {
        if (wordList != null && !wordList.isEmpty())
        {
            controlList.clear();
            List<Word> subList = this.wordList.subList(start, end);
            controlList.addAll(subList);
            additionalList = new ArrayList<>();
            additionalList.addAll(controlList);
            controlListSize = controlList.size();
            Date date = new Date();
            RandomNumberGenerator randGenRight = new RandomNumberGenerator(controlListSize, (int) date.getTime());
            long delay = 0;
            for (int i = 0; i < subList.size(); i++)
            {
                Button btnLeft = (Button) btnLayoutLeft.getChildAt(i);
                Button btnRight = (Button) btnLayoutRight.getChildAt(i);

                btnLeft.setScaleX(1.0f);
                btnLeft.setScaleY(1.0f);
                btnRight.setScaleX(1.0f);
                btnRight.setScaleY(1.0f);
                btnLeft.setText(subList.get(i).getEnglish());
                btnRight.setText(subList.get(randGenRight.generate()).getTranslate());
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
            setProgressValue(wordIndex, wordList.size());
            wordIndex++;
            progressBar.setProgress(progressBar.getProgress()+1);
            tempButtonLeft.setBackgroundResource(R.drawable.text_btn_for_test_green);
            tempButtonRight.setBackgroundResource(R.drawable.text_btn_for_test_green);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "find_pair_fragm");
            try
            {
                SplashScreenActivity.speech.setLanguage(Locale.US);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    SplashScreenActivity.speech.speak(enword, TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                }
                else
                {
                    SplashScreenActivity.speech.speak(enword, TextToSpeech.QUEUE_ADD, hashMap);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            ViewPropertyAnimator animScale = tempButtonLeft.animate().scaleX(0).scaleY(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setStartDelay(0);
            animScale_Listener(animScale);
            tempButtonRight.animate().scaleX(0).scaleY(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setStartDelay(0);

            counterRightAnswer++;
        }
        else
        {
            counterRightAnswer--;
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

    private void setProgressValue(double progressValue, double progressMax)
    {
        if (progressMax != 0)
        {
            double percentProgress = progressValue / progressMax * 100;
            String value = BigDecimal.valueOf(percentProgress).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue() + "%";
            tvProgressValue.setText(value);
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
//        boolean isFill = true;
//        for (int i = 0; i < btnLayoutLeft.getChildCount(); i++)
//        {
//            Button button = (Button) btnLayoutLeft.getChildAt(i);
//            if (!button.getText().equals(""))
//            {
//                isFill = false;
//                break;
//            }
//        }
//        if (isFill && spinnSelectedItem != null)
//        {
//            fillButtonsLayout(wordIndex, wordIndex + ROWS);
//        }
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

                    if (wordIndex < wordList.size())
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
                            fillButtonsLayout(wordIndex, wordIndex + ROWS);
                            return;
                        }
                        buttonsToDown(btnLayoutLeft, tempButtonLeft.getX(), tempButtonLeft.getY(), false);
                        buttonsToDown(btnLayoutRight, tempButtonRight.getX(), tempButtonRight.getY(), true);
                    }
                    if (wordIndex == wordList.size() - 1)
                    {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(testResults.getOverallResult(counterRightAnswer, wordList.size()));
                        list.add(counterRightAnswer + " из " + wordList.size());

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

    private void buttonsToDown(LinearLayout layout, float x, float y, final boolean isListen)
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
            if (getActivity() != null && fragmentManager != null)
            {
                getActivity().onBackPressed();
                if (arrStudiedDict.size() > 0)
                {
                    DialogChangePlayList dialogChangePlayList = new DialogChangePlayList();
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(dialogChangePlayList.KEY_LIST_DICT, arrStudiedDict);
                    dialogChangePlayList.setArguments(bundle);
                    dialogChangePlayList.setCancelable(false);
                    dialogChangePlayList.show(fragmentManager, dialogChangePlayList.TAG);
                }
            }
        }
    }

    private void addToStudiedList()
    {
        ArrayList<String> playList = appSettings.getPlayList();

        boolean containsInPlayList = playList.contains(spinnListDict.getSelectedItem().toString());
        boolean contains = arrStudiedDict.contains(spinnListDict.getSelectedItem().toString());
        if (counterRightAnswer == wordList.size())
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
                topPanelVisible(0, 1, isOpen);
            }
        });
    }

    @Override
    public void onDestroy()
    {
        composite.dispose();
        composite.clear();
        super.onDestroy();
    }
}
