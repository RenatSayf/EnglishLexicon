package com.myapp.lexicon.wordstests;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
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
public class OneOfFiveTest extends Fragment implements DialogTestComplete.IDialogComplete_Result
{
    public static final String TAG = "one_of_five_fragment";
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

    private static RandomNumberGenerator randomGenerator;
    private DisplayMetrics displayMetrics;
    private int delta = 60;

    private TextView textView;
    private LinearLayout buttonsLayout;
    private Spinner spinnListDict;
    private ProgressBar progressBar;
    private Button tempButton;
    private TextView tvProgressValue;
    private LockOrientation lockOrientation;
    private TestResults testResults;
    private DialogTestComplete dialogTestComplete;
    private AppSettings appSettings;
    private AppData appData;
    private Fields fields;

    private final String KEY_TEXT = "key_text";
    private final String KEY_PROGRESS = "key_progress";
    private final String KEY_PROGRESS_MAX = "key_progress_max";
    private final String KEY_FIELDS = "key_fields";

    private FragmentManager fragmentManager;

    public OneOfFiveTest()
    {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TEXT, textView.getText().toString());
        outState.putInt(KEY_PROGRESS_MAX, progressBar.getMax());
        outState.putInt(KEY_PROGRESS, progressBar.getProgress());
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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);

        if (savedInstanceState == null)
        {
            fields = new Fields();
        }

        if (savedInstanceState != null)
        {
            fields = savedInstanceState.getParcelable(KEY_FIELDS);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        fragmentManager = getFragmentManager();
        if (getActivity() != null)
        {
            lockOrientation = new LockOrientation(getActivity());
            testResults = new TestResults(getActivity());
            appSettings = new AppSettings(getActivity());

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
        }
        appData = AppData.getInstance();

        View fragment_view = inflater.inflate(R.layout.t_one_of_five_test, container, false);

        imageBack = fragment_view.findViewById(R.id.img_back_1of5_layout);

        topPanel = fragment_view.findViewById(R.id.top_panel);
        topPanelParams = (RelativeLayout.LayoutParams) topPanel.getLayoutParams();
        TextView headerTopPanel = fragment_view.findViewById(R.id.header_top_panel);
        headerTopPanel.setText(R.string.text_choose_correctly);
        topPanelButtonOK = fragment_view.findViewById(R.id.btn_ok);
        topPanelButtonFinish = fragment_view.findViewById(R.id.btn_complete);
        topPanelButtonThreePoints = fragment_view.findViewById(R.id.btn_more_horiz);
        topPanelButtons_OnClick();

        spinnListDict= fragment_view.findViewById(R.id.spinner_dict);
        buttonsLayout = fragment_view.findViewById(R.id.layout_1of5);
        textView = fragment_view.findViewById(R.id.text_view_1of5);
        progressBar = fragment_view.findViewById(R.id.progress_test1of5);
        tvProgressValue = fragment_view.findViewById(R.id.tv_progress_value);
        setProgressValue(0, fields.wordsCount);

        dialogTestComplete = new DialogTestComplete();
        dialogTestComplete.setIDialogCompleteResult(OneOfFiveTest.this);

        if (savedInstanceState == null)
        {
            buttonsLeftGone();
        }

        if (savedInstanceState != null)
        {
            textView.setText(savedInstanceState.getString(KEY_TEXT));
            progressBar.setMax(savedInstanceState.getInt(KEY_PROGRESS_MAX));
            progressBar.setProgress(savedInstanceState.getInt(KEY_PROGRESS));
            setProgressValue(progressBar.getProgress(), progressBar.getMax());

            for (int i = 0; i < buttonsLayout.getChildCount(); i++)
            {
                Button button = (Button) buttonsLayout.getChildAt(i);
                try
                {
                    button.setText(fields.textArray.get(i));
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

                topPanelVisible(touchDown, touchUp, fields.isOpen[0]);

                return true;
            }
        });
        if (savedInstanceState != null && fields.isOpen[0])
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
            button.setTranslationY(((float) displayMetrics.widthPixels));
            button.setText(null);
            button.setVisibility(View.GONE);
        }
    }

    private void setItemsToSpinnListDict()
    {
        if (fields != null && fields.storedListDict.size() > 0 && getActivity() != null)
        {
            ArrayAdapter<String> spinnAdapter = new ArrayAdapter<>(getActivity(), R.layout.my_content_spinner_layout, fields.storedListDict);
            spinnListDict.setAdapter(spinnAdapter);
            return;
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
                fillLayoutLeft(fields.wordsCount);
                progressBar.setMax(fields.wordsCount);
                progressBar.setProgress(fields.wordIndex - 1);
                setProgressValue(progressBar.getProgress(), progressBar.getMax());
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

        GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(getActivity(), fields.spinnSelectedItem, fields.wordIndex, fields.wordIndex + count - 1, new GetEntriesFromDbAsync.GetEntriesListener()
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
                    button.setTranslationX(displayMetrics.widthPixels);
                    button.setTranslationY(0);
                    button.setVisibility(View.VISIBLE);
                    button.animate().translationX(0).setDuration(duration).setInterpolator(new AnticipateOvershootInterpolator()).setListener(null).setStartDelay(start_delay);
                    start_delay += 70;
                    btn_OnClick(button);
                    button.setText(fields.controlList.get(randomGenerator.generate()).getTranslate());
                    fields.wordIndex++;
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
                if (fields.isStartAnim[0])
                {
                    return;
                }
                tempButton = (Button) view;
                fields.tempButtonId = tempButton.getId();
                fields.buttonY = tempButton.getY();
                fields.buttonX = tempButton.getX();
                compareWords(fields.spinnSelectedItem,textView.getText().toString(), tempButton.getText().toString());
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
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "one_of_five_fragm");
                    try
                    {
                        SplashScreenActivity.speech.setLanguage(Locale.US);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            SplashScreenActivity.speech.speak(textView.getText().toString(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        } else
                        {
                            SplashScreenActivity.speech.speak(textView.getText().toString(), TextToSpeech.QUEUE_ADD, hashMap);
                        }
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    textViewToLeftAnimatoin();
                    buttonToRightAnimation(tempButton);
                    tempButton.setBackgroundResource(R.drawable.text_btn_for_test_green);
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
            Animation animNotRight = null;
            if (getActivity() != null)
            {
                animNotRight = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.anim_not_right);
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
            }

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
                        fields.isStartAnim[0] = true;
                    }

                    @Override
                    public void onAnimationEnd(android.animation.Animator animation)
                    {
                        if (fields.listFromDB.size() > 0)
                        {
                            fields.controlList.set(fields.indexEn, fields.listFromDB.get(0));
                            tempButton.setText(fields.listFromDB.get(0).getTranslate());
                            tempButton.setBackgroundResource(R.drawable.text_button_for_test);
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
                            textView.setText(fields.controlList.get(randomNumber).getEnglish());
                        }
                        else if (fields.controlList.size() <= ROWS)
                        {
                            tempButton.setBackgroundResource(R.drawable.text_button_for_test);
                            try
                            {
                                fields.controlList.remove(fields.indexEn);
                                tempButton.setText(fields.additionalList.get(fields.additonalCount).getTranslate());
                                fields.additonalCount++;
                                textView.setText("");
                                if (fields.controlList.size() > 0)
                                {
                                    randomGenerator = new RandomNumberGenerator(fields.controlList.size(), (int) new Date().getTime());
                                    int randomNumber = randomGenerator.generate();
                                    textView.setText(fields.controlList.get(randomNumber).getEnglish());
                                }
                            } catch (Exception e)
                            {
                                Toast.makeText(getActivity(), "Error - "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (fields.buttonY > 0)
                        {
                            buttonsToDownAnimation(fields.buttonX, fields.buttonY);
                        } else
                        {
                            textViewToRightAnimation();
                        }
                        fields.wordIndex++;
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
                        fields.isStartAnim[0] = false;
                        if (textView.getText().toString().equals(""))
                        {
                            //Toast.makeText(activity,"Тест завершен",Toast.LENGTH_SHORT).show();
                            ArrayList<String> list = new ArrayList<>();
                            if (getActivity() != null)
                            {
                                list.add(testResults.getOverallResult(fields.counterRightAnswer, fields.wordsCount));
                                list.add(fields.counterRightAnswer + getActivity().getString(R.string.text_out_of) + fields.wordsCount);
                            } else
                            {
                                list.add(testResults.getOverallResult(fields.counterRightAnswer, fields.wordsCount));
                                list.add(fields.counterRightAnswer + " / " + fields.wordsCount);
                            }
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

            if (getActivity() != null)
            {
                spinnListDict.setSelection(-1);
                fields.spinnSelectedIndex = -1;
                getActivity().onBackPressed();
                if (fields.arrStudiedDict.size() > 0)
                {
                    DialogChangePlayList dialogChangePlayList = new DialogChangePlayList();
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(dialogChangePlayList.KEY_LIST_DICT, fields.arrStudiedDict);
                    dialogChangePlayList.setArguments(bundle);
                    dialogChangePlayList.setCancelable(false);
                    if (getFragmentManager() != null)
                    {
                        dialogChangePlayList.show(getFragmentManager(), "dialog_change_pl_lexicon");
                    }
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

    @Override
    public void onDetach()
    {
        super.onDetach();
        if (isRemoving() && fields.wordIndex -1 - ROWS < fields.wordsCount && fields.counterRightAnswer > 1 && fields.wordsCount >= ROWS && getActivity() != null)
        {
            fields.spinnSelectedIndex = -1;
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

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        RandomNumberGenerator generator = new RandomNumberGenerator(BackgroundFragm.imagesId.length, (int) new Date().getTime());
        imageBack.setImageResource(BackgroundFragm.imagesId[generator.generate()]);
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
                    fields.isOpen[0] = true;
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
                    fields.isOpen[0] = false;
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


}
