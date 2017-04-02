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
import com.myapp.lexicon.main.BackgroundAnim2;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.settings.AppData2;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class OneOfFiveTest extends Fragment implements DialogTestComplete.IDialogComplete_Result
{
    public static final int ROWS = 5;

    private static RelativeLayout.LayoutParams saveTopPanelParams;

    private ImageView imageBack;

    private LinearLayout topPanel;
    private RelativeLayout.LayoutParams topPanelParams;
    private float touchDown = 0, touchUp = 0;
    private Button topPanelButtonOK;
    private Button topPanelButtonFinish;
    private ImageButton topPanelButtonThreePoints;
    private long duration = 1000;
    private static boolean isOpen = false;

    private static ArrayList<String> storedListDict = new ArrayList<>();
    private static ArrayList<DataBaseEntry> controlList;
    private static ArrayList<DataBaseEntry> additionalList;
    private static int additonalCount = 0;
    private static int wordIndex = 1;
    private static float buttonY;
    private static float buttonX;
    private static RandomNumberGenerator randomGenerator;
    private static ArrayList<DataBaseEntry> listFromDB;
    private static int indexEn = -1;
    private static int indexRu = -1;
    private static ArrayList<String> textArray = new ArrayList<>();
    private DisplayMetrics displayMetrics;
    private int delta = 60;
    private static boolean isStartAnim = false;

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
    private LockOrientation lockOrientation;
    private static int counterRightAnswer = 0;
    private TestResults testResults;
    private DialogTestComplete dialogTestComplete;
    private ArrayList<String> arrStudiedDict = new ArrayList<>();
    private AppSettings appSettings;
    private AppData2 appData;

    private String KEY_BUTTON_ID = "key_button_id";
    private String KEY_TEXT = "key_text";
    private String KEY_CONTROL_LIST_SIZE = "key_control_list_size";
    private String KEY_WORDS_COUNT = "key_words_count";
    private String KEY_SPINN_SELECT_ITEM = "key_spinn_select_item";
    private String KEY_SPINN_SELECT_INDEX = "key_spinn_select_index";
    private String KEY_PROGRESS = "key_progress";
    private String KEY_PROGRESS_MAX = "key_progress_max";
    //private String KEY_COUNTER_RIGHT_ANSWER = "key_counter_right";

    static FragmentManager fragmentManager;
    public OneOfFiveTest()
    {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
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
        textArray.clear();
        for (String item : stringCollection)
        {
            textArray.add(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        if (savedInstanceState == null && Tests.bundleOneOfFiveTest.containsKey(KEY_TEXT))
        {
            savedInstanceState = Tests.bundleOneOfFiveTest;
        }
        fragmentManager = getFragmentManager();
        lockOrientation = new LockOrientation(getActivity());
        testResults = new TestResults(getActivity());
        appSettings = new AppSettings(getActivity());
        appData = AppData2.getInstance();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        View fragment_view = inflater.inflate(R.layout.t_one_of_five_test, container, false);

        imageBack = (ImageView) fragment_view.findViewById(R.id.img_back_1of5_layout);

        topPanel = (LinearLayout) fragment_view.findViewById(R.id.top_panel);
        topPanelParams = (RelativeLayout.LayoutParams) topPanel.getLayoutParams();
        TextView headerTopPanel = (TextView) fragment_view.findViewById(R.id.header_top_panel);
        headerTopPanel.setText(R.string.text_choose_correctly);
        topPanelButtonOK = (Button) fragment_view.findViewById(R.id.btn_ok);
        topPanelButtonFinish = (Button) fragment_view.findViewById(R.id.btn_complete);
        topPanelButtonThreePoints = (ImageButton) fragment_view.findViewById(R.id.btn_more_horiz);
        topPanelButtons_OnClick();

        spinnListDict= (Spinner) fragment_view.findViewById(R.id.spinner_dict);
        buttonsLayout = (LinearLayout) fragment_view.findViewById(R.id.layout_1of5);
        textView = (TextView) fragment_view.findViewById(R.id.text_view_1of5);
        progressBar = (ProgressBar) fragment_view.findViewById(R.id.progress_test1of5);
        dialogTestComplete = new DialogTestComplete();
        dialogTestComplete.setIDialogCompleteResult(OneOfFiveTest.this);

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
                btn_OnClick(button);
            }
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
                String currentDict = playList.get(appData.getNdict());
                if (arrayList.contains(currentDict))
                {
                    int indexOf = arrayList.indexOf(currentDict);
                    spinnListDict.setSelection(indexOf);
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

        GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), spinnSelectedItem, new GetCountWordsAsync.GetCountListener()
        {
            @Override
            public void onTaskComplete(int count)
            {
                wordsCount = count;
                fillLayoutLeft(wordsCount);
                spinnSelectedIndex = position;
                progressBar.setMax(wordsCount);
                progressBar.setProgress(0);
                counterRightAnswer = 0;
            }
        });
        if (getCountWordsAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getCountWordsAsync.execute();
        }
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

        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), spinnSelectedItem, wordIndex, count, new GetEntriesFromDbAsync.GetEntriesListener()
        {
            @Override
            public void getEntriesListener(ArrayList<DataBaseEntry> entries)
            {
                controlList = entries;
                additionalList = new ArrayList<>();
                additonalCount = 0;
                for (DataBaseEntry entry : entries)
                {
                    additionalList.add(entry);
                }
                controlListSize = controlList.size();
                randomGenerator = new RandomNumberGenerator(controlListSize, (int) new Date().getTime());
                long start_delay = 0;
                for (int i = 0; i < controlList.size(); i++)
                {
                    Button button = (Button) buttonsLayout.getChildAt(i);
                    button.setTranslationX(displayMetrics.widthPixels);
                    button.setTranslationY(0);
                    button.setVisibility(View.VISIBLE);
                    button.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null).setStartDelay(start_delay);
                    start_delay += 70;
                    btn_OnClick(button);
                    button.setText(controlList.get(randomGenerator.generate()).getTranslate());
                    wordIndex++;
                }
                randomGenerator = new RandomNumberGenerator(entries.size(), (int) new Date().getTime());
                textView.setText(entries.get(randomGenerator.generate()).getEnglish());
                textView.setTranslationX(-displayMetrics.widthPixels);
                textView.setTranslationY(0);
                textView.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null).setStartDelay(start_delay);
            }
        });
        if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
        {
            getEntriesFromDbAsync.execute();
        }
    }

    private void btn_OnClick(final View view)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (isStartAnim)
                {
                    return;
                }
                tempButton = (Button) view;
                tempButtonId = tempButton.getId();
                buttonY = tempButton.getY();
                buttonX = tempButton.getX();
                compareWords(spinnSelectedItem,textView.getText().toString(), tempButton.getText().toString());
            }
        });
    }

    private void compareWords(String tableName, String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;

        for (int i = 0; i < controlList.size(); i++)
        {
            if (controlList.get(i).getEnglish().equals(enword))
            {
                indexEn = i;
            }
            if (controlList.get(i).getTranslate().equals(ruword))
            {
                indexRu = i;
            }
        }

        if (indexEn == indexRu && indexEn != -1 && indexRu != -1)
        {
            GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), tableName, wordIndex, wordIndex, new GetEntriesFromDbAsync.GetEntriesListener()
            {
                @Override
                public void getEntriesListener(ArrayList<DataBaseEntry> entries)
                {
                    listFromDB = entries;
                    progressBar.setProgress(progressBar.getProgress()+1);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "one_of_five_fragm");
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
                    {
                        SplashScreenActivity.speech.speak(textView.getText().toString(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                    } else
                    {
                        SplashScreenActivity.speech.speak(textView.getText().toString(), TextToSpeech.QUEUE_ADD, hashMap);
                    }

                    textViewToLeftAnimatoin();
                    buttonToRightAnimation(tempButton);
                    tempButton.setBackgroundResource(R.drawable.text_btn_for_test_green);
                    counterRightAnswer++;
                }
            });
            if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
            {
                getEntriesFromDbAsync.execute();
            }
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

    public void textViewToLeftAnimatoin()
    {
        textView.animate().x(-(displayMetrics.widthPixels + delta))
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(0)
                .setListener(new android.animation.Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(android.animation.Animator animation)
                    {
                        lockOrientation.lock();
                        isStartAnim = true;
                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation)
                    {
                        if (listFromDB.size() > 0)
                        {
                            controlList.set(indexEn, listFromDB.get(0));
                            tempButton.setText(listFromDB.get(0).getTranslate());
                            tempButton.setBackgroundResource(R.drawable.text_button_for_test);
                            if (controlListSize != controlList.size())
                            {
                                randomGenerator = new RandomNumberGenerator(controlList.size(), (int) new Date().getTime());
                                controlListSize = controlList.size();
                            }
                            int randomNumber = randomGenerator.generate();
                            if (randomNumber < 0)
                            {
                                randomGenerator = new RandomNumberGenerator(controlListSize, (int) new Date().getTime());
                                randomNumber = randomGenerator.generate();
                            }
                            textView.setText(controlList.get(randomNumber).getEnglish());
                        }
                        else if (listFromDB.size() == 0 && controlList.size() <= ROWS)
                        {
                            tempButton.setBackgroundResource(R.drawable.text_button_for_test);
                            try
                            {
                                controlList.remove(indexEn);
                                tempButton.setText(additionalList.get(additonalCount).getTranslate());
                                additonalCount++;
                                textView.setText("");
                                if (controlList.size() > 0)
                                {
                                    randomGenerator = new RandomNumberGenerator(controlList.size(), (int) new Date().getTime());
                                    int randomNumber = randomGenerator.generate();
                                    textView.setText(controlList.get(randomNumber).getEnglish());
                                }
                            } catch (Exception e)
                            {
                                Toast.makeText(getActivity(), "Ошибка - "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (buttonY > 0)
                        {
                            buttonsToDownAnimation(buttonX, buttonY);
                        } else
                        {
                            textViewToRightAnimation();
                        }
                        wordIndex++;
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

    public void buttonsToDownAnimation(float x, float y)
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
                            textViewToRightAnimation();
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

    public void textViewToRightAnimation()
    {
        textView.animate().translationX(0)
                .setDuration(duration)
                .setStartDelay(0)
                .setInterpolator(new AnticipateOvershootInterpolator())
                .setListener(new android.animation.Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(android.animation.Animator animation)
                    {
                        if (tempButton != null)
                        {
                            tempButton.setY(0);
                            tempButton.animate().translationXBy(-tempButton.getWidth()-delta)
                                    .setDuration(duration)
                                    .setStartDelay(0)
                                    .setInterpolator(new AnticipateOvershootInterpolator());
                        }
                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation)
                    {
                        lockOrientation.unLock();
                        isStartAnim = false;
                        if (textView.getText().toString().equals(""))
                        {
                            //Toast.makeText(activity,"Тест завершен",Toast.LENGTH_SHORT).show();
                            ArrayList<String> list = new ArrayList<>();
                            list.add(testResults.getOverallResult(counterRightAnswer, wordsCount));
                            list.add(counterRightAnswer + getActivity().getString(R.string.text_out_of) + wordsCount);
                            Bundle bundle = new Bundle();
                            bundle.putString(dialogTestComplete.KEY_RESULT, list.get(0));
                            bundle.putString(dialogTestComplete.KEY_ERRORS, list.get(1));
                            dialogTestComplete.setArguments(bundle);
                            dialogTestComplete.setCancelable(false);
                            dialogTestComplete.show(fragmentManager, "dialog_complete_lexicon");
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

    public void buttonToRightAnimation(View view)
    {
        final Button button = (Button) view;
        if (button != null)
        {
            button.animate().translationXBy((button.getWidth()+delta))
                    .setDuration(duration)
                    .setStartDelay(0)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setListener(new android.animation.Animator.AnimatorListener()
                    {
                        @Override
                        public void onAnimationStart(android.animation.Animator animation)
                        {

                        }

                        @Override
                        public void onAnimationEnd(android.animation.Animator animation)
                        {

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

    private void addToStudiedList()
    {
        boolean containsInPlayList = false;
        for (String item : appSettings.getPlayList())
        {
            if (item.equals(spinnListDict.getSelectedItem()))
            {
                containsInPlayList = true; break;
            }
        }
        boolean contains = arrStudiedDict.contains(spinnListDict.getSelectedItem().toString());
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
        Tests.bundleOneOfFiveTest = new Bundle();
        onSaveInstanceState(Tests.bundleOneOfFiveTest);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        RandomNumberGenerator generator = new RandomNumberGenerator(BackgroundAnim2.imagesId.length, (int) new Date().getTime());
        imageBack.setImageResource(BackgroundAnim2.imagesId[generator.generate()]);
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
                    OneOfFiveTest.isOpen = true;
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
                    OneOfFiveTest.isOpen = false;
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
