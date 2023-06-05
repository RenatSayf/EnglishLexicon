package com.myapp.lexicon.wordstests

import android.Manifest
import android.animation.Animator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.widget.RxTextView
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.showInterstitial
import com.myapp.lexicon.databinding.TestFragmentBinding
import com.myapp.lexicon.dialogs.DictListDialog
import com.myapp.lexicon.helpers.*
import com.myapp.lexicon.main.SpeechViewModel
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.adsIsEnabled
import com.myapp.lexicon.settings.getTestStateFromPref
import com.myapp.lexicon.settings.saveTestStateToPref
import com.myapp.lexicon.viewmodels.AnimViewModel
import com.myapp.lexicon.viewmodels.PageBackViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class TestFragment : Fragment(R.layout.test_fragment), DictListDialog.ISelectItemListener,
    DialogWarning.Listener,
    DialogTestComplete.Listener
{
    companion object
    {
        fun newInstance() = TestFragment()
    }

    private lateinit var binding: TestFragmentBinding
    private val testVM: TestViewModel by lazy {
        ViewModelProvider(this)[TestViewModel::class.java]
    }
    private val animVM: AnimViewModel by viewModels()
    private val speechVM: SpeechViewModel by viewModels()
    private val pageBackVM: PageBackViewModel by viewModels()
    private val composite = CompositeDisposable()
    private var dialogWarning: DialogWarning? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    @Suppress("ObjectLiteralToLambda")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = TestFragmentBinding.bind(view)

        pageBackVM.imageBack.observe(viewLifecycleOwner) { img ->
            binding.imgBack.setImageResource(img)
        }

        testVM.currentWord.observe(viewLifecycleOwner) {
            binding.btnViewDict.text = it.dictName
            testVM.getWordsByDictName(it.dictName)
        }

        testVM.wordsList.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                binding.btnViewDict.text = list[0].dictName
                binding.tvTargetWord.text = list[0].translate
                binding.tvTargetWord.apply {
                    tag = list[0]._id
                    scaleX = 1f
                    scaleY = 1f
                }
                binding.tvTranslateWord.text = list[0].english
                binding.progressBar.apply {
                    max = list.size
                    progress = testVM.testState.progress
                }
            }
        }

        testVM.resetRight()
        testVM.isRight.observe(viewLifecycleOwner) { isRight ->
            isRight?.let {
                when (it) {
                    true -> {
                        LockOrientation(requireActivity()).lock()
                        testVM.testState.rightAnswers++
                        binding.editTextView.hideKeyboard()
                        binding.checkBtn.setBackgroundResource(R.drawable.text_btn_for_test_green)
                        binding.tvTranslateWord.animate().scaleX(1f).scaleY(1f).apply {
                            duration = 500
                            interpolator = AnticipateOvershootInterpolator()
                            startDelay = 0
                            animIncreaseScaleListener(this)
                        }.start()

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.text_is_right),
                            Toast.LENGTH_LONG
                        ).apply {
                            setGravity(Gravity.TOP, 0, 0)
                        }.show()
                    }
                    else -> {
                        testVM.testState.rightAnswers--
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.text_wrong),
                            Toast.LENGTH_LONG
                        ).apply {
                            setGravity(Gravity.TOP, 0, 0)
                        }.show()

                        val animNotRight = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_not_right)
                        animNotRight.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) {
                                binding.checkBtn.setBackgroundResource(R.drawable.text_btn_for_test_red)
                            }

                            override fun onAnimationEnd(p0: Animation?) {
                                binding.checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                            }

                            override fun onAnimationRepeat(p0: Animation?) {}
                        })
                        binding.checkBtn.startAnimation(animNotRight)
                    }
                }
            }
        }

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

        testVM.similarWords.observe(viewLifecycleOwner) { words ->
            if (words.isNotEmpty()) {
                val translates = arrayListOf<String>()
                words.forEach {
                    translates.add(it.english)
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    translates
                )
                binding.editTextView.apply {
                    setAdapter(adapter)
                }
            }
        }

        binding.checkBtn.setOnClickListener {
            val translateText = binding.editTextView.text.toString()
            if (translateText.isNotEmpty())
            {
                val enText = binding.tvTargetWord.text.toString()
                val searchedWord = Word(-1, "", translateText, enText, 1)
                testVM.searchWord(searchedWord)
                val wordId = binding.tvTargetWord.tag.toString().toInt()
                testVM.testState.studiedWordIds.add(wordId)
            }
        }

        binding.clearBtnView.setOnClickListener {
            binding.editTextView.text.clear()
        }

        animVM.apply {
            uiState.observe(viewLifecycleOwner) { state ->
                if (state is UiState.TextViewCreated) {
                    binding.tvTranslateWord.apply {
                        scaleX = state.scaleX
                        scaleY = state.scaleY
                    }
                } else if (state is UiState.TextViewAfterAnim) {
                    binding.tvTranslateWord.apply {
                        scaleX = state.scaleX
                        scaleY = state.scaleY
                    }
                }
            }
        }

        testVM.wordsCount.observe(viewLifecycleOwner) {
            binding.progressBar.max = it
            val progressValue = "1 / $it"
            binding.progressValueTV.text = progressValue
        }

        testVM.wordIndex.observe(viewLifecycleOwner) {
            binding.progressBar.progress = it
            val progressValue = "$it / ${testVM.wordsCount.value}"
            binding.progressValueTV.text = progressValue
        }

        animVM.animState.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.AnimStarted -> LockOrientation(requireActivity()).lock()
                is UiState.AnimEnded, is UiState.AnimCanceled -> {
                    binding.checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                    LockOrientation(requireActivity()).unLock()
                }
                else -> {
                    binding.checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                    LockOrientation(requireActivity()).unLock()
                }
            }
        }

        binding.microphoneBtnView.setOnClickListener {
            // TODO Runtime permission Step 4
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                recordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
                return@setOnClickListener
            }
            speechRecognize(Locale.US)
        }

        binding.selectVariantBtn.setOnClickListener {

            val id = binding.tvTargetWord.tag.toString().toInt()
            val enWord = binding.tvTargetWord.text.toString()
            val ruWord = binding.tvTranslateWord.text.toString()
            val dictName = binding.btnViewDict.text.toString()
            val targetWord = Word(id, dictName, ruWord, enWord, 1)

            val shuffledList = testVM.wordsList.value?.shuffled()?.toMutableList()
            if (!shuffledList.isNullOrEmpty() && shuffledList.size > 1) {
                shuffledList.remove(targetWord)
            }
            val subList = try {
                shuffledList?.subList(0, 3)
            } catch (e: IndexOutOfBoundsException) {
                shuffledList
            }

            if (!subList.isNullOrEmpty())
            {
                val hintDialog = HintDialogFragment.newInstance(
                    targetWord,
                    subList.toMutableList(),
                    onItemSelected = { item ->
                        binding.editTextView.setText(item)
                    }
                )
                hintDialog.show(parentFragmentManager, HintDialogFragment.TAG)
            }
        }

        binding.btnViewDict.setOnClickListener {
            val dictList = testVM.dictList.value?.toMutableList()
            dictList?.let {
                val dictsDialog = DictListDialog.getInstance(it, this)
                dictsDialog.show(parentFragmentManager, DictListDialog.TAG)
                dictsDialog.selectedItem.observe(viewLifecycleOwner) { dict ->
                    if (dict.isNotEmpty()) {
                        testVM.getWordsByDictName(dict)
                    }
                }
            }
        }

        binding.speakerBtnView.setOnClickListener {
            val text = binding.editTextView.text.toString()
            if (text.isNotEmpty()) {
                speechVM.doSpeech(text, Locale.US)
            }
        }

        binding.editTextView.onItemClickListener = object : AdapterView.OnItemClickListener
        {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long)
            {
                binding.editTextView.hideKeyboard()
            }
        }


    }

    override fun onStart()
    {
        super.onStart()

        this.getTestStateFromPref(
            onInit = {
                val dictName = testVM.currentWord.value?.dictName
                dictName?.let { dict ->
                    testVM.getWordsByDictName(dict)
                }
            },
            onSuccess = {
                if (dialogWarning == null) {
                    dialogWarning = DialogWarning().apply {
                        setListener(this@TestFragment)
                    }
                    dialogWarning?.show(
                        parentFragmentManager.beginTransaction(),
                        DialogWarning.DIALOG_TAG
                    )
                }
            },
            onError = { err ->
                Throwable(err).printStackTrace()
            }
        )
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onPause() {

        val testState = testVM.testState.apply {
            dict = binding.btnViewDict.text.toString()
            wordId = binding.tvTargetWord.tag.toString().toInt()
            progress = binding.progressBar.progress
            progressMax = binding.progressBar.max
        }
        if (BuildConfig.DEBUG) {
            when {
                testState.studiedWordIds.size > 1 -> {
                    this.saveTestStateToPref(testState)
                }
            }
        }
        else {
            when {
                testState.studiedWordIds.size > 5 -> {
                    this.saveTestStateToPref(testState)
                }
            }
        }

        super.onPause()
    }

    // TODO ViewPropertyAnimation.Scale Step 6 слушатель анимации
    private fun animIncreaseScaleListener(animator: ViewPropertyAnimator)
    {
        animator.setListener(object : Animator.AnimatorListener
        {
            override fun onAnimationStart(p0: Animator)
            {
                animVM.setAnimState(UiState.AnimStarted)
            }

            override fun onAnimationEnd(p0: Animator)
            {
                binding.tvTargetWord.animate().scaleX(0f).scaleY(0f).apply {
                    duration = 500
                    interpolator = AnticipateOvershootInterpolator()
                    startDelay = 1000
                }.start()

                binding.tvTranslateWord.animate().scaleX(0f).scaleY(0f).apply {
                    duration = 500
                    interpolator = AnticipateOvershootInterpolator()
                    animDecreaseScaleListener(this)
                    startDelay = 1000
                }.start()
            }

            override fun onAnimationCancel(p0: Animator)
            {
                binding.checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
            }

            override fun onAnimationRepeat(p0: Animator)
            {}
        })
    }

    private fun animDecreaseScaleListener(animator: ViewPropertyAnimator)
    {
        animator.setListener(object : Animator.AnimatorListener
        {
            override fun onAnimationStart(p0: Animator)
            {}

            override fun onAnimationEnd(p0: Animator)
            {
                binding.editTextView.apply {
                    text.clear()
                    clearFocus()
                }
                val nextWord = testVM.getNextWords()
                nextWord?.let {
                    binding.tvTargetWord.text = nextWord.translate
                    binding.tvTargetWord.tag = nextWord._id
                    binding.tvTranslateWord.text = nextWord.english
                    binding.tvTargetWord.animate().scaleX(1f).scaleY(1f).apply {
                        duration = 300
                        interpolator = AccelerateInterpolator()
                        setListener(object : Animator.AnimatorListener
                        {
                            override fun onAnimationStart(p0: Animator)
                            {}

                            override fun onAnimationEnd(p0: Animator)
                            {
                                animVM.setAnimState(UiState.AnimEnded())
                            }

                            override fun onAnimationCancel(p0: Animator)
                            {
                                animVM.setAnimState(UiState.AnimCanceled())
                            }

                            override fun onAnimationRepeat(p0: Animator)
                            {}

                        })
                        startDelay = 0
                    }
                } ?: run {
                    Toast.makeText(requireContext(), getString(R.string.text_test_completed), Toast.LENGTH_SHORT).apply {
                        setGravity(Gravity.TOP, 0, 0)
                    }.show()

                    binding.checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                    DialogTestComplete.getInstance(
                        testVM.testState.rightAnswers,
                        testVM.testState.progressMax,
                        this@TestFragment
                    ).show(parentFragmentManager, DialogTestComplete.TAG)
                }
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {}
        })
    }

    // TODO Runtime permission Step 3
    private val recordAudioPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) Snackbar.make(binding.mainScroll, getString(R.string.text_is_granted), Snackbar.LENGTH_LONG).show()
        return@registerForActivityResult
    }

    //TODO Speech Recognizer Step 3 Готовим intent
    @Suppress("SameParameterValue")
    private fun speechRecognize(locale: Locale)
    {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            action = RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.text_do_speak))
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

    override fun onDetach() {

        if (this.adsIsEnabled) {
            this.showInterstitial()
        }
        super.onDetach()
    }

    override fun dictListItemOnSelected(dict: String)
    {

    }

    override fun onTestCompleteClick() {
        testVM.testState.reset()
        parentFragmentManager.popBackStack()
    }

    override fun onTestRepeatClick() {
        testVM.testState.reset()
        val dict = binding.btnViewDict.text.toString()
        if (dict.isNotEmpty())
        {
            testVM.getWordsByDictName(dict)
        }
    }

    override fun onNextTestClick() {
        testVM.testState.reset()
        val dictList = testVM.dictList.value?.toMutableList()
        dictList?.let {
            val dictsDialog = DictListDialog.getInstance(it, this)
            dictsDialog.show(parentFragmentManager, DictListDialog.TAG)
            dictsDialog.selectedItem.observe(viewLifecycleOwner) { dict ->
                if (dict.isNotEmpty()) {
                    testVM.getWordsByDictName(dict)
                }
            }
        }
    }

    override fun onPositiveClick() {
        this.getTestStateFromPref(
            onSuccess = { state ->
                testVM.testState = state
                testVM.getWordsByDictName(state.dict)
            },
            onError = { err ->
                showSnackBar(err?: "Unknown error")
                Throwable(err).printStackTrace()
            }
        )
    }

    override fun onNegativeClick() {
        testVM.testState.reset()
        this.saveTestStateToPref(null)
    }

}