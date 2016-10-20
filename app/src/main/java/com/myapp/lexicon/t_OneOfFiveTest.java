package com.myapp.lexicon;


import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class t_OneOfFiveTest extends Fragment implements t_Animator.ITextViewToLeftListener, t_Animator.ITextViewToRightListener
{
    public static final int ROWS = 5;
    private static Button[] buttonsArray;
    private static String mainWord;
    private static ArrayList<String> storedListDict = new ArrayList<>();
    private TextView textView;
    private LinearLayout buttonsLayout;
    private Spinner spinnListDict;
    private static int spinnSelectedIndex = -1;
    private static int btn_position;
    private static int wordIndex = 1;
    private static int guessedWordsCount = 0;
    private static String spinnSelectedItem;
    private static int wordsCount;
    private static int wordsResidue;
    private t_Animator animator;
    private z_LockOrientation lockOrientation;

    private String KEY_BUTTON_ID = "key_button_id";
    private String KEY_BUTTON_X = "key_button_x";
    private String KEY_BUTTON_Y = "key_button_y";

    FragmentActivity activity;
    public t_OneOfFiveTest()
    {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
//        outState.putInt(KEY_BUTTON_ID, buttonId);
//        outState.putFloat(KEY_BUTTON_X, buttonX);
//        outState.putFloat(KEY_BUTTON_Y, buttonY);
        mainWord = textView.getText().toString();
        saveButtonsLayoutState();
        super.onSaveInstanceState(outState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment

        activity = getActivity();
        lockOrientation = new z_LockOrientation(activity);
        View fragment_view = inflater.inflate(R.layout.t_one_of_five_test, container, false);
        spinnListDict= (Spinner) fragment_view.findViewById(R.id.spinn_1of5);
        buttonsLayout = (LinearLayout) fragment_view.findViewById(R.id.layout_1of5);
        textView = (TextView) fragment_view.findViewById(R.id.text_view_1of5);
        animator = t_Animator.getInstance();

        if (savedInstanceState != null)
        {
            //buttonId = savedInstanceState.getInt(KEY_BUTTON_ID);
//            buttonX = savedInstanceState.getFloat(KEY_BUTTON_X);
//            buttonY = savedInstanceState.getFloat(KEY_BUTTON_Y);
        }

        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();

        if (buttonsArray != null)
        {
            textView.setText(mainWord);
            //buttonsLeftGone();
            for (int i = 0; i < buttonsArray.length; i++)
            {
                Button button = (Button) buttonsLayout.getChildAt(i);
                //button.setId(10+i);
                button.setText(buttonsArray[i].getText());
                button.setTranslationX(buttonsArray[i].getTranslationX());
                button.setTranslationY(buttonsArray[i].getTranslationY());
                button.setVisibility(buttonsArray[i].getVisibility());
                //buttonsArray[i] = button;
                btnLeft_OnClick(button, i);
            }
            animator.setLayout(buttonsLayout, textView);
//            animator.setTextViewToLeftListener(t_OneOfFiveTest.this);
//            animator.setTextViewToRightListener(t_OneOfFiveTest.this);
        }

        return fragment_view;
    }

    private void buttonsLeftGone()
    {
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            //button.setId(10+i);
            button.setTranslationX(0);
            button.setTranslationY(0);
            button.setVisibility(View.GONE);
        }
        //animator = t_Animator.getInstance();
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
        guessedWordsCount = 0;
        spinnSelectedItem = spinnListDict.getSelectedItem().toString();
        DataBaseQueries.GetWordsCountAsync getWordsCount = new DataBaseQueries.GetWordsCountAsync()
        {
            @Override
            public void resultAsyncTask(int res)
            {
                wordsCount = res;
                wordsResidue = wordsCount - ROWS;
                fillLayoutLeft(wordsCount);
                spinnSelectedIndex = position;
            }
        };
        getWordsCount.execute(spinnSelectedItem);
    }

    private static ArrayList<DataBaseEntry> controlList;

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
                        btnLeft_OnClick(button, i);
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
    private static int controlListSize = 0;
    private static float buttonY;
    private static float buttonX;
    private Button button;
    private static int buttonId;
    private void btnLeft_OnClick(final View view, final int index)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                button = (Button) view;
                buttonId = button.getId();
                buttonY = button.getY();
                buttonX = button.getX();
                btn_position = index;
                //Toast.makeText(getActivity(), "Клик по кнопке "+index, Toast.LENGTH_SHORT).show();
                compareWords(spinnSelectedItem,textView.getText().toString(),button.getText().toString());
            }
        });
    }

    private static z_RandomNumberGenerator randomGenerator;
    private int range = 127;
    private static ArrayList<DataBaseEntry> listFromDB;
    private static int indexEn = -1;
    private static int indexRu = -1;
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
                //Toast.makeText(getActivity(), "Правильно", Toast.LENGTH_SHORT).show();
                AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
                {
                    @Override
                    public void resultAsyncTask(ArrayList<DataBaseEntry> list)
                    {
                        listFromDB = list;
                        animator.textViewToLeft();
                        animator.buttonToRight(buttonsLayout, buttonId);
                    }
                };
                asyncTask.execute(tableName, wordIndex, wordIndex);
            }
            else
            {
                Toast.makeText(getActivity(), "Неправильно", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void textViewToLeftListener(int result, TextView textView, Button button)
    {
        if (listFromDB.size() > 0)
        {
            controlList.set(indexEn, listFromDB.get(0));
            //buttonsArray[btn_position].setText(listFromDB.get(0).get_translate());
            //button = (Button) buttonsLayout.findViewById(buttonId);
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
            textView.setText("");
            if (controlList.size() > 0)
            {
                randomGenerator = new z_RandomNumberGenerator(controlList.size(), range);
                int randomNumber = randomGenerator.generate();
                textView.setText(controlList.get(randomNumber).get_english());
            }
            button.setVisibility(View.INVISIBLE);
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
    public void textViewToRightListener(int result)
    {
        saveButtonsLayoutState();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    }

    private void saveButtonsLayoutState()
    {
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            if (buttonsLayout.getChildAt(i).getVisibility() == View.VISIBLE)
            {
                buttonsArray[i] = (Button) buttonsLayout.getChildAt(i);
            }
        }
    }



}
