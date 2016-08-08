package com.myapp.lexicon;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;

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
    private Button btnLeft0, btnLeft1, btnLeft2, btnLeft3, btnLeft4;
    private Button btnRight0, btnRight1, btnRight2, btnRight3, btnRight4;
    private TableRow tableRow0, tableRow1, tableRow2, tableRow3, tableRow4;
    private Button[] arrayBtnLeft = new Button[5];
    private Button[] arrayBtnRight = new Button[5];
    private TableRow[] arrayTableRow = new TableRow[5];
    private ArrayList<DataBaseEntry> wordsList = new ArrayList<>();

    private int btn_position;
    private int word_position;
    private int buttonId;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragment_view = inflater.inflate(R.layout.t_fragment_match, container, false);
        initViews(fragment_view);
        return fragment_view;
    }

    private void initViews(View fragment_view)
    {
        btnLeft0 = (Button) fragment_view.findViewById(R.id.left_button0);
        btnLeft1 = (Button) fragment_view.findViewById(R.id.left_button1);
        btnLeft2 = (Button) fragment_view.findViewById(R.id.left_button2);
        btnLeft3 = (Button) fragment_view.findViewById(R.id.left_button3);
        btnLeft4 = (Button) fragment_view.findViewById(R.id.left_button4);

        btnRight0 = (Button) fragment_view.findViewById(R.id.right_button0);
        btnRight1 = (Button) fragment_view.findViewById(R.id.right_button1);
        btnRight2 = (Button) fragment_view.findViewById(R.id.right_button2);
        btnRight3 = (Button) fragment_view.findViewById(R.id.right_button3);
        btnRight4 = (Button) fragment_view.findViewById(R.id.right_button4);

        arrayBtnLeft[0] = btnLeft0;     arrayBtnRight[0] = btnRight0;
        arrayBtnLeft[1] = btnLeft1;     arrayBtnRight[1] = btnRight1;
        arrayBtnLeft[2] = btnLeft2;     arrayBtnRight[2] = btnRight2;
        arrayBtnLeft[3] = btnLeft3;     arrayBtnRight[3] = btnRight3;
        arrayBtnLeft[4] = btnLeft4;     arrayBtnRight[4] = btnRight4;

        arrayTableRow[0] = tableRow0;
        arrayTableRow[1] = tableRow1;
        arrayTableRow[2] = tableRow2;
        arrayTableRow[3] = tableRow3;
        arrayTableRow[4] = tableRow4;

        btnLeft_OnClick();
        btnRight_OnClick();

    }

    private void btnLeft_OnClick()
    {
        View.OnClickListener clickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        };

        for (int i = 0; i < arrayBtnLeft.length; i++)
        {
            Button button = arrayBtnLeft[i];
            button.setOnClickListener(clickListener);
        }
    }

    private void btnRight_OnClick()
    {
        View.OnClickListener clickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        };

        for (int i = 0; i < arrayBtnRight.length; i++)
        {
            Button button = arrayBtnRight[i];
            button.setOnClickListener(clickListener);
        }
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




}
