package com.myapp.lexicon.wordstests;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.RandomNumberGenerator;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FindPairFragment extends Fragment implements DialogTestComplete.IDialogComplete_Result
{
    public static int ROWS = 5;

    private static RelativeLayout.LayoutParams saveTopPanelParams;

    private LinearLayout linLayout;
    private LinearLayout topPanel;
    private RelativeLayout.LayoutParams topPanelParams;
    private float touchDown = 0, touchUp = 0;
    private Button topPanelButtonOK;
    private Button topPanelButtonFinish;
    private TextView headerTopPanel;
    private long duration = 1000;
    private static boolean isOpen = false;

    private LinearLayout btnLayoutLeft, btnLayoutRight;

    private Spinner spinnListDict;
    private int spinnSelectedIndex = -1;
    private String spinnSelectedItem;
    private static ArrayList<String> storedListDict = new ArrayList<>();

    private ProgressBar progressBar;

    private LockOrientation lockOrientation;
    private int wordIndex = 1;
    private int wordsCount;
    private static int counterRightAnswer = 0;
    private static ArrayList<String> textArrayleft = new ArrayList<>();
    private static ArrayList<String> textArrayRight = new ArrayList<>();
    private ArrayList<String> arrStudiedDict = new ArrayList<>();
    private static ArrayList<DataBaseEntry> controlList;
    private static ArrayList<DataBaseEntry> additionalList;
    private static int additonalCount = 0;
    private int controlListSize = 0;
    private int rangeRight = -1;
    private DisplayMetrics metrics;
    private static ArrayList<DataBaseEntry> listFromDB;
    private String enWord = null;
    private String ruWord = null;
    private Button tempButtonLeft;
    private Button tempButtonRight;
    private Button btnNoRight;
    private Animator animator;

    private DialogTestComplete dialogTestComplete;
    private TestResults testResults;
    private static FragmentManager fragmentManager;

    private String KEY_CONTROL_LIST_SIZE = "key_control_list_size";
    private String KEY_WORDS_COUNT = "key_words_count";
    private String KEY_SPINN_SELECT_ITEM = "key_spinn_select_item";
    private String KEY_SPINN_SELECT_INDEX = "key_spinn_select_index";
    private String KEY_PROGRESS = "key_progress";
    private String KEY_PROGRESS_MAX = "key_progress_max";
    private String KEY_WORD_INDEX = "key_word_index";
    private String KEY_NEXT_INDEX = "key_next_index";


    public FindPairFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString(KEY_SPINN_SELECT_ITEM, spinnSelectedItem);
        outState.putInt(KEY_SPINN_SELECT_INDEX, spinnSelectedIndex);
        outState.putInt(KEY_PROGRESS, progressBar.getProgress());
        outState.putInt(KEY_PROGRESS_MAX, progressBar.getMax());
        outState.putInt(KEY_WORD_INDEX, wordIndex);
        outState.putInt(KEY_WORDS_COUNT, wordsCount);
        outState.putInt(KEY_CONTROL_LIST_SIZE, controlListSize);

        if (isOpen)
        {
            saveTopPanelParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            saveTopPanelParams.setMargins(topPanelParams.leftMargin, (int) topPanel.getY(), topPanelParams.rightMargin, topPanelParams.height);
        }

//        for (int i = 0; i < btnLayoutLeft.getChildCount(); i++)
//        {
//            Button btnLeft = (Button) btnLayoutLeft.getChildAt(i);
//            Button btnRight = (Button) btnLayoutRight.getChildAt(i);
//            textArrayleft.set(i, btnLeft.getText().toString());
//            textArrayRight.set(i, btnRight.getText().toString());
//        }
        saveButtonsLayoutState();
        super.onSaveInstanceState(outState);
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
        return;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Tests.bundleFindPair = new Bundle();
        onSaveInstanceState(Tests.bundleFindPair);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        if (savedInstanceState == null && Tests.bundleFindPair.containsKey(KEY_WORD_INDEX))
        {
            savedInstanceState = Tests.bundleFindPair;
        }

        fragmentManager = getFragmentManager();
        lockOrientation = new LockOrientation(getActivity());
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        View fragment_view = inflater.inflate(R.layout.t_find_pair_fragment, container, false);
        topPanel = (LinearLayout) fragment_view.findViewById(R.id.top_panel);
        topPanelParams = (RelativeLayout.LayoutParams) topPanel.getLayoutParams();
        linLayout = (LinearLayout) fragment_view.findViewById(R.id.lin_layout_find_pair);
        headerTopPanel = (TextView) fragment_view.findViewById(R.id.header_top_panel);
        headerTopPanel.setText("Найди парные слова");
        topPanelButtonOK = (Button) fragment_view.findViewById(R.id.btn_ok);
        topPanelButtonFinish = (Button) fragment_view.findViewById(R.id.btn_complete);
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
        if (savedInstanceState != null && isOpen)
        {
            topPanel.setLayoutParams(saveTopPanelParams);
        }

        spinnListDict = (Spinner) fragment_view.findViewById(R.id.spinner_dict);
        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();
        if (savedInstanceState != null)
        {
            spinnSelectedItem = savedInstanceState.getString(KEY_SPINN_SELECT_ITEM);
            spinnSelectedIndex = savedInstanceState.getInt(KEY_SPINN_SELECT_INDEX);
        }

        progressBar = (ProgressBar) fragment_view.findViewById(R.id.prog_bar_find_pair);
        if (savedInstanceState != null)
        {
            progressBar.setProgress(savedInstanceState.getInt(KEY_PROGRESS));
            progressBar.setMax(savedInstanceState.getInt(KEY_PROGRESS_MAX));
        }

        if (savedInstanceState != null)
        {
            wordIndex = savedInstanceState.getInt(KEY_WORD_INDEX);
            wordsCount = savedInstanceState.getInt(KEY_WORDS_COUNT);
            controlListSize = savedInstanceState.getInt(KEY_CONTROL_LIST_SIZE);
        }

        btnLayoutLeft = (LinearLayout) fragment_view.findViewById(R.id.btn_layout_left);
        btnLayoutRight = (LinearLayout) fragment_view.findViewById(R.id.btn_layout_right);
        if (savedInstanceState == null)
        {
            hideWordButtons();
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

        testResults = new TestResults(getActivity());
        //dialogTestComplete = t_DialogTestComplete.getInstance();
        //dialogTestComplete.setIDialogCompleteResult(t_FindPairFragment.this);

        return fragment_view;
    }

    private void spinnListDict_OnItemSelectedListener()
    {
        spinnListDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id)
            {
                if (position == spinnSelectedIndex) return;
                startTest(position);
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

        new DataBaseQueries.GetLictTableAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<String> list)
            {
                ArrayAdapter<String> adapterSpinner= new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.my_content_spinner_layout, list);
                spinnListDict.setAdapter(adapterSpinner);
                spinnListDict.setSelection(spinnSelectedIndex);
                spinnListDict_OnItemSelectedListener();
                for (int i = 0; i < spinnListDict.getAdapter().getCount(); i++)
                {
                    storedListDict.add(spinnListDict.getAdapter().getItem(i).toString());
                }
            }
        }.execute();
    }

    private void startTest(final int position)
    {
        lockOrientation.lock();
        wordIndex = 0;
        spinnSelectedItem = spinnListDict.getSelectedItem().toString();
        DataBaseQueries.GetWordsCountAsync getWordsCount = new DataBaseQueries.GetWordsCountAsync()
        {
            @Override
            public void resultAsyncTask(int res)
            {
                wordsCount = res;
                spinnSelectedIndex = position;
                progressBar.setMax(res);
                progressBar.setProgress(0);
                counterRightAnswer = 0;
                topPanelVisible(0, 1, isOpen);
                hideWordButtons();
                if (wordsCount < ROWS)
                {
                    fillButtonsLayout(spinnSelectedItem, wordIndex, wordsCount);
                }
                else
                {
                    fillButtonsLayout(spinnSelectedItem, wordIndex, ROWS);
                }
            }
        };
        getWordsCount.execute(spinnSelectedItem);
    }

    private static int buttonMaxHeigt;
    private void fillButtonsLayout(String dictName, int start, int end)
    {
        AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<DataBaseEntry> list)
            {
                controlList = list;
                additionalList = new ArrayList<>();
                additonalCount = 0;
                for (DataBaseEntry entry : list)
                {
                    additionalList.add(entry);
                }
                controlListSize = controlList.size();
                RandomNumberGenerator randGenRight = new RandomNumberGenerator(controlListSize, rangeRight);
                long delay = 0;
                for (int i = 0; i < controlList.size(); i++)
                {
                    Button btnLeft = (Button) btnLayoutLeft.getChildAt(i);
                    Button btnRight = (Button) btnLayoutRight.getChildAt(i);

                    btnLeft.setScaleX(1.0f);
                    btnLeft.setScaleY(1.0f);
                    btnRight.setScaleX(1.0f);
                    btnRight.setScaleY(1.0f);
                    btnLeft.setText(controlList.get(i).get_english());
                    btnRight.setText(controlList.get(randGenRight.generate()).get_translate());
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
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };
        asyncTask.execute(dictName, start, end);
        asyncTask = null;
    }

    private void btnRight_OnClick(final Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Toast.makeText(getActivity(), button.getText(), Toast.LENGTH_SHORT).show();
                int requestedOrientation = getActivity().getRequestedOrientation();
                if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) return;
                tempButtonRight = (Button) view;
                ruWord = button.getText().toString();
                compareWords(spinnSelectedItem, enWord, ruWord);
                btnNoRight = (Button) view;
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
                //Toast.makeText(getActivity(), button.getText(), Toast.LENGTH_SHORT).show();
                int requestedOrientation = getActivity().getRequestedOrientation();
                if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) return;
                tempButtonLeft = (Button) view;
                enWord = button.getText().toString();
                compareWords(spinnSelectedItem, enWord, ruWord);
                btnNoRight = (Button) view;
            }
        });
    }

    private void compareWords(final String tableName, final String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;

        if (enword != null && ruword != null)
        {
            lockOrientation.lock();
            DataBaseQueries.GetRowIdOfWordAsync asyncTask = new DataBaseQueries.GetRowIdOfWordAsync()
            {
                @Override
                public void resultAsyncTask(Integer id)
                {
                    if (id > 0)
                    {
                        wordIndex++;
                        progressBar.setProgress(progressBar.getProgress()+1);
                        tempButtonLeft.setBackgroundResource(R.drawable.text_btn_for_test_green);
                        tempButtonRight.setBackgroundResource(R.drawable.text_btn_for_test_green);
                        SplashScreenActivity.speech.speak(enword, TextToSpeech.QUEUE_ADD, SplashScreenActivity.map);
                        SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
                        {
                            @Override
                            public void onStart(String utteranceId)
                            {

                            }

                            @Override
                            public void onDone(String utteranceId)
                            {

                            }

                            @Override
                            public void onError(String utteranceId)
                            {

                            }
                        });
                        ViewPropertyAnimator animScale = tempButtonLeft.animate().scaleX(0).scaleY(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setStartDelay(0);
                        animScale_Listener(animScale);
                        tempButtonRight.animate().scaleX(0).scaleY(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setStartDelay(0);

                        counterRightAnswer++;
                    }
                    if (id < 0)
                    {
                        //Toast.makeText(getActivity(), "Неправильно", Toast.LENGTH_SHORT).show();
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
                                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
            };
            asyncTask.execute(tableName, enword, ruword);
        }
    }

    private int t = 1;
    private void animScale_Listener(ViewPropertyAnimator animScale)
    {
        if (animScale == null) return;
        animScale.setListener(new android.animation.Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(android.animation.Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(android.animation.Animator animation)
            {
                if (tempButtonLeft != null && tempButtonRight != null)
                {
                    //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    tempButtonLeft.setBackgroundResource(R.drawable.text_button_for_test);
                    tempButtonRight.setBackgroundResource(R.drawable.text_button_for_test);
                    enWord = null;
                    ruWord = null;
                    tempButtonLeft.setText(null);
                    tempButtonRight.setText(null);
                    tempButtonLeft.setVisibility(View.INVISIBLE);
                    tempButtonRight.setVisibility(View.INVISIBLE);

                    buttonsToDown(btnLayoutLeft, tempButtonLeft.getX(), tempButtonLeft.getY(), false);
                    buttonsToDown(btnLayoutRight, tempButtonRight.getX(), tempButtonRight.getY(), true);

//                    boolean isFill = true;
//                    for (int i = 0; i < btnLayoutLeft.getChildCount(); i++)
//                    {
//                        Button button = (Button) btnLayoutLeft.getChildAt(i);
//                        if (!button.getText().equals(""))
//                        {
//                            isFill = false;
//                            break;
//                        }
//                    }
//                    if (isFill)
//                    {
//                        fillButtonsLayout(spinnSelectedItem, wordIndex + 1, wordIndex + ROWS);
//                    }
//
//                    if (wordIndex == wordsCount)
//                    {
//                        //Toast.makeText(getActivity(),"Завершено",Toast.LENGTH_SHORT).show();
//                        ArrayList<String> list = new ArrayList<String>();
//                        list.add(testResults.getOverallResult(counterRightAnswer, wordsCount));
//                        list.add(counterRightAnswer + getActivity().getString(R.string.text_out_of) + wordsCount);
//                        Bundle bundle = new Bundle();
//                        bundle.putString(dialogTestComplete.KEY_RESULT, list.get(0));
//                        bundle.putString(dialogTestComplete.KEY_ERRORS, list.get(1));
//                        dialogTestComplete.setArguments(bundle);
//                        dialogTestComplete.setCancelable(false);
//                        dialogTestComplete.show(getFragmentManager(), "dialog_complete_lexicon");
//                    }
                }
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

                        }

                        @Override
                        public void onAnimationEnd(android.animation.Animator animation)
                        {
                            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
                                fillButtonsLayout(spinnSelectedItem, wordIndex + 1, wordIndex + ROWS);
                            }

                            if (wordIndex == wordsCount)
                            {
                                //Toast.makeText(getActivity(),"Завершено",Toast.LENGTH_SHORT).show();
                                ArrayList<String> list = new ArrayList<String>();
                                list.add(testResults.getOverallResult(counterRightAnswer, wordsCount));
                                list.add(counterRightAnswer + getActivity().getString(R.string.text_out_of) + wordsCount);
                                if (dialogTestComplete == null)
                                {
                                    dialogTestComplete = new DialogTestComplete();
                                    dialogTestComplete.setIDialogCompleteResult(FindPairFragment.this);
                                }
                                if (dialogTestComplete != null)
                                {
                                    try
                                    {
                                        if (!dialogTestComplete.isAdded())
                                        {
                                            Bundle bundle = new Bundle();
                                            bundle.putString(dialogTestComplete.KEY_RESULT, list.get(0));
                                            bundle.putString(dialogTestComplete.KEY_ERRORS, list.get(1));
                                            dialogTestComplete.setArguments(bundle);
                                            dialogTestComplete.setCancelable(false);
                                            dialogTestComplete.show(getFragmentManager(), "dialog_complete_find_pair");
                                        }

                                    } catch (IllegalStateException e)
                                    {
                                        dialogTestComplete = null;
                                        return;
                                    }
                                }
                            }
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

    @Override
    public void dialogCompleteResult(int res)
    {
        if (res == 0)
        {
            addToStudiedList();
            spinnSelectedIndex = spinnListDict.getSelectedItemPosition();
            startTest(spinnSelectedIndex);
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
                dialogChangePlayList.show(getFragmentManager(), "dialog_change_pl_lexicon");
            }
        }
    }

    public ArrayList<String> getPlayList()
    {
        ArrayList<String> listDicts=new ArrayList<>();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.key_play_list), MODE_PRIVATE);
        String play_list_items = sharedPreferences.getString(getString(R.string.play_list_items), null);
        if (play_list_items != null && play_list_items.length() > 0)
        {
            String[] splitArray = play_list_items.split(" ");
            for (int i = 0; i < splitArray.length; i++)
            {
                listDicts.add(i, splitArray[i]);
            }
        }

        return listDicts;
    }

    private void addToStudiedList()
    {
        boolean containsInPlayList = false;
        for (String item : getPlayList())
        {
            if (item.equals(spinnListDict.getSelectedItem()))
            {
                containsInPlayList = true; break;
            }
        }
        boolean contains = arrStudiedDict.contains(spinnListDict.getSelectedItem());
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
    }


}
