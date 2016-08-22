package com.myapp.lexicon;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link t_MatchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link t_MatchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class t_MatchFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    SimpleCursorAdapter scAdapter;



    public t_MatchFragment()
    {
        // Required empty public constructor
        this.setRetainInstance(true);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment t_MatchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static t_MatchFragment newInstance(String param1, String param2)
    {
        t_MatchFragment fragment = new t_MatchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    public static final int ROWS = 5;
    private LinearLayout linLayoutLeft, linLayoutRight;
    private ViewPropertyAnimator animToLeft, animToRight, animToTop, animToDown;
    private long duration = 1000;
    private ArrayList<DataBaseEntry> wordsList = new ArrayList<>();
    private Spinner spinnListDict;
    private ArrayList<String> storedListDict = new ArrayList<>();
    private int spinnSelectedIndex = 0;
    private int btn_left_position;
    private int btn_right_position;
    private int buttonId;
    private DisplayMetrics metrics;
    private int windowHeight;
    private float textSize;
    private final String LEFT_SIDE = "left";
    private final String RIGHT_SIDE = "right";
    private final String KEY_STORED_LIST_DICT = "storedListDict";
    private DataBaseQueries baseQueries;
    private int wordsCount;
    private DataBaseQueries.GetWordsCountAsync getWordsCount;
    private z_RandomNumberGenerator generatorLeft;
    private z_RandomNumberGenerator generatorRight;
    private z_LockOrientation lockOrientation;
    private int wordIndex = 1;
    private String KEY_WORD_INDEX = "wordIndex";
    View fragment_view = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragment_view = inflater.inflate(R.layout.t_fragment_match, container, false);
        initViews(fragment_view);
        if (savedInstanceState==null)
        {
            setItemsToSpinnListDict();
        }

        if (savedInstanceState != null)
        {
            storedListDict = savedInstanceState.getStringArrayList(KEY_STORED_LIST_DICT);
            ArrayAdapter<String> spinnAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.my_content_spinner_layout, storedListDict);
            spinnListDict.setAdapter(spinnAdapter);
            //spinnSelectedIndex = savedInstanceState.getInt(SPINN_LIST_DICT);
            wordIndex = savedInstanceState.getInt(KEY_WORD_INDEX);

            for (int i = 0; i < ROWS; i++)
            {
                Button btnLeft = (Button) linLayoutRight.getChildAt(i);
                btnLeft.setVisibility(AppData.arrayBtnRight[i].getVisibility());
                btnLeft.setText(AppData.arrayBtnRight[i].getText().toString());

                Button btnRight = (Button) linLayoutLeft.getChildAt(i);
                btnRight.setVisibility(AppData.arrayBtnLeft[i].getVisibility());
                btnRight.setText(AppData.arrayBtnLeft[i].getText().toString());
            }
        }

        return fragment_view;
    }

    private String SPINN_LIST_DICT = "spinner";
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        spinnSelectedIndex = spinnListDict.getSelectedItemPosition();
        outState.putInt(SPINN_LIST_DICT, spinnSelectedIndex);
        outState.putInt(KEY_WORD_INDEX, wordIndex);
        outState.putStringArrayList(KEY_STORED_LIST_DICT, storedListDict);
        for (int i = 0; i < ROWS; i++)
        {
            AppData.arrayBtnRight[i] = (Button) linLayoutRight.getChildAt(i);
            AppData.arrayBtnLeft[i] = (Button) linLayoutLeft.getChildAt(i);
        }

        super.onSaveInstanceState(outState);
    }
    private void initViews(View fragment_view)
    {
        getHeightScreen();
        //region Инициализация экземпляра z_LockOrientation для блокировки смены ориентации экрана
        lockOrientation = new z_LockOrientation(getActivity());
        //endregion

        //region инициализация экземпляра DataBaseQueries для работы с БД
        try
        {
            baseQueries = new DataBaseQueries(getActivity().getApplicationContext());
        } catch (SQLException e)
        {
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(),"Error - "+e.getMessage(),Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        //endregion
        linLayoutLeft = (LinearLayout) fragment_view.findViewById(R.id.left_layout);
        buttonsLeftGone();

        //region получение правого LinearLayout и скрытие его содержимого
        linLayoutRight = (LinearLayout) fragment_view.findViewById(R.id.right_layout);
        buttonsRightGone();
        //endregion
        //region получение Spinner выбора словаря
        spinnListDict = (Spinner) fragment_view.findViewById(R.id.spinn_list_dict);
        spinnListDict_OnItemSelectedListener();
        //endregion
        //region инициализация генераторов случайных чисел
        generatorLeft = new z_RandomNumberGenerator(wordsCount,0);
        generatorRight = new z_RandomNumberGenerator(wordsCount, 100);
        //endregion

    }

    private void setItemsToSpinnListDict()
    {
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

    private void buttonsRightGone()
    {
        for (int i = 0; i < ROWS; i++)
        {
            Button button = (Button) linLayoutRight.getChildAt(i);
            button.setId(i);
            button.setVisibility(View.GONE);
        }
    }

    private void buttonsLeftGone()
    {
        for (int i = 0; i < ROWS; i++)
        {
            Button button = (Button) linLayoutLeft.getChildAt(i);
            button.setId(i);
            button.setVisibility(View.GONE);
        }
    }

    private String spinnSelectedItem;
    private void spinnListDict_OnItemSelectedListener()
    {
        spinnListDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                lockOrientation.lock();
                spinnSelectedItem = spinnListDict.getSelectedItem().toString();
                getWordsCount = new DataBaseQueries.GetWordsCountAsync()
                {
                    @Override
                    public void resultAsyncTask(int res)
                    {
                        wordsCount = res;
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        getWordsCount = null;
                        fillLayoutRight(wordsCount);
                        fillLayoutLeft(wordsCount);
                    }
                };
                getWordsCount.execute(spinnSelectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    private void fillLayoutRight(final int rowsCount)
    {
        buttonsRightGone();
        if (rowsCount <= 0) return;
        lockOrientation.lock();
        int count = rowsCount;
        if (count > ROWS)
        {
            count = ROWS;
        }
        generatorRight = new z_RandomNumberGenerator(count, 100);
        AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<DataBaseEntry> list)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    int randIndex = generatorRight.generate();
                    AppData.arrayBtnRight[i] = (Button) linLayoutRight.getChildAt(i);
                    AppData.arrayBtnRight[i].setText(list.get(randIndex).get_translate());

                }
            }
        };
        asyncTask.execute(spinnSelectedItem, wordIndex, count);
        asyncTask = null;

        for (int i = 0; i < count; i++)
        {
            Button button = (Button) linLayoutRight.getChildAt(i);
            button.setVisibility(View.VISIBLE);
            AppData.arrayBtnRight[i] = button;
            btnRight_OnClick(button, i);
        }

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
        generatorLeft = new z_RandomNumberGenerator(count, 0);
        AsyncTask<Object, Void, ArrayList<DataBaseEntry>> asyncTask = new DataBaseQueries.GetWordsFromDBAsync()
        {
            @Override
            public void resultAsyncTask(ArrayList<DataBaseEntry> list)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    int randIndex = generatorLeft.generate();
                    AppData.arrayBtnLeft[i].setText(list.get(randIndex).get_english());
                }
            }
        };
        asyncTask.execute(spinnSelectedItem, wordIndex, count);
        asyncTask = null;

        for (int i = 0; i < count; i++)
        {
            Button button = (Button) linLayoutLeft.getChildAt(i);
            button.setVisibility(View.VISIBLE);
            AppData.arrayBtnLeft[i] = button;
            btnLeft_OnClick(button, i);
        }

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private String enWord = null;
    private void btnLeft_OnClick(final View view, final int index)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                enWord = AppData.arrayBtnLeft[index].getText().toString();
                compareWords(spinnSelectedItem, enWord, ruWord);
                if (index > 0)
                {

                }
            }
        });
    }


    private String ruWord = null;
    private void btnRight_OnClick(final View view, final int index)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ruWord = AppData.arrayBtnRight[index].getText().toString();
                compareWords(spinnSelectedItem, enWord, ruWord);
                if (index > 0)
                {

                }
            }
        });
    }

    private Integer resultCompare = 0;
    private void compareWords(String tableName, String enword, String ruword)
    {
        if (enword == null || ruword == null)   return;
        if (enword != null && ruword != null)
        {
            DataBaseQueries.GetRowIdOfWordAsync asyncTask = new DataBaseQueries.GetRowIdOfWordAsync()
            {
                @Override
                public void resultAsyncTask(Integer id)
                {
                    resultCompare = id;
                    if (resultCompare > 0)
                    {
                        Toast.makeText(getActivity().getApplicationContext(), "Правильно", Toast.LENGTH_SHORT).show();
                        enWord = null; ruWord = null;
                    }
                    if (resultCompare < 0)
                    {
                        Toast.makeText(getActivity().getApplicationContext(), "Неправильно", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            asyncTask.execute(tableName, enword, ruword);
        }
    }

    private void updateArrayClickListenerLeft(Button[] newArray)
    {
        AppData.arrayBtnLeft = newArray;
        for (int i = 0; i < AppData.arrayBtnLeft.length; i++)
        {
            btnLeft_OnClick(AppData.arrayBtnLeft[i], i);
        }
        Button[] arrayBtn = AppData.arrayBtnLeft;
    }

    private void updateArrayClickListenerRight(Button[] newArray)
    {
        AppData.arrayBtnRight = newArray;
        for (int i = 0; i < AppData.arrayBtnRight.length; i++)
        {
            btnRight_OnClick(AppData.arrayBtnRight[i], i);
        }
        Button[] arrayBtn = AppData.arrayBtnRight;
    }

    private void animToLeft_Listener(ViewPropertyAnimator animator)
    {

    }

    private void animToRight_Listener(ViewPropertyAnimator animator)
    {

    }

    private void animToDown_Listener(ViewPropertyAnimator animator, String side)
    {

    }

    private void animToTop_Listener(ViewPropertyAnimator animator, String side)
    {

    }






    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //region Получение размеров экрана
    private float getHeightScreen()
    {
        metrics = new DisplayMetrics();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        int windowHeight = metrics.heightPixels;
        //windowHeight / 60;
        return windowHeight;

    }


}
