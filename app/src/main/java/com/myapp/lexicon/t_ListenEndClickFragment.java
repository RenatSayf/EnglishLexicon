package com.myapp.lexicon;


import android.animation.Animator;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.util.TimeUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;



/**
 * A simple {@link Fragment} subclass.
 */
public class t_ListenEndClickFragment extends Fragment implements t_DialogTestComplete.IDialogComplete_Result
{
    public static final int ROWS = 5;

    private static RelativeLayout.LayoutParams saveTopPanelParams;
    private static boolean isOpen = false;
    private static ArrayList<String> storedListDict = new ArrayList<>();
    private static z_RandomNumberGenerator randomGenerator;
    private static ArrayList<DataBaseEntry> controlList;
    private static ArrayList<DataBaseEntry> additionalList;
    private static ArrayList<String> textArray = new ArrayList<>();
    private static String textEn;
    private static int indexEn = -1;
    private static int indexRu = -1;
    private static ArrayList<DataBaseEntry> listFromDB;
    private static int counterRightAnswer = 0;
    private static int additonalCount = 0;

    private RelativeLayout relLayout;
    private LinearLayout linLayout;
    private LinearLayout topPanel;
    private RelativeLayout.LayoutParams topPanelParams;
    private float touchDown = 0, touchUp = 0;
    private Button tempButton;
    private RelativeLayout buttonsLayout;
    private Spinner spinnListDict;
    private int spinnSelectedIndex = -1;
    private ImageButton buttonSpeech;
    private ProgressBar progressBar;
    private Button topPanelButtonOK;
    private Button topPanelButtonFinish;
    private int controlListSize = 0;
    private z_LockOrientation lockOrientation;
    private long duration = 1000;
    private int wordIndex = 1;
    private String spinnSelectedItem;
    private int wordsCount;
    private t_DialogTestComplete dialogTestComplete;
    private ArrayList<String> arrStudiedDict = new ArrayList<>();
    private t_TestResults testResults;
    private int range = 4;

    private String KEY_CONTROL_LIST_SIZE = "key_control_list_size";
    private String KEY_WORDS_COUNT = "key_words_count";
    private String KEY_SPINN_SELECT_ITEM = "key_spinn_select_item";
    private String KEY_SPINN_SELECT_INDEX = "key_spinn_select_index";
    private String KEY_PROGRESS = "key_progress";
    private String KEY_PROGRESS_MAX = "key_progress_max";
    private String KEY_WORD_INDEX = "key_word_index";


    public t_ListenEndClickFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(KEY_CONTROL_LIST_SIZE, controlListSize);
        outState.putInt(KEY_WORD_INDEX, wordIndex);
        outState.putInt(KEY_WORDS_COUNT, wordsCount);
        outState.putString(KEY_SPINN_SELECT_ITEM, spinnSelectedItem);
        outState.putInt(KEY_SPINN_SELECT_INDEX, spinnSelectedIndex);
        outState.putInt(KEY_PROGRESS, progressBar.getProgress());
        outState.putInt(KEY_PROGRESS_MAX, progressBar.getMax());

        if (isOpen)
        {
            saveTopPanelParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            saveTopPanelParams.setMargins(topPanelParams.leftMargin, (int) topPanel.getY(), topPanelParams.rightMargin, topPanelParams.height);
        }

        textArray.clear();
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            textArray.add(button.getText().toString());
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        if (savedInstanceState == null && t_Tests.bundleListenTest.containsKey(KEY_WORD_INDEX))
        {
            savedInstanceState = t_Tests.bundleListenTest;
        }

        lockOrientation = new z_LockOrientation(getActivity());
        View fragment_view = inflater.inflate(R.layout.t_listen_end_click_layout, container, false);
        relLayout = (RelativeLayout) fragment_view.findViewById(R.id.rel_layout);
        topPanel = (LinearLayout) fragment_view.findViewById(R.id.top_panel);
        topPanelParams = (RelativeLayout.LayoutParams) topPanel.getLayoutParams();
        linLayout = (LinearLayout) fragment_view.findViewById(R.id.linear_layout);
        spinnListDict = (Spinner) fragment_view.findViewById(R.id.spinner_dict);
        buttonsLayout = (RelativeLayout) fragment_view.findViewById(R.id.buttons_layout);
        buttonSpeech = (ImageButton) fragment_view.findViewById(R.id.btn_speech);
        buttonSpeech_OnClick();
        progressBar = (ProgressBar) fragment_view.findViewById(R.id.prog_bar_listen);
        topPanelButtonOK = (Button) fragment_view.findViewById(R.id.btn_ok);
        topPanelButtonFinish = (Button) fragment_view.findViewById(R.id.btn_complete);
        topPanelButtons_OnClick();

        testResults = new t_TestResults(getActivity());
        dialogTestComplete = t_DialogTestComplete.getInstance();
        dialogTestComplete.setIDialogCompleteResult(t_ListenEndClickFragment.this);

        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();

        if (savedInstanceState != null && isOpen)
        {
            topPanel.setLayoutParams(saveTopPanelParams);
        }

        if (savedInstanceState != null)
        {
            wordIndex = savedInstanceState.getInt(KEY_WORD_INDEX);
            controlListSize = savedInstanceState.getInt(KEY_CONTROL_LIST_SIZE);
            wordsCount = savedInstanceState.getInt(KEY_WORDS_COUNT);
            spinnSelectedItem = savedInstanceState.getString(KEY_SPINN_SELECT_ITEM);
            spinnSelectedIndex = savedInstanceState.getInt(KEY_SPINN_SELECT_INDEX);
            progressBar.setProgress(savedInstanceState.getInt(KEY_PROGRESS));
            progressBar.setMax(savedInstanceState.getInt(KEY_PROGRESS_MAX));

            for (int i = 0; i < buttonsLayout.getChildCount(); i++)
            {
                Button button = (Button) buttonsLayout.getChildAt(i);
                if (!textArray.get(i).equals(""))
                {
                    button.setText(textArray.get(i));
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

                topPanelVisible(touchDown, touchUp, isOpen);

                return true;
            }
        });

