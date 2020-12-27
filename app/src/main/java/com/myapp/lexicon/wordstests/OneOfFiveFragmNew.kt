package com.myapp.lexicon.wordstests

import android.animation.Animator
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.adapters.OneFiveTestAdapter
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.wordstests.DialogTestComplete.IDialogComplete_Result
import java.util.*
import kotlin.collections.ArrayList

private const val ROWS: Int = 5

class OneOfFiveFragmNew : Fragment(), IDialogComplete_Result, OneFiveTestAdapter.ITestAdapterListener
{
    //private lateinit var viewModel: OneOfFiveViewModel
    private var wordList: ArrayList<Word>? = null
    private var answersRecyclerView: RecyclerView? = null
    private lateinit var mysteryWordView: TextView


    companion object
    {
        private const val ARG_WORD_LIST = "ARG_WORD_LIST"
        private var instance: OneOfFiveFragmNew? = null

        @JvmStatic
        fun getInstance(list: List<Word>): OneOfFiveFragmNew?
        {
            val shuffledList = list.shuffled()
            if (instance == null) instance = OneOfFiveFragmNew() else instance as OneOfFiveFragmNew
            return instance?.apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_WORD_LIST, shuffledList as ArrayList<out Parcelable?>?)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        //viewModel = ViewModelProvider(this)[OneOfFiveViewModel::class.java]

        if (arguments != null)
        {
            wordList = requireArguments().getParcelableArrayList(ARG_WORD_LIST)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        val root = inflater.inflate(R.layout.one_of_five_fragm_new, container, false)

        val startItems = wordList?.take(5) as ArrayList
        answersRecyclerView = root.findViewById(R.id.answersRecyclerView)
        answersRecyclerView?.apply {
            this.adapter = startItems.let {
                OneFiveTestAdapter(it).apply {
                    setHasStableIds(true)
                    layoutManager = LinearLayoutManager(requireContext())
                    setOnItemClickListener(this@OneOfFiveFragmNew)
                }
            }
        }


        mysteryWordView = root.findViewById<TextView>(R.id.mysteryWordView).apply {
            text = wordList?.last()?.translate ?: ""
        }


        return root
    }

    override fun dialogCompleteResult(res: Int)
    {
    }

    override fun onItemClickListener(position: Int, word: Word, view: Button)
    {
        val testAdapter = answersRecyclerView?.adapter as OneFiveTestAdapter
        val translate = mysteryWordView.text.toString()
        val word2 = Word(word._id, word.dictName, word.english, translate, word.countRepeat)
        val items = testAdapter.getItems()
        if (!items.isNullOrEmpty() && items.contains(word2))
        {
            view.setBackgroundResource(R.drawable.text_btn_for_test_green)
            testAdapter.removeItem(position)
            val animRight = AnimationUtils.loadAnimation(requireContext(), R.anim.from_right_to_left_anim)
            if (!testAdapter.getItems().isNullOrEmpty())
            {
                mysteryWordView.text = testAdapter.getItems()[0].translate
            }
            else
            {
                mysteryWordView.text = ""
                        return
            }
            mysteryWordView.startAnimation(animRight)
        }
        else
        {
            val animNotRight = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_not_right)
            animNotRight.setAnimationListener(object : Animation.AnimationListener
            {
                override fun onAnimationStart(p0: Animation?)
                {
                    view.setBackgroundResource(R.drawable.text_btn_for_test_red)
                }

                override fun onAnimationEnd(p0: Animation?)
                {
                    view.setBackgroundResource(R.drawable.text_button_for_test)
                }

                override fun onAnimationRepeat(p0: Animation?)
                {

                }
            })
            view.startAnimation(animNotRight)
        }
    }


}