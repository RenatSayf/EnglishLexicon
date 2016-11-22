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

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class t_ListenEndClickFragment extends Fragment
{
    public static final int ROWS = 5;

    private static RelativeLayout.LayoutParams saveTopPanelParams;
    private static boolean isOpen = false;
    private static int spinnSelectedIndex = -1;
    private static ArrayList<String> storedListDict = new ArrayList<>();
    private static z_RandomNumberGenerator randomGenerator;
    private static ArrayList<DataBaseEntry> controlList;
    private static ArrayList<DataBaseEntry> additionalList;
    private static int controlListSize = 0;
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
    private ImageButton buttonSpeech;
    private ProgressBar progressBar;

    private z_LockOrientation lockOrientation;
    private int btn_position;
    private String KEY_BTN_POSITION = "btn_position";
    private int width;
    private int height;
    private int delta = 30;
    private long duration = 1000;
    private ViewPropertyAnimator animToLeft, animToRight, animToTop, animToDown;
    private int wordIndex = 1;
    private int guessedWordsCount = 0;
    private String spinnSelectedItem;
    private int wordsCount;
    private int wordsResidue;

    private int range = 4;

    public t_ListenEndClickFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        if (savedInstanceState == null && t_Tests.bundleListenTest.containsKey(KEY_BTN_POSITION))
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


        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();

        if (savedInstanceState != null && isOpen)
        {
            topPanel.setLayoutParams(saveTopPanelParams);
        }

        if (savedInstanceState != null)
        {
            for (int i = 0; i < buttonsLayout.getChildCount(); i++)
            {
                Button button = (Button) buttonsLayout.getChildAt(i);
                if (!textArray.get(i).equals(""))
                {
                    button.setText(textArray.get(i));
                    button.setVisibility(View.VISIBLE);
                    btnLeft_OnClick(button, i);
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

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(KEY_BTN_POSITION, btn_position);

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
        lockOrientation.lock();
        btn_position = 0;
        wordIndex = 1;
        guessedWordsCount = 0;
        spinnSelectedItem = spinnListDict.getSelectedItem().toString();
        DataBaseQueries.GetWordsCountAsync getWordsCount = new DataBaseQueries.GetWordsCountAsync()
        {
            @Override
            public void resultAsyncTask(int res)
            {
                fillLayoutLeft();
                spinnSelectedIndex = position;
                progressBar.setMax(res);
                progressBar.setProgress(0);
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
                    btnLeft_OnClick(button, i);
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

    private void btnLeft_OnClick(final View view, final int index)
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
                btn_position = index;
                width = view.getWidth();
                height = view.getHeight();

                //Toast.makeText(getActivity(), "Клик по кнопке "+index, Toast.LENGTH_SHORT).show();
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


        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
                        //button.setText(listFromDB.get(0).get_translate());

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

                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {

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

    @Override
    public void onPause()
    {
        super.onPause();
        t_Tests.bundleListenTest = new Bundle();
        onSaveInstanceState(t_Tests.bundleListenTest);
    }
}
