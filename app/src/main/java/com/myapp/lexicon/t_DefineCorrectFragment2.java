package com.myapp.lexicon;


import android.content.pm.ActivityInfo;
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
public class t_DefineCorrectFragment2 extends Fragment
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
    private static z_RandomNumberGenerator generatorLeft;

    public t_DefineCorrectFragment2()
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
        View fragment_view = inflater.inflate(R.layout.t_define_correct_layout2, container, false);
        spinnListDict= (Spinner) fragment_view.findViewById(R.id.spinn_list_dict2);
        buttonsLayout = (LinearLayout) fragment_view.findViewById(R.id.left_layout2);
        //buttonsLeftGone();
        textView = (TextView) fragment_view.findViewById(R.id.text_view2);

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
                if (wordsResidue > 0)
                {
                    generatorLeft = new z_RandomNumberGenerator(wordsResidue,0);
                }
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

        final z_RandomNumberGenerator generator = new z_RandomNumberGenerator(count, 0);
        final int finalCount = count;
        AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<DataBaseEntry> list)
            {
                controlList = list;
                for (int i = 0; i < list.size(); i++)
                {
                    int randIndex = generator.generate();
                    buttonsArray[i].setText(list.get(randIndex).get_translate());
                    wordIndex = finalCount;
                }
                textView.setText(list.get(0).get_english());
            }
        };
        asyncTask.execute(spinnSelectedItem, wordIndex, count);
        asyncTask = null;


        for (int i = 0; i < count; i++)
        {
            Button button = (Button) buttonsLayout.getChildAt(i);
            button.setVisibility(View.VISIBLE);
            button.setTranslationX(0);
            btnLeft_OnClick(button, i);
            if (button.getVisibility() == View.VISIBLE)
            {
                buttonsArray[i] = button;
            }
        }


    }

    private void btnLeft_OnClick(final View view, final int index)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                btn_position = index;


//                enWord = AppData.arrayBtnLeft[index].getText().toString();
//                compareWords(spinnSelectedItem, enWord, ruWord);
//                btnNoRight = (Button) view;
                Toast.makeText(getActivity(), "Клик по кнопке "+index, Toast.LENGTH_SHORT).show();
                compareWords(spinnSelectedItem,textView.getText().toString(),buttonsArray[btn_position].getText().toString());
            }
        });
    }

    private void compareWords(String tableName, String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;
        int indexEn = -1, indexRu = -1;
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
                Toast.makeText(getActivity(), "Правильно", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getActivity(), "Неправильно", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