        return fragment_view;
    }

    private void buttonSpeech_OnClick()
    {
        buttonSpeech.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                topPanelVisible(1, 0, isOpen);
                a_SplashScreenActivity.speech.speak(textEn, TextToSpeech.QUEUE_ADD, a_SplashScreenActivity.map);
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
        lockOrientation.lock();
        wordIndex = 1;
        spinnSelectedItem = spinnListDict.getSelectedItem().toString();
        DataBaseQueries.GetWordsCountAsync getWordsCount = new DataBaseQueries.GetWordsCountAsync()
        {
            @Override
            public void resultAsyncTask(int res)
            {
                wordsCount = res;
                fillLayoutLeft();
                spinnSelectedIndex = position;
                progressBar.setMax(res);
                progressBar.setProgress(0);
                counterRightAnswer = 0;
                topPanelVisible(0, 1, isOpen);
            }
        };
        getWordsCount.execute(spinnSelectedItem);
    }

    private void fillLayoutLeft()
    {
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            button.setText("");
            button.setVisibility(View.GONE);
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

                for (int i = 0; i < controlList.size(); i++)
                {
                    Button button = (Button) buttonsLayout.getChildAt(i);
                    button.setText(controlList.get(i).get_translate());
                    button.setVisibility(View.VISIBLE);
                    btnLeft_OnClick(button);
                    wordIndex++;
                }
                randomGenerator = new z_RandomNumberGenerator(controlListSize, range);
                int randIndex = randomGenerator.generate();
                textEn = list.get(randIndex).get_english();
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };
        asyncTask.execute(spinnSelectedItem, wordIndex, ROWS);
        asyncTask = null;
    }

    private void btnLeft_OnClick(final View view)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int requestedOrientation = getActivity().getRequestedOrientation();
                if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                {
                    return;
                }
                lockOrientation.lock();
                tempButton = (Button) view;
                compareWords(spinnSelectedItem, textEn, tempButton.getText().toString());
            }
        });
    }

    private void compareWords(String tableName, String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;

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
                    if (tempButton != null)
                    {
                        tempButton.setText(textEn);
                        tempButton.setBackgroundResource(R.drawable.text_btn_for_test_green);
                    }

                    a_SplashScreenActivity.speech.speak(textEn, TextToSpeech.QUEUE_ADD, a_SplashScreenActivity.map);
                    a_SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
                    {
                        @Override
                        public void onStart(String utteranceId)
                        {

                        }

                        @Override
                        public void onDone(String utteranceId)
                        {
                            animButtonToLeft(tempButton);
                        }

                        @Override
                        public void onError(String utteranceId)
                        {

                        }
                    });
                    counterRightAnswer++;
                }
            };
            asyncTask.execute(tableName, wordIndex, wordIndex);
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
                    tempButton.setBackgroundResource(R.drawable.text_button_for_test);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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

    private void animButtonToLeft(final Button button)
    {
        if (button == null) return;
        button.animate().translationX(-button.getWidth()-button.getLeft())
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        DisplayMetrics metrics = new DisplayMetrics();
                        display.getMetrics(metrics);
                        button.setX(metrics.widthPixels+10);
                        int range = 4;
                        if (listFromDB.size() > 0)
                        {
                            controlList.set(indexEn, listFromDB.get(0));
                            button.setText(listFromDB.get(0).get_translate());
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
                            textEn = controlList.get(randomNumber).get_english();
                        }
                        else if (listFromDB.size() == 0 && controlList.size() <= ROWS)
                        {
                            try
                            {
                                controlList.remove(indexEn);
                                button.setText(additionalList.get(additonalCount).get_translate());
                                additonalCount++;
                                textEn = "";
                                if (controlList.size() > 0)
                                {
                                    randomGenerator = new z_RandomNumberGenerator(controlList.size(), range);
                                    int randomNumber = randomGenerator.generate();
                                    textEn = controlList.get(randomNumber).get_english();
                                }
                            } catch (Exception e)
                            {
                                Toast.makeText(getActivity(), "Ошибка - "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        wordIndex++;
                        animButtonFromRigth(button);
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
        button.animate().translationX(0)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {
                        tempButton.setBackgroundResource(R.drawable.text_button_for_test);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        a_SplashScreenActivity.speech.speak(textEn, TextToSpeech.QUEUE_ADD, a_SplashScreenActivity.map);
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
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        if (textEn == "")
                        {
                            ArrayList<String> list = new ArrayList<String>();
                            list.add(testResults.getOverallResult(counterRightAnswer, wordsCount));
                            list.add(counterRightAnswer + getActivity().getString(R.string.text_out_of) + wordsCount);
                            Bundle bundle = new Bundle();
                            bundle.putString(dialogTestComplete.KEY_RESULT, list.get(0));
                            bundle.putString(dialogTestComplete.KEY_ERRORS, list.get(1));
                            dialogTestComplete.setArguments(bundle);
                            dialogTestComplete.setCancelable(false);
                            dialogTestComplete.show(getFragmentManager(), "dialog_complete_lexicon");
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
                    t_ListenEndClickFragment.isOpen = true;
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
                    t_ListenEndClickFragment.isOpen = false;
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

    @Override
    public void onPause()
    {
        super.onPause();
        t_Tests.bundleListenTest = new Bundle();
        onSaveInstanceState(t_Tests.bundleListenTest);
    }


}
