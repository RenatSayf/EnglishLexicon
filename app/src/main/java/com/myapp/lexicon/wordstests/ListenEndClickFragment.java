package com.myapp.lexicon.wordstests;


import android.animation.Animator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

    private static RandomNumberGenerator randomGenerator;
    private LockOrientation lockOrientation;
    private long duration = 1000;
    private DialogTestComplete dialogTestComplete;
    private ArrayList<String> arrStudiedDict = new ArrayList<>();
    private TestResults testResults;
    private DisplayMetrics displayMetrics;
    private AppSettings appSettings;
    private AppData2 appData;
    private Fields fields;

    private final String KEY_FIELDS = "key_fields";
    private final String KEY_PROGRESS = "key_progress";
    private final String KEY_PROGRESS_MAX = "key_progress_max";


    public ListenEndClickFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
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
        for (String item : stringCollection)
        {
            fields.textArray.add(item);
        }
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        lockOrientation = new LockOrientation(getActivity());
        appSettings = new AppSettings(getActivity());
        appData = AppData2.getInstance();

        View fragment_view = inflater.inflate(R.layout.t_listen_end_click_layout, container, false);

        backImage = (ImageView) fragment_view.findViewById(R.id.img_back_listen_layout);
        topPanel = (LinearLayout) fragment_view.findViewById(R.id.top_panel);
        topPanelParams = (RelativeLayout.LayoutParams) topPanel.getLayoutParams();
        LinearLayout linLayout = (LinearLayout) fragment_view.findViewById(R.id.linear_layout);
        spinnListDict = (Spinner) fragment_view.findViewById(R.id.spinner_dict);
        buttonsLayout = (LinearLayout) fragment_view.findViewById(R.id.buttons_layout);
        buttonSpeech = (ImageButton) fragment_view.findViewById(R.id.btn_speech);
        buttonSpeech_OnClick();
        progressBar = (ProgressBar) fragment_view.findViewById(R.id.prog_bar_listen);
        topPanelButtonOK = (Button) fragment_view.findViewById(R.id.btn_ok);
        topPanelButtonFinish = (Button) fragment_view.findViewById(R.id.btn_complete);
        topPanelButtonThreePoints = (ImageButton) fragment_view.findViewById(R.id.btn_more_horiz);
        topPanelButtons_OnClick();

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
            ArrayAdapter<String> spinnAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.my_content_spinner_layout, fields.storedListDict);
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

        GetCountWordsAsync getCountWordsAsync = new GetCountWordsAsync(getActivity(), fields.spinnSelectedItem, new GetCountWordsAsync.GetCountListener()
        {
            @Override
            public void onTaskComplete(int count)
            {
                fields.wordsCount = count;
                fillLayoutLeft();
                progressBar.setMax(count);
                progressBar.setProgress(0);
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
                for (DataBaseEntry entry : entries)
                {
                    fields.additionalList.add(entry);
                }
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
                compareWords(fields.spinnSelectedItem, fields.textEn, tempButton.getText().toString());
            }
        });
    }

    private void compareWords(String tableName, String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;

        for (int i = 0; i < fields.controlList.size(); i++)
        {
            if (fields.controlList.get(i).getEnglish().equals(enword))
            {
                fields.indexEn = i;
            }
            if (fields.controlList.get(i).getTranslate().equals(ruword))
            {
                fields.indexRu = i;
            }
        }

        if (fields.indexEn == fields.indexRu && fields.indexEn != -1 && fields.indexRu != -1)
        {

            GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), tableName, fields.wordIndex, fields.wordIndex, new GetEntriesFromDbAsync.GetEntriesListener()
            {
                @Override
                public void getEntriesListener(ArrayList<DataBaseEntry> entries)
                {
                    fields.listFromDB = entries;
                    progressBar.setProgress(progressBar.getProgress()+1);
                    if (tempButton != null)
                    {
                        tempButton.setText(fields.textEn);
                        tempButton.setBackgroundResource(R.drawable.text_btn_for_test_green);
                    }
                    final HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "compare_words");
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
                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        DisplayMetrics metrics = new DisplayMetrics();
                        display.getMetrics(metrics);
                        button.setX(metrics.widthPixels+10);
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
                        else if (fields.listFromDB.size() == 0 && fields.controlList.size() <= ROWS)
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
                                Toast.makeText(getActivity(), "Ошибка - "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                fields.isStartAnim[0] = false;
                if (fields.textEn.equals(""))
                {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(testResults.getOverallResult(fields.counterRightAnswer, fields.wordsCount));
                    list.add(fields.counterRightAnswer + getActivity().getString(R.string.text_out_of) + fields.wordsCount);
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
                getActivity().onBackPressed();
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
//        Tests.bundleListenTest = new Bundle();
//        onSaveInstanceState(Tests.bundleListenTest);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        RandomNumberGenerator generator = new RandomNumberGenerator(BackgroundFragm.imagesId.length, (int) new Date().getTime());
        backImage.setImageResource(BackgroundFragm.imagesId[generator.generate()]);
    }


}
