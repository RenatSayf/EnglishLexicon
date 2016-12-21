package com.myapp.lexicon;


import android.animation.Animator;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class t_OneOfFiveTest extends Fragment implements t_Animator.ITextViewToLeftListener, t_Animator.ITextViewToRightListener, t_DialogTestComplete.IDialogComplete_Result
{
    public static final int ROWS = 5;

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

    private static ArrayList<String> storedListDict = new ArrayList<>();
    private static ArrayList<DataBaseEntry> controlList;
    private static ArrayList<DataBaseEntry> additionalList;
    private static int additonalCount = 0;
    private static int wordIndex = 1;
    private static float buttonY;
    private static float buttonX;
    private static z_RandomNumberGenerator randomGenerator;
    private static ArrayList<DataBaseEntry> listFromDB;
    private static int indexEn = -1;
    private static int indexRu = -1;
    private static ArrayList<String> textArray = new ArrayList<>();
    private DisplayMetrics displayMetrics;

    private TextView textView;
    private LinearLayout buttonsLayout;
    private Spinner spinnListDict;
    private ProgressBar progressBar;
    private int spinnSelectedIndex = -1;
    private String spinnSelectedItem;
    private int wordsCount;
    private int controlListSize = 0;
    private Button tempButton;
    private int tempButtonId;
    private t_Animator animator;
    private z_LockOrientation lockOrientation;
    private static int counterRightAnswer = 0;
    private t_TestResults testResults;
    private t_DialogTestComplete dialogTestComplete;
    private ArrayList<String> arrStudiedDict = new ArrayList<>();

    private String KEY_BUTTON_ID = "key_button_id";
    private String KEY_TEXT = "key_text";
    private String KEY_CONTROL_LIST_SIZE = "key_control_list_size";
    private String KEY_WORDS_COUNT = "key_words_count";
    private String KEY_SPINN_SELECT_ITEM = "key_spinn_select_item";
    private String KEY_SPINN_SELECT_INDEX = "key_spinn_select_index";
    private String KEY_PROGRESS = "key_progress";
    private String KEY_PROGRESS_MAX = "key_progress_max";
    private String KEY_COUNTER_RIGHT_ANSWER = "key_counter_right";

    static FragmentActivity activity;
    static FragmentManager fragmentManager;
    public t_OneOfFiveTest()
    {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(KEY_BUTTON_ID, tempButtonId);
        outState.putString(KEY_TEXT, textView.getText().toString());
        outState.putInt(KEY_CONTROL_LIST_SIZE, controlListSize);
        outState.putInt(KEY_WORDS_COUNT, wordsCount);
        outState.putString(KEY_SPINN_SELECT_ITEM, spinnSelectedItem);
        outState.putInt(KEY_SPINN_SELECT_INDEX, spinnSelectedIndex);
        outState.putInt(KEY_PROGRESS_MAX, progressBar.getMax());
        outState.putInt(KEY_PROGRESS, progressBar.getProgress());
        //outState.putInt(KEY_COUNTER_RIGHT_ANSWER, counterRightAnswer);

        if (isOpen)
        {
            saveTopPanelParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            saveTopPanelParams.setMargins(topPanelParams.leftMargin, (int) topPanel.getY(), topPanelParams.rightMargin, topPanelParams.height);
        }

        saveButtonsLayoutState();
        super.onSaveInstanceState(outState);
    }

    private void saveButtonsLayoutState()
    {
        Map<Float, String> stringMap = new HashMap<>();
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            stringMap.put(button.getY(), button.getText().toString());
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
        textArray.clear();
        for (String item : stringCollection)
        {
            textArray.add(item);
        }
        return;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        if (savedInstanceState == null && t_Tests.bundleOneOfFiveTest.containsKey(KEY_TEXT))
        {
            savedInstanceState = t_Tests.bundleOneOfFiveTest;
        }
        activity = getActivity();
        fragmentManager = getFragmentManager();
        lockOrientation = new z_LockOrientation(activity);
        testResults = new t_TestResults(activity);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        View fragment_view = inflater.inflate(R.layout.t_one_of_five_test, container, false);

        topPanel = (LinearLayout) fragment_view.findViewById(R.id.top_panel);
        topPanelParams = (RelativeLayout.LayoutParams) topPanel.getLayoutParams();
        headerTopPanel = (TextView) fragment_view.findViewById(R.id.header_top_panel);
        headerTopPanel.setText("Выбери правильно");
        topPanelButtonOK = (Button) fragment_view.findViewById(R.id.btn_ok);
        topPanelButtonFinish = (Button) fragment_view.findViewById(R.id.btn_complete);
        topPanelButtons_OnClick();

        spinnListDict= (Spinner) fragment_view.findViewById(R.id.spinner_dict);
        buttonsLayout = (LinearLayout) fragment_view.findViewById(R.id.layout_1of5);
        textView = (TextView) fragment_view.findViewById(R.id.text_view_1of5);
        progressBar = (ProgressBar) fragment_view.findViewById(R.id.progress_test1of5);
        animator = t_Animator.getInstance();
        dialogTestComplete = t_DialogTestComplete.getInstance();
        dialogTestComplete.setIDialogCompleteResult(t_OneOfFiveTest.this);

        if (savedInstanceState == null)
        {
            buttonsLeftGone();
        }

        if (savedInstanceState != null)
        {
            textView.setText(savedInstanceState.getString(KEY_TEXT));
            tempButtonId = savedInstanceState.getInt(KEY_BUTTON_ID);
            controlListSize = savedInstanceState.getInt(KEY_CONTROL_LIST_SIZE);
            wordsCount = savedInstanceState.getInt(KEY_WORDS_COUNT);
            spinnSelectedItem = savedInstanceState.getString(KEY_SPINN_SELECT_ITEM);
            spinnSelectedIndex = savedInstanceState.getInt(KEY_SPINN_SELECT_INDEX);
            progressBar.setMax(savedInstanceState.getInt(KEY_PROGRESS_MAX));
            progressBar.setProgress(savedInstanceState.getInt(KEY_PROGRESS));

            for (int i = 0; i < buttonsLayout.getChildCount(); i++)
            {
                Button button = (Button) buttonsLayout.getChildAt(i);
                try
                {
                    button.setText(textArray.get(i));
                } catch (Exception e)
                {
                    button.setText(null);
                }
                if (button.getText().toString().equals(""))
                {
                    button.setVisibility(View.GONE);
                }
                btnLeft_OnClick(i, button);
            }
            animator.setLayout(buttonsLayout, textView);
        }

        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();

        buttonsLayout.setOnTouchListener(new View.OnTouchListener()
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

        return fragment_view;
    }

    private void buttonsLeftGone()
    {
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            button.setTranslationX(0);
            button.setTranslationY(displayMetrics.widthPixels);
            button.setText(null);
            button.setVisibility(View.GONE);
        }
        animator.setLayout(buttonsLayout, textView);
        animator.setTextViewToLeftListener(t_OneOfFiveTest.this);
        animator.setTextViewToRightListener(t_OneOfFiveTest.this);
    }

    private void setItemsToSpinnListDict()
    {
        if (storedListDict.size() > 0)
        {
            ArrayAdapter<String> spinnAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.my_content_spinner_layout, storedListDict);
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

    private void spinnListDict_OnItemSelectedListener()
    {
        spinnListDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id)
            {
                if (position == spinnSelectedIndex) return;
                textArray.clear();
                startTest(position);
                topPanelVisible(1, 0, isOpen);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    private void startTest(final int position)
    {
        wordIndex = 1;
        spinnSelectedItem = spinnListDict.getSelectedItem().toString();
        DataBaseQueries.GetWordsCountAsync getWordsCount = new DataBaseQueries.GetWordsCountAsync()
        {
            @Override
            public void resultAsyncTask(int res)
            {
                wordsCount = res;
                fillLayoutLeft(wordsCount);
                spinnSelectedIndex = position;
                progressBar.setMax(wordsCount);
                progressBar.setProgress(0);
                counterRightAnswer = 0;
                topPanelVisible(0, 1, isOpen);
            }
        };
        getWordsCount.execute(spinnSelectedItem);
    }

    private void fillLayoutLeft(final int rowsCount)
    {
        buttonsLeftGone();
        if (rowsCount <= 0) return;
        int count = rowsCount;
        if (count > ROWS)
        {
            count = ROWS;
        }

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
                randomGenerator = new z_RandomNumberGenerator(controlListSize, 133);
                long start_delay = 0;
                for (int i = 0; i < controlList.size(); i++)
                {
                    Button button = (Button) buttonsLayout.getChildAt(i);
                    button.setTranslationX(displayMetrics.widthPixels);
                    button.setTranslationY(0);
                    button.setVisibility(View.VISIBLE);
                    button.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null).setStartDelay(start_delay);
                    start_delay += 70;
                    btnLeft_OnClick(i, button);
                    button.setText(controlList.get(i).get_translate());
                    wordIndex++;
                }
                int randIndex = randomGenerator.generate();
                textView.setText(list.get(randIndex).get_english());
                textView.setTranslationX(-displayMetrics.widthPixels);
                textView.setTranslationY(0);
                textView.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null).setStartDelay(start_delay);
            }
        };
        asyncTask.execute(spinnSelectedItem, wordIndex, count);
        asyncTask = null;
    }

    private int btnIndex;
    private void btnLeft_OnClick(final int index, final View view)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                tempButton = (Button) view;
                tempButtonId = tempButton.getId();
                btnIndex = index;
                buttonY = tempButton.getY();
                buttonX = tempButton.getX();
                compareWords(spinnSelectedItem,textView.getText().toString(), tempButton.getText().toString());
            }
        });
    }

    private void compareWords(String tableName, String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;

        if (enword != null && ruword != null)
        {
            lockOrientation.lock();
            for (int i = 0; i < controlList.size(); i++)
            {
                if (controlList.get(i).get_english().equals(enword))
                {
                    indexEn = i;
                }
                if (controlList.get(i).get_translate().equals(ruword))
                {
                    indexRu = i;
                }
            }

            if (indexEn == indexRu && indexEn != -1 && indexRu != -1)
            {
                AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
                {
                    @Override
                    public void resultAsyncTask(ArrayList<DataBaseEntry> list)
                    {
                        listFromDB = list;
                        progressBar.setProgress(progressBar.getProgress()+1);
                        a_SplashScreenActivity.speech.speak(textView.getText().toString(), TextToSpeech.QUEUE_ADD, a_SplashScreenActivity.map);
                        a_SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
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

                        animator.textViewToLeft(displayMetrics);
                        animator.buttonToRight(buttonsLayout, tempButtonId, displayMetrics);
                        counterRightAnswer++;
                    }
                };
                asyncTask.execute(tableName, wordIndex, wordIndex);
            }
            else
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
                        tempButton.setBackgroundResource(R.drawable.text_button_for_test);
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
    }

    @Override
    public void textViewToLeftListener(int result, TextView textView, Button button)
    {
        int range = 127;
        if (listFromDB.size() > 0)
        {
            controlList.set(indexEn, listFromDB.get(0));
            button.setText(listFromDB.get(0).get_translate());
            //arrayHelper.updateArray(0, listFromDB.get(0).get_translate());
            if (controlListSize != controlList.size())
            {
                randomGenerator = new z_RandomNumberGenerator(controlList.size(), range);
                controlListSize = controlList.size();
            }
            int randomNumber = randomGenerator.generate();
            if (randomNumber < 0)
            {
                randomGenerator = new z_RandomNumberGenerator(controlListSize, range);
                randomNumber = randomGenerator.generate();
            }
            String english = controlList.get(randomNumber).get_english();
            textView.setText(controlList.get(randomNumber).get_english());
        }
        else if (listFromDB.size() == 0 && controlList.size() <= ROWS)
        {
            try
            {
                controlList.remove(indexEn);
                button.setText(additionalList.get(additonalCount).get_translate());
                additonalCount++;
                textView.setText("");
                if (controlList.size() > 0)
                {
                    randomGenerator = new z_RandomNumberGenerator(controlList.size(), range);
                    int randomNumber = randomGenerator.generate();
                    textView.setText(controlList.get(randomNumber).get_english());
                }
            } catch (Exception e)
            {
                Toast.makeText(getActivity(), "Ошибка - "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        if (buttonY > 0)
        {
            animator.buttonsToDown(buttonX, buttonY);
        } else
        {
            animator.textViewToRight();
        }
        wordIndex++;
    }

    @Override
    public void textViewToRightListener(int result, TextView textView)
    {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        if (textView.getText().toString() == "")
        {
            //Toast.makeText(activity,"Тест завершен",Toast.LENGTH_SHORT).show();
            ArrayList<String> list = new ArrayList<String>();
            list.add(testResults.getOverallResult(counterRightAnswer, wordsCount));
            list.add(counterRightAnswer + activity.getString(R.string.text_out_of) + wordsCount);
            Bundle bundle = new Bundle();
            bundle.putString(dialogTestComplete.KEY_RESULT, list.get(0));
            bundle.putString(dialogTestComplete.KEY_ERRORS, list.get(1));
            dialogTestComplete.setArguments(bundle);
            dialogTestComplete.setCancelable(false);
            dialogTestComplete.show(fragmentManager, "dialog_complete_lexicon");
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
                t_DialogChangePlayList dialogChangePlayList = new t_DialogChangePlayList();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(dialogChangePlayList.KEY_LIST_DICT, arrStudiedDict);
                dialogChangePlayList.setArguments(bundle);
                dialogChangePlayList.setCancelable(false);
                dialogChangePlayList.show(getFragmentManager(), "dialog_change_pl_lexicon");
            }
        }
    }

    private void addToStudiedList()
    {
        boolean containsInPlayList = false;
        for (p_ItemListDict item : a_MainActivity.getPlayList())
        {
            if (item.get_dictName().equals(spinnListDict.getSelectedItem()))
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

    @Override
    public void onPause()
    {
        super.onPause();
        t_Tests.bundleOneOfFiveTest = new Bundle();
        onSaveInstanceState(t_Tests.bundleOneOfFiveTest);
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
                    t_OneOfFiveTest.isOpen = true;
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
                    t_OneOfFiveTest.isOpen = false;
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
