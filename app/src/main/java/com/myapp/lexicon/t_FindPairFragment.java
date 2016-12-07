package com.myapp.lexicon;


import android.animation.Animator;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class t_FindPairFragment extends Fragment
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
    private Button buttonWord;

    private Spinner spinnListDict;
    private int spinnSelectedIndex = -1;
    private String spinnSelectedItem;
    private static ArrayList<String> storedListDict = new ArrayList<>();

    private ProgressBar progressBar;

    private z_LockOrientation lockOrientation;
    private int wordIndex = 1;
    private int wordsCount;
    private static int counterRightAnswer = 0;
    private static ArrayList<String> textArrayleft = new ArrayList<>();
    private static ArrayList<String> textArrayRight = new ArrayList<>();
    private static ArrayList<DataBaseEntry> controlList;
    private static ArrayList<DataBaseEntry> additionalList;
    private static int additonalCount = 0;
    private int controlListSize = 0;
    private static z_RandomNumberGenerator randGenLeft;
    private static z_RandomNumberGenerator randGenRight;
    private int rangeLeft = 1;
    private int rangeRight = 4;
    DisplayMetrics metrics;
    private static int indexEn = -1;
    private static int indexRu = -1;
    private static ArrayList<DataBaseEntry> listFromDB;
    private String enWord = null;
    private String ruWord = null;
    private Button tempButtonLeft;
    private Button tempButtonRight;

    private String KEY_CONTROL_LIST_SIZE = "key_control_list_size";
    private String KEY_WORDS_COUNT = "key_words_count";
    private String KEY_SPINN_SELECT_ITEM = "key_spinn_select_item";
    private String KEY_SPINN_SELECT_INDEX = "key_spinn_select_index";
    private String KEY_PROGRESS = "key_progress";
    private String KEY_PROGRESS_MAX = "key_progress_max";
    private String KEY_WORD_INDEX = "key_word_index";


    public t_FindPairFragment()
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
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        lockOrientation = new z_LockOrientation(getActivity());
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
            if (textArrayleft.size() == btnLayoutLeft.getChildCount() && textArrayRight.size() == btnLayoutRight.getChildCount())
            {
                for (int i = 0; i < btnLayoutLeft.getChildCount() && i < btnLayoutRight.getChildCount(); i++)
                {
                    Button buttonLeft = (Button) btnLayoutLeft.getChildAt(i);
                    Button buttonRight = (Button) btnLayoutRight.getChildAt(i);
                    buttonLeft.setText(textArrayleft.get(i));
                    buttonRight.setText(textArrayRight.get(i));
                    btnLeft_OnClick(buttonLeft);
                    btnRight_OnClick(buttonRight);
                }
            }
        }

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
//                textArray.clear();
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
        wordIndex = 1;
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
                //hideWordButtons();

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
                        randGenLeft = new z_RandomNumberGenerator(controlListSize, rangeLeft);
                        randGenRight = new z_RandomNumberGenerator(controlListSize, rangeRight);
                        for (int i = 0; i < controlList.size(); i++)
                        {
                            Button btnLeft = (Button) btnLayoutLeft.getChildAt(i);
                            Button btnRight = (Button) btnLayoutRight.getChildAt(i);
                            btnLeft.setText(controlList.get(randGenLeft.generate()).get_english());
                            btnRight.setText(controlList.get(randGenRight.generate()).get_translate());
                            btnLeft.setX(metrics.widthPixels);
                            btnRight.setX(-metrics.widthPixels);
                            btnLeft.setVisibility(View.VISIBLE);
                            btnRight.setVisibility(View.VISIBLE);
                            btnLeft.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator());
                            btnRight.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator());
                            btnLeft_OnClick(btnLeft);
                            btnRight_OnClick(btnRight);
                            wordIndex++;
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
                asyncTask.execute(spinnSelectedItem, wordIndex, ROWS);
                asyncTask = null;
            }
        };
        getWordsCount.execute(spinnSelectedItem);
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
            }
        });
    }

    private void compareWords(String tableName, final String enword, String ruword)
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
                        a_SplashScreenActivity.speech.speak(enword, TextToSpeech.QUEUE_ADD, a_SplashScreenActivity.map);
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
                        ViewPropertyAnimator animScale = tempButtonLeft.animate().scaleX(0).scaleY(0).setDuration(duration).setInterpolator(new AccelerateInterpolator());
                        animScale_Listener(animScale);
                        tempButtonRight.animate().scaleX(0).scaleY(0).setDuration(duration).setInterpolator(new AccelerateInterpolator());
//                        animator.textViewToLeft();
//                        animator.buttonToRight(buttonsLayout, tempButtonId);
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
                        //tempButton.setBackgroundResource(R.drawable.text_button_for_test);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {

                    }
                });
//                if (tempButton != null)
//                {
//                    tempButton.setBackgroundResource(R.drawable.text_btn_for_test_red);
//                    tempButton.startAnimation(animNotRight);
//                }
            }
        }
    }

    private void animScale_Listener(ViewPropertyAnimator animScale)
    {
        if (animScale == null) return;
        animScale.setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                if (tempButtonLeft != null && tempButtonRight != null)
                {
                    tempButtonLeft.setX(-metrics.widthPixels);
                    tempButtonRight.setX(metrics.widthPixels);
                    tempButtonLeft.setScaleX(1.0f);
                    tempButtonLeft.setScaleY(1.0f);
                    tempButtonRight.setScaleX(1.0f);
                    tempButtonRight.setScaleY(1.0f);
                    tempButtonLeft.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null);
                    tempButtonRight.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null);
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

    private void hideWordButtons()
    {
        for (int i = 0; i < btnLayoutLeft.getChildCount(); i++)
        {
            Button btnLeft = (Button) btnLayoutLeft.getChildAt(i);
            Button btnRight = (Button) btnLayoutRight.getChildAt(i);
            btnLeft.setVisibility(View.GONE);
            btnRight.setVisibility(View.GONE);
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
                    t_FindPairFragment.isOpen = true;
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
                    t_FindPairFragment.isOpen = false;
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
