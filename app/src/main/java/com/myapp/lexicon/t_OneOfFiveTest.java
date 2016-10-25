package com.myapp.lexicon;


import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class t_OneOfFiveTest extends Fragment implements t_Animator.ITextViewToLeftListener, t_Animator.ITextViewToRightListener, t_DialogTestComplete.IDialogComplete_Result
{
    public static final int ROWS = 5;

    private static Button[] buttonsArray;
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
    private int counterRightAnswer = 0;
    private t_TestResults testResults;
    private t_DialogTestComplete dialogTestComplete;

    private String KEY_BUTTON_ID = "key_button_id";
    private String KEY_TEXT = "key_text";
    private String KEY_CONTROL_LIST_SIZE = "key_control_list_size";
    private String KEY_WORDS_COUNT = "key_words_count";
    private String KEY_SPINN_SELECT_ITEM = "key_spinn_select_item";
    private String KEY_SPINN_SELECT_INDEX = "key_spinn_select_index";
    private String KEY_PROGRESS = "key_progress";
    private String KEY_PROGRESS_MAX = "key_progress_max";
    private String KEY_COUNTER_RIGHT_ANSWER = "key_counter_right";

    FragmentActivity activity;
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
        outState.putInt(KEY_COUNTER_RIGHT_ANSWER, counterRightAnswer);
        saveButtonsLayoutState();
        super.onSaveInstanceState(outState);
    }

    private void saveButtonsLayoutState()
    {
        int childCount = buttonsLayout.getChildCount();
        buttonsArray = new Button[childCount];
        for (int i = 0; i < childCount; i++)
        {
            buttonsArray[i] = (Button) buttonsLayout.getChildAt(i);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        activity = getActivity();
        lockOrientation = new z_LockOrientation(activity);
        testResults = new t_TestResults(activity);
        View fragment_view = inflater.inflate(R.layout.t_one_of_five_test, container, false);
        spinnListDict= (Spinner) fragment_view.findViewById(R.id.spinn_1of5);
        buttonsLayout = (LinearLayout) fragment_view.findViewById(R.id.layout_1of5);
        textView = (TextView) fragment_view.findViewById(R.id.text_view_1of5);
        progressBar = (ProgressBar) fragment_view.findViewById(R.id.progress_test1of5);
        animator = t_Animator.getInstance();

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
            counterRightAnswer = savedInstanceState.getInt(KEY_COUNTER_RIGHT_ANSWER);
        }

        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();

        if (buttonsArray != null)
        {
            for (int i = 0; i < buttonsArray.length; i++)
            {
                Button button = (Button) buttonsLayout.getChildAt(i);
                button.setText(buttonsArray[i].getText());
                button.setTranslationX(buttonsArray[i].getTranslationX());
                button.setTranslationY(buttonsArray[i].getTranslationY());
                button.setVisibility(buttonsArray[i].getVisibility());
                btnLeft_OnClick(button);
            }
            animator.setLayout(buttonsLayout, textView);
        }

        return fragment_view;
    }

    private void buttonsLeftGone()
    {
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            button.setTranslationX(0);
            button.setTranslationY(0);
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
                startTest(position);

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
                buttonsArray = new Button[list.size()];
                controlList = list;
                additionalList = new ArrayList<>();
                additonalCount = 0;
                for (DataBaseEntry entry : list)
                {
                    additionalList.add(entry);
                }
                controlListSize = controlList.size();
                randomGenerator = new z_RandomNumberGenerator(controlListSize, 133);
                for (int i = 0; i < controlList.size(); i++)
                {
                    Button button = (Button) buttonsLayout.getChildAt(i);
                    button.setVisibility(View.VISIBLE);
                    button.setTranslationX(0);
                    button.setTranslationY(0);
                    if (button.getVisibility() == View.VISIBLE)
                    {
                        buttonsArray[i] = button;
                        btnLeft_OnClick(button);
                    }

                    button.setText(controlList.get(i).get_translate());
                    wordIndex++;
                }
                int randIndex = randomGenerator.generate();
                textView.setText(list.get(randIndex).get_english());
                textView.setTranslationX(0);
                textView.setTranslationY(0);
            }
        };
        asyncTask.execute(spinnSelectedItem, wordIndex, count);
        asyncTask = null;
    }

    private void btnLeft_OnClick(final View view)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
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
                        animator.textViewToLeft();
                        animator.buttonToRight(buttonsLayout, tempButtonId);
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
            list.add(counterRightAnswer + getString(R.string.text_out_of) + wordsCount);
            dialogTestComplete = new t_DialogTestComplete();
            dialogTestComplete.setIDialogCompleteResult(t_OneOfFiveTest.this);
            Bundle bundle = new Bundle();
            bundle.putString(dialogTestComplete.KEY_RESULT, list.get(0));
            bundle.putString(dialogTestComplete.KEY_ERRORS, list.get(1));
            dialogTestComplete.setArguments(bundle);
            dialogTestComplete.setCancelable(false);
            dialogTestComplete.show(getFragmentManager(), "dialog_complete_lexicon");
        }
    }




    @Override
    public void dialogCompleteResult(int res)
    {

    }
}
