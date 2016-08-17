package com.myapp.lexicon;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
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
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragment_view = inflater.inflate(R.layout.t_fragment_match, container, false);
        initViews(fragment_view);

        if (savedInstanceState != null)
        {
            spinnSelectedIndex = savedInstanceState.getInt(SPINN_LIST_DICT);
        }
        setRetainInstance(true);
        return fragment_view;
    }


    public static final int ROWS = 5;
    private LinearLayout linLayoutLeft, linLayoutRight;
    private ViewPropertyAnimator animToLeft, animToRight, animToTop, animToDown;
    private long duration = 1000;
    private Button[] arrayBtnLeft =new Button[ROWS];
    private Button[] arrayBtnRight =new Button[ROWS];
    private ArrayList<DataBaseEntry> wordsList = new ArrayList<>();
    private Spinner spinnListDict;
    private int spinnSelectedIndex = 0;
    private int btn_left_position;
    private int btn_right_position;
    private int buttonId;
    private DisplayMetrics metrics;
    private int windowHeight;
    private float textSize;
    private final String LEFT_SIDE = "left";
    private final String RIGHT_SIDE = "right";
    private DataBaseQueries baseQueries;
    private int wordsCount;
    private DataBaseQueries.GetWordsCountAsync getWordsCount;
    private z_RandomNumberGenerator generatorLeft;
    private z_RandomNumberGenerator generatorRight;
    private z_LockOrientation lockOrientation;

    private void initViews(View fragment_view)
    {
        metrics = new DisplayMetrics();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        windowHeight = metrics.heightPixels;
        textSize = windowHeight / 60;

        lockOrientation = new z_LockOrientation(getActivity());

        try
        {
            baseQueries = new DataBaseQueries(getActivity().getApplicationContext());
        } catch (SQLException e)
        {
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(),"Error - "+e.getMessage(),Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        linLayoutLeft = (LinearLayout) fragment_view.findViewById(R.id.left_layout);
        linLayoutRight = (LinearLayout) fragment_view.findViewById(R.id.right_layout);

        spinnListDict = (Spinner) fragment_view.findViewById(R.id.spinn_list_dict);
        baseQueries.setListTableToSpinner(spinnListDict, spinnSelectedIndex);
        spinnListDict_OnItemSelectedListener();
        generatorLeft = new z_RandomNumberGenerator(wordsCount,0);
        generatorRight = new z_RandomNumberGenerator(wordsCount, 100);

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
                        if (wordsCount < ROWS)
                        {
                            cleanLinLayout();
                            fillLayoutLeft(wordsCount);
                            fillLayoutRight(wordsCount);
                        }
                        else if (wordsCount >= ROWS)
                        {
                            cleanLinLayout();
                            fillLayoutLeft(ROWS);
                            fillLayoutRight(ROWS);
                        }
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

    private void cleanLinLayout()
    {
        linLayoutLeft.removeAllViewsInLayout();
        linLayoutRight.removeAllViewsInLayout();
    }


    private void fillLayoutRight(final int rowsCount)
    {
        lockOrientation.lock();
        final z_RandomNumberGenerator generator = new z_RandomNumberGenerator(rowsCount,100);
        for (int i = 0; i < rowsCount; i++)
        {
            final Button button = new Button(getActivity().getApplicationContext());
            new DataBaseQueries.GetWordsFromDBAsync()
            {
                @Override
                public void resultAsyncTask(ArrayList<DataBaseEntry> list)
                {
                    int i_rand = generator.generate();
                    button.setText(list.get(i_rand).get_translate());
                }
            }.execute(spinnSelectedItem,1,rowsCount);
            button.setId(i);
            button.setTextSize(textSize);
            button.setPadding(0,0,0,0);
            button.setBackgroundResource(R.drawable.text_button_for_test);
            button.setTextColor(getResources().getColorStateList(R.color.t_text_color_for_btn));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(5,5,5,5);
            params.weight = 1;
            linLayoutRight.addView(button, params);
            arrayBtnRight[i]=button;
            btnRight_OnClick(button, i);
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void fillLayoutLeft(int rowsCount)
    {
        lockOrientation.lock();
        final z_RandomNumberGenerator generator = new z_RandomNumberGenerator(rowsCount,0);
        for (int i = 0; i < rowsCount; i++)
        {
            final Button button = new Button(getActivity().getApplicationContext());
            new DataBaseQueries.GetWordsFromDBAsync()
            {
                @Override
                public void resultAsyncTask(ArrayList<DataBaseEntry> list)
                {
                    int i_rand = generator.generate();
                    button.setText(list.get(i_rand).get_english());
                }
            }.execute(spinnSelectedItem,1,rowsCount);
            button.setId(i);
            button.setTextSize(textSize);
            button.setBackgroundResource(R.drawable.text_button_for_test);
            button.setTextColor(getResources().getColorStateList(R.color.t_text_color_for_btn));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(5,5,5,5);
            params.weight = 1;
            linLayoutLeft.addView(button, params);
            arrayBtnLeft[i]=button;
            btnLeft_OnClick(button, i);
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void btnLeft_OnClick(final View view, final int index)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }

    private void btnRight_OnClick(final View view, final int index)
    {
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }

    private void updateArrayClickListenerLeft(Button[] newArray)
    {
        arrayBtnLeft = newArray;
        for (int i = 0; i < arrayBtnLeft.length; i++)
        {
            btnLeft_OnClick(arrayBtnLeft[i], i);
        }
        Button[] arrayBtn = arrayBtnLeft;
    }

    private void updateArrayClickListenerRight(Button[] newArray)
    {
        arrayBtnLeft = newArray;
        for (int i = 0; i < arrayBtnLeft.length; i++)
        {
            btnRight_OnClick(arrayBtnLeft[i], i);
        }
        Button[] arrayBtn = arrayBtnLeft;
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

    private String SPINN_LIST_DICT = "spinner";
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        spinnSelectedIndex = spinnListDict.getSelectedItemPosition();
        outState.putInt(SPINN_LIST_DICT, spinnSelectedIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState)
    {

        super.onViewStateRestored(savedInstanceState);
    }


}
