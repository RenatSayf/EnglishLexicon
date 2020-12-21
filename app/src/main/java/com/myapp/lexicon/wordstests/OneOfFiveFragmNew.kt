package com.myapp.lexicon.wordstests

import com.myapp.lexicon.wordstests.DialogTestComplete.IDialogComplete_Result
import com.myapp.lexicon.database.Word
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.myapp.lexicon.R
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.adapters.OneFiveTestAdapter
import java.util.ArrayList

class OneOfFiveFragmNew : Fragment(), IDialogComplete_Result
{
    private var wordList: List<Word>? = null

    companion object
    {
        private const val ARG_WORD_LIST = "ARG_WORD_LIST"
        private var instance: OneOfFiveFragmNew? = null

        @JvmStatic
        fun getInstance(list: List<Word>): OneOfFiveFragmNew?
        {
            if (instance == null) instance = OneOfFiveFragmNew() else instance as OneOfFiveFragmNew
            return instance?.apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_WORD_LIST, list as ArrayList<out Parcelable?>?)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        if (arguments != null)
        {
            wordList = requireArguments().getParcelableArrayList(ARG_WORD_LIST)
            return
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        val root = inflater.inflate(R.layout.one_of_five_fragm_new, container, false)

        val answersRecyclerView = root.findViewById<RecyclerView>(R.id.answersRecyclerView)
        answersRecyclerView.apply {
            this.adapter = OneFiveTestAdapter(wordList as MutableList<Word>).apply {
                setHasStableIds(true)
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        return root
    }

    override fun dialogCompleteResult(res: Int)
    {
    }


}