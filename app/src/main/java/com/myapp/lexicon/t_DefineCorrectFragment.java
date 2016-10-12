package com.myapp.lexicon;


import android.animation.Animator;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
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
public class t_DefineCorrectFragment extends Fragment
{
    public static final int ROWS = 5;
    private static Button[] buttonsArray;
    private static ArrayList<String> storedListDict = new ArrayList<>();
    private TextView textView;
    private LinearLayout buttonsLayout;
    private Spinner spinnListDict;
    private static int spinnSelectedIndex = -1;
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
    private static z_RandomNumberGenerator randomGenerator;
    private static ArrayList<DataBaseEntry> controlList;
    private static int controlListSize = 0;
    private int range = 127;

    public t_DefineCorrectFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        lockOrientation = new z_LockOrientation(getActivity());
        View fragment_view = inflater.inflate(R.layout.t_define_correct_layout, container, false);
        spinnListDict= (Spinner) fragment_view.findViewById(R.id.spinn_one_of_five);
        buttonsLayout = (LinearLayout) fragment_view.findViewById(R.id.layout_one_of_five);
        //buttonsLeftGone();
        textView = (TextView) fragment_view.findViewById(R.id.text_view_one_of_five);

        spinnListDict_OnItemSelectedListener();
        setItemsToSpinnListDict();

        if (savedInstanceState != null)
        {
            for (int i = 0; i < buttonsLayout.getChildCount(); i++)
            {
                Button button = (Button) buttonsLayout.getChildAt(i);
                try
                {
                    button.setText(buttonsArray[i].getText());
                    button.setVisibility(buttonsArray[i].getVisibility());
                    //buttonLeft.setTextSize(textSize);
                    buttonsArray[i] = button;
                    btnLeft_OnClick(buttonsArray[i], i);
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    button.setVisibility(View.GONE);
                }
            }
        }

        return fragment_view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(KEY_BTN_POSITION, btn_position);
        super.onSaveInstanceState(outState);
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
        lockOrientation.lock();
        buttonsArray = null;
        btn_position = 0;
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
                fillLayoutLeft(ROWS);
                spinnSelectedIndex = position;
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };
        getWordsCount.execute(spinnSelectedItem);
    }

    private void fillLayoutLeft(final int rowsCount)
    {
        buttonsLeftGone();
        if (rowsCount <= 0) return;
        lockOrientation.lock();
        int count = rowsCount;
        if (count > ROWS)
        {
            count = ROWS;
        }

        final int finalCount = count;
        AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<DataBaseEntry> list)
            {
                controlList = list;
                controlListSize = controlList.size();
                buttonsArray = new Button[controlListSize];
                randomGenerator = new z_RandomNumberGenerator(controlListSize, range);
                for (int i = 0; i < controlList.size(); i++)
                {
                    Button button = (Button) buttonsLayout.getChildAt(i);
                    button.setVisibility(View.VISIBLE);
                    button.setTranslationX(0);
                    button.setTranslationY(0);
                    btnLeft_OnClick(button, i);
                    buttonsArray[i] = button;

                    buttonsArray[i].setText(controlList.get(i).get_translate());
                    wordIndex++;
                }
                int randIndex = randomGenerator.generate();
                textView.setText(list.get(randIndex).get_english());
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
                btn_position = index;
                width = view.getWidth();
                height = view.getHeight();

                Toast.makeText(getActivity(), "Клик по кнопке "+index, Toast.LENGTH_SHORT).show();
                compareWords("AAA","SSS","DDD");
            }
        });
    }

    private void compareWords(String tableName, String enword, String ruword)
    {
        //lockOrientation.lock();
        animToRight = buttonsArray[btn_position].animate().x((width + delta))
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator());
        animToLeft_Listener(animToRight);

        textView.animate().x(-(width + delta))
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator());
    }

    private void animToLeft_Listener(ViewPropertyAnimator animator)
    {
        animator.setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                if (btn_position == 0)
                {
                    textView.animate().translationX(0).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator());
                    animToRight = buttonsArray[btn_position].animate().x(0).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator());
                    animToRight_Listener(animToRight);
                }

                if (btn_position > 0)
                {
                    for (int i=btn_position-1; i >= 0; i--)
                    {
                        animToDown = buttonsArray[i].animate().translationYBy(height).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator());
                        if (i == 0)
                        {
                            animToDown_Listener(animToDown);
                        }
                    }
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

    private void animToDown_Listener(ViewPropertyAnimator animToDown)
    {
        animToDown.setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                animToTop = buttonsArray[btn_position].animate().translationYBy(-height * btn_position).setDuration(10);
                animToTop_Listener(animToTop);
                //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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

    private void animToTop_Listener(ViewPropertyAnimator animToTop)
    {
        animToTop.setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                animToRight = buttonsArray[btn_position].animate().x(0).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator());
                animToRight_Listener(animToRight);
                textView.animate().translationX(0).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator());
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

    private void animToRight_Listener(ViewPropertyAnimator animator)
    {
        animator.setListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
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
                    updateArrayClickListenerLeft(newArray);
                } else
                {

                }

                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
