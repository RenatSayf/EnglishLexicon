package com.myapp.lexicon;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
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
public class t_OneOfFiveTest extends Fragment implements t_Animator.ITextViewToleftEnd,
        t_Animator.IButtonsToDownEnd, t_Animator.IButtonToTopListener, t_Animator.IButtonToLeftListener
{
    public static final int ROWS = 5;
    private static Button[] buttonsArray;
    private static TextView[] textViewArray;
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
    private ViewPropertyAnimator animToLeft, animToRight, animToTop, animToDown;
    private t_Animator animator;

    public t_OneOfFiveTest()
    {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        mainWord = textView.getText().toString();
        super.onSaveInstanceState(outState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragment_view = inflater.inflate(R.layout.t_one_of_five_test, container, false);
        spinnListDict= (Spinner) fragment_view.findViewById(R.id.spinn_one_of_five);
        buttonsLayout = (LinearLayout) fragment_view.findViewById(R.id.layout_one_of_five);
        //buttonsLeftGone();
        textView = (TextView) fragment_view.findViewById(R.id.text_view_one_of_five);

        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();

        if (buttonsArray != null)
        {
            textView.setText(mainWord);
            buttonsLeftGone();
            for (int i = 0; i < buttonsArray.length; i++)
            {
                Button buttonLeft = (Button) buttonsLayout.getChildAt(i);
                buttonLeft.setText(buttonsArray[i].getText());
                buttonLeft.setVisibility(buttonsArray[i].getVisibility());
                buttonsArray[i] = buttonLeft;
                btnLeft_OnClick(buttonsArray[i], i);
            }
        }

        return fragment_view;
    }

    private void buttonsLeftGone()
    {
        for (int i = 0; i < buttonsLayout.getChildCount(); i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            button.setId(10+i);
            button.setVisibility(View.GONE);
        }
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
        buttonsArray = new Button[count];

        final z_RandomNumberGenerator generator = new z_RandomNumberGenerator(count, 100);
        final int finalCount = count;
        AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<DataBaseEntry> list)
            {
                controlList = list;
                controlListSize = controlList.size();
                randomGenerator = new z_RandomNumberGenerator(controlListSize, 133);
                for (int i = 0; i < controlList.size(); i++)
                {
                    Button button = (Button) buttonsLayout.getChildAt(i);
                    button.setVisibility(View.VISIBLE);
                    button.setTranslationX(0);
                    button.setTranslationY(0);
                    btnLeft_OnClick(button, i);
                    if (button.getVisibility() == View.VISIBLE)
                    {
                        buttonsArray[i] = button;
                    }

                    buttonsArray[i].setText(controlList.get(i).get_translate());
                    wordIndex++;
                }
                int randIndex = randomGenerator.generate();
                textView.setText(list.get(randIndex).get_english());

                animator = new t_Animator(buttonsArray, textView);
                animator.registrationITextViewToleftEnd(t_OneOfFiveTest.this);
                animator.registrationButtonsToDownEnd(t_OneOfFiveTest.this);
                animator.setButtonToTopListener(t_OneOfFiveTest.this);
                animator.setButtonToLeftListener(t_OneOfFiveTest.this);
            }
        };
        asyncTask.execute(spinnSelectedItem, wordIndex, count);
        asyncTask = null;

//        for (int i = 0; i < count; i++)
//        {
//            Button button = (Button) buttonsLayout.getChildAt(i);
//            button.setVisibility(View.VISIBLE);
//            button.setTranslationX(0);
//            btnLeft_OnClick(button, i);
//            if (button.getVisibility() == View.VISIBLE)
//            {
//                buttonsArray[i] = button;
//            }
//        }
    }
    private static int controlListSize = 0;
    private void btnLeft_OnClick(final View view, final int index)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                btn_position = index;
                Toast.makeText(getActivity(), "Клик по кнопке "+index, Toast.LENGTH_SHORT).show();
                compareWords(spinnSelectedItem,textView.getText().toString(),buttonsArray[btn_position].getText().toString());
            }
        });
    }

    private static z_RandomNumberGenerator randomGenerator;
    private int range = 127;
    private ArrayList<DataBaseEntry> listFromDB;
    int indexEn = -1;
    int indexRu = -1;
    private void compareWords(String tableName, String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;

        if (enword != null && ruword != null)
        {
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
                        animator.buttonToRight(btn_position);
                    }
                };
                asyncTask.execute(tableName, wordIndex+1, wordIndex+1);
            }
            else
            {
                Toast.makeText(getActivity(), "Неправильно", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void textViewToLeftEnd(int result)
    {
        if (listFromDB.size() > 0)
        {
            controlList.set(indexEn, listFromDB.get(0));
            buttonsArray[btn_position].setText(listFromDB.get(0).get_translate());
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
            buttonsArray[btn_position].setVisibility(View.INVISIBLE);
        }
        if (btn_position == 0)
        {
            animator.buttonToRight(btn_position);
        }

        if (btn_position > 0)
        {
            animator.buttonsToDown(btn_position);
        }


        wordIndex++;
    }

    @Override
    public void buttonsToDownEnd(int result)
    {
        //Toast.makeText(getActivity(), "buttonsToDownEnd = "+result, Toast.LENGTH_SHORT).show();
        //animator.textViewToRight();
        animator.buttonToTop(btn_position);
    }

    @Override
    public void buttonToTopListener(int result)
    {
        animator.textViewToRight();
        animator.buttonToLeft(btn_position);
    }

    @Override
    public void buttonToLeftListener(int result)
    {
        Button[] newArray = null;
        if (true)
        {
            int j = 0;
            newArray = new Button[buttonsArray.length];
            newArray[0] = buttonsArray[btn_position];
            for (int i=0; i < btn_position; i++)
            {
                j++;
                newArray[j] = buttonsArray[i];
            }

            for (int i = btn_position+1; i < buttonsArray.length; i++)
            {
                newArray[i] = buttonsArray[i];

            }
            //updateArrayClickListenerLeft(newArray);
        } else
        {

        }
    }

    private void updateArrayClickListenerLeft(Button[] newArray)
    {
        buttonsArray = null;
        buttonsArray = newArray;
        for (int i = 0; i < buttonsArray.length; i++)
        {
            if (buttonsArray[i] != null)
            {
                btnLeft_OnClick(buttonsArray[i], i);
            }
        }
        Button[] arrayBtn = buttonsArray;
    }


}
