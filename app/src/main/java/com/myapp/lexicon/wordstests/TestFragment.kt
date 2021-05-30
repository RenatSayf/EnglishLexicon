package com.myapp.lexicon.wordstests

import android.Manifest
import android.animation.Animator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.widget.RxTextView
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.databinding.TestFragmentBinding
import com.myapp.lexicon.helpers.Keyboard
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.UiState
import com.myapp.lexicon.viewmodels.AnimViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.util.*
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

        testVM.resetRight()
        testVM.isRight.observe(viewLifecycleOwner, { isRight ->
            isRight?.let {
                when(it)
                {
                    true ->
                    {
                        LockOrientation(requireActivity()).lock()
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

        binding.checkBtn.setOnClickListener {
            val translateText = binding.editTextView.text.toString()
            if (translateText.isNotEmpty())
            {
                val enText = binding.enWordTV.text.toString()
                val searchedWord = Word(-1, "", enText, translateText, 1)
                testVM.searchWord(searchedWord)
            }
        }

        binding.clearBtnView.setOnClickListener {
            binding.editTextView.text.clear()
        }

        animVM.apply {
            uiState.observe(viewLifecycleOwner, { state ->
                if (state is UiState.TextViewCreated)
                {
                    binding.ruWordTV.apply {
                        scaleX = state.scaleX
                        scaleY = state.scaleY
                    }
                }
                else if (state is UiState.TextViewAfterAnim)
                {
                    binding.ruWordTV.apply {
                        scaleX = state.scaleX
                        scaleY = state.scaleY
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

        animVM.animState.observe(viewLifecycleOwner, {
            when (it)
            {
                is UiState.AnimStarted -> LockOrientation(requireActivity()).lock()
                is UiState.AnimEnded, is UiState.AnimCanceled -> LockOrientation(requireActivity()).unLock()
                else -> LockOrientation(requireActivity()).unLock()
            }
        })

        binding.microphoneBtnView.setOnClickListener {
            // TODO Runtime permission Step 4
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                recordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
                return@setOnClickListener
            }
            runSpeechRecognizer()
        }

        binding.selectVariantBtn.setOnClickListener {

        }

    }

    // TODO ViewPropertyAnimation.Scale Step 6 слушатель анимации
    private fun animIncreaseScaleListener(animator: ViewPropertyAnimator)
    {
        animator.setListener(object : Animator.AnimatorListener
        {
            override fun onAnimationStart(p0: Animator?)
            {
                animVM.setAnimState(UiState.AnimStarted())
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
                        setListener(object : Animator.AnimatorListener
                        {
                            override fun onAnimationStart(p0: Animator?)
                            {}

                            override fun onAnimationEnd(p0: Animator?)
                            {
                                animVM.setAnimState(UiState.AnimEnded())
                            }

                            override fun onAnimationCancel(p0: Animator?)
                            {
                                animVM.setAnimState(UiState.AnimCanceled())
                            }

                            override fun onAnimationRepeat(p0: Animator?)
                            {}

                        })
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

    // TODO Runtime permission Step 3
    private val recordAudioPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) Snackbar.make(binding.mainScroll, getString(R.string.text_is_granted), Snackbar.LENGTH_LONG).show()
        return@registerForActivityResult
    }

    //TODO Speech Recognizer Step 3 Готовим intent
    private fun runSpeechRecognizer()
    {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            action = RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите в микрофон...")
        }
        speechRecognizer.launch(intent)
    }

    //TODO Speech Recognizer Step 4 Получение результата распознавания
    private val speechRecognizer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        val data = result.data
        data?.let {
            val listExtra = it.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!listExtra.isNullOrEmpty())
            {
                val dict = binding.btnViewDict.text.toString()
                testVM.getAllSimilarWords(dict, "%${listExtra[0]}%")
                binding.editTextView.showDropDown()
            }
        }
        return@registerForActivityResult
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