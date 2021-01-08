package com.myapp.lexicon.wordstests

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myapp.lexicon.R
import com.myapp.lexicon.adapters.OneFiveTestAdapter
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.dialogs.TestCompleteDialog
import com.myapp.lexicon.helpers.RandomNumberGenerator
import com.myapp.lexicon.wordstests.DialogTestComplete.IDialogComplete_Result
import kotlinx.android.synthetic.main.one_of_five_fragm_new.*
import java.util.*
import kotlin.collections.ArrayList

const val ROWS: Int = 5

class OneOfFiveFragmNew : Fragment(), TestCompleteDialog.ITestCompleteDialogListener, OneFiveTestAdapter.ITestAdapterListener
{
    private lateinit var viewModel: OneOfFiveViewModel
    private var wordList: List<Word>? = null
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
        viewModel = ViewModelProvider(this)[OneOfFiveViewModel::class.java]

        if (arguments != null)
        {
            wordList = requireArguments().getParcelableArrayList(ARG_WORD_LIST)
            wordList?.let { viewModel.initTest(it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        val root = inflater.inflate(R.layout.one_of_five_fragm_new, container, false)

        answersRecyclerView = root.findViewById(R.id.answersRecyclerView)
        viewModel.adapterList.observe(viewLifecycleOwner, {
            answersRecyclerView?.apply {
                this.adapter = OneFiveTestAdapter(it).apply {
                    setHasStableIds(true)
                    layoutManager = LinearLayoutManager(requireContext())
                    setOnItemClickListener(this@OneOfFiveFragmNew)
                }
            }
        })

        mysteryWordView = root.findViewById<TextView>(R.id.mysteryWordView)
        viewModel.mysteryWord.observe(viewLifecycleOwner, {
            mysteryWordView.text = it
        })

        val progressView = root.findViewById<ProgressBar>(R.id.progressView1of5).apply {
            wordList?.size?.let {
                max = ROWS + it
            }
        }

        val progressValueView = root.findViewById<TextView>(R.id.progressValueView)

        viewModel.progress.observe(viewLifecycleOwner, {
            progressView.progress = it
            val progressValue = "$it/${progressView.max}"
            progressValueView.text = progressValue
            if (it == progressView.max)
            {
                val errors = viewModel.wrongAnswerCount.value
                val dialog = errors?.let { it1 -> TestCompleteDialog.getInstance(it1, this) }
                dialog?.show(requireActivity().supportFragmentManager, TestCompleteDialog.TAG)
            }
        })

        return root
    }


    override fun onItemClickListener(position: Int, word: Word, view: Button)
    {
        val testAdapter = answersRecyclerView?.adapter as OneFiveTestAdapter
        val items = testAdapter.getItems()
        if (!items.isNullOrEmpty())
        {
            val translate = mysteryWordView.text.toString()
            val english = view.text.toString()
            val result = items.any { w -> w.english == english && w.translate == translate }
            if (result)
            {
                view.setBackgroundResource(R.drawable.text_btn_for_test_green)
                testAdapter.removeItem(english, translate)
                val animRight = AnimationUtils.loadAnimation(requireContext(), R.anim.from_right_to_left_anim)
                if (!testAdapter.getItems().isNullOrEmpty())
                {
                    val itemCount = testAdapter.itemCount
                    val randomIndex = RandomNumberGenerator(itemCount, (Date().time.toInt())).generate()
                    viewModel.setMysteryWord(testAdapter.getItems()[randomIndex].translate)
                }
                else
                {
                    viewModel.mysteryWord = MutableLiveData()
                }
                viewModel.setProgress(progressView1of5.progress + 1)
                mysteryWordView.startAnimation(animRight.apply {
                    setAnimationListener(object : Animation.AnimationListener
                    {
                        override fun onAnimationStart(p0: Animation?)
                        {
                            if (!viewModel.wordsList.value.isNullOrEmpty())
                            {
                                val nextItem = viewModel.takeNextWord()
                                nextItem?.let{
                                    testAdapter.addItem(position, it)
                                    testAdapter.notifyDataSetChanged()
                                }
                            }
                            view.setBackgroundResource(R.drawable.text_button_for_test)
                        }

                        override fun onAnimationEnd(p0: Animation?)
                        {
                        }

                        override fun onAnimationRepeat(p0: Animation?)
                        {
                        }

                    })
                })
            }
            else
            {
                viewModel.increaseWrongAnswerCount()
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

                    override fun onAnimationRepeat(p0: Animation?){}
                })
                view.startAnimation(animNotRight)
            }
        }
    }

    override fun onTestPassed()
    {
        Toast.makeText(requireContext(), "***** onTestPassed() ******", Toast.LENGTH_SHORT).show()
    }

    override fun onTestFailed(errors: Int)
    {
        Toast.makeText(requireContext(), "***** onTestFailed(errors: $errors) ******", Toast.LENGTH_SHORT).show()
    }


}