package com.myapp.lexicon.wordstests

import android.animation.Animator
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.jakewharton.rxbinding2.widget.RxTextView
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.databinding.TestFragmentBinding
import com.myapp.lexicon.helpers.Keyboard
import com.myapp.lexicon.helpers.UiState
import com.myapp.lexicon.viewmodels.AnimViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class TestFragment : Fragment(R.layout.test_fragment)
{

    companion object
    {
        fun newInstance() = TestFragment()
    }

    private lateinit var binding: TestFragmentBinding
    private lateinit var testVM: TestViewModel
    private lateinit var animVM: AnimViewModel
    private val composite = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = TestFragmentBinding.bind(view)
        testVM = ViewModelProvider(this)[TestViewModel::class.java]
        animVM = ViewModelProvider(this)[AnimViewModel::class.java]

        testVM.currentWord.observe(viewLifecycleOwner, {
            binding.btnViewDict.text = it.dictName
        })

        testVM.wordsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty())
            {
                binding.enWordTV.text = testVM.wordsList.value!![0].english
                binding.ruWordTV.text = testVM.wordsList.value!![0].translate
            }
        })

        testVM.isRight.observe(viewLifecycleOwner, { isRight ->
            isRight?.let {
                when(it)
                {
                    true ->
                    {
                        Keyboard.getInstance().forceHide(requireContext(), binding.editTextView)
                        binding.ruWordTV.animate().scaleX(1f).scaleY(1f).apply {
                            duration = 500
                            interpolator = AnticipateOvershootInterpolator()
                            startDelay = 0
                            animIncreaseScaleListener(this)
                        }.start()
                        Toast.makeText(requireContext(), "Правильно...", Toast.LENGTH_LONG).show()
                    }
                    else -> Toast.makeText(requireContext(), "Неправильно...", Toast.LENGTH_LONG).show()
                }
            }
        })

        composite.add(
            RxTextView.textChanges(binding.editTextView)
                .debounce(250, TimeUnit.MILLISECONDS)
                .subscribe({ chars ->
                    if (chars.isNotEmpty())
                    {
                        val dict = binding.btnViewDict.text.toString()
                        testVM.getAllSimilarWords(dict, "%${chars}%")
                    }
                }, { t ->
                    t.printStackTrace()
                }))

        testVM.similarWords.observe(viewLifecycleOwner, { words ->
            if (words.isNotEmpty())
            {
                val translates = arrayListOf<String>()
                words.forEach {
                    translates.add(it.translate)
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, translates)
                binding.editTextView.apply {
                    setAdapter(adapter)
                }

            }
        })

        binding.editTextView.setOnItemClickListener(object : AdapterView.OnItemClickListener
        {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long)
            {
                val translateText = (p1 as TextView).text.toString()
                val enText = binding.enWordTV.text.toString()
                val searchedWord = Word(-1, "", enText, translateText, 1)
                testVM.searchWord(searchedWord)
            }

        })

        binding.clearBtnView.setOnClickListener {
            binding.editTextView.text.clear()
        }

        animVM.apply {
            uiState.observe(viewLifecycleOwner, { state ->
                when(state)
                {
                    is UiState.TextViewCreated ->
                    {
                        binding.ruWordTV.apply {
                            scaleX = state.scaleX
                            scaleY = state.scaleY
                        }
                    }
                    is UiState.TextViewAfterAnim ->
                    {
                        binding.ruWordTV.apply {
                            scaleX = state.scaleX
                            scaleY = state.scaleY
                        }
                    }
                }
            })
        }

        testVM.wordsCount.observe(viewLifecycleOwner, {
            binding.progressBar.max = it
            val progressValue = "1 / $it"
            binding.progressValueTV.text = progressValue
        })

        testVM.wordIndex.observe(viewLifecycleOwner, {
            binding.progressBar.progress = it
            val progressValue = "$it / ${testVM.wordsCount.value}"
            binding.progressValueTV.text = progressValue
        })

    }

    // TODO ViewPropertyAnimation.Scale Step 6 слушатель анимации
    private fun animIncreaseScaleListener(animator: ViewPropertyAnimator)
    {
        animator.setListener(object : Animator.AnimatorListener
        {
            override fun onAnimationStart(p0: Animator?)
            {

            }

            override fun onAnimationEnd(p0: Animator?)
            {
                binding.enWordTV.animate().scaleX(0f).scaleY(0f).apply {
                    duration = 500
                    interpolator = AnticipateOvershootInterpolator()
                    animDecreaseScaleListener(this)
                    startDelay = 1000
                }.start()

                binding.ruWordTV.animate().scaleX(0f).scaleY(0f).apply {
                    duration = 500
                    interpolator = AnticipateOvershootInterpolator()
                    animDecreaseScaleListener(this)
                    startDelay = 1000
                }.start()
            }

            override fun onAnimationCancel(p0: Animator?)
            {

            }

            override fun onAnimationRepeat(p0: Animator?)
            {

            }
        })
    }

    private fun animDecreaseScaleListener(animator: ViewPropertyAnimator)
    {
        animator.setListener(object : Animator.AnimatorListener
        {
            override fun onAnimationStart(p0: Animator?)
            {

            }

            override fun onAnimationEnd(p0: Animator?)
            {
                binding.editTextView.apply {
                    text.clear()
                    clearFocus()
                }
                val nextWord = testVM.getNextWords()
                nextWord?.let {
                    binding.enWordTV.text = nextWord.english
                    binding.ruWordTV.text = nextWord.translate
                    binding.enWordTV.animate().scaleX(1f).scaleY(1f).apply {
                        duration = 300
                        interpolator = AccelerateInterpolator()
                        setListener(null)
                        startDelay = 0
                    }
                } ?: run {
                    Toast.makeText(requireContext(), "Тест завершен...", Toast.LENGTH_LONG).show()
                }

            }

            override fun onAnimationCancel(p0: Animator?)
            {

            }

            override fun onAnimationRepeat(p0: Animator?)
            {

            }

        })
    }

    override fun onDestroy()
    {
        composite.apply {
            dispose()
            clear()
        }
        super.onDestroy()
    }

}