package com.myapp.lexicon.wordstests;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.Word;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;



public class FindPairFragmNew extends Fragment implements DialogTestComplete.IDialogComplete_Result
{

    private static final String ARG_WORD_LIST = "param1";


    // TODO: Rename and change types of parameters
    private List<Word> wordList;


    public FindPairFragmNew()
    {
        // Required empty public constructor
    }


    public static FindPairFragmNew newInstance(List<Word> list)
    {
        FindPairFragmNew fragment = new FindPairFragmNew();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_WORD_LIST, (ArrayList<? extends Parcelable>) list);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            wordList = getArguments().getParcelableArrayList(ARG_WORD_LIST);
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.find_pair_fragm_new, container, false);
    }

    @Override
    public void dialogCompleteResult(int res)
    {

    }
}