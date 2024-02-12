package com.myapp.lexicon.wordstests

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxbinding2.widget.RxTextView
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.ads.InterstitialAdIds
import com.myapp.lexicon.ads.RevenueViewModel
import com.myapp.lexicon.ads.showAd
import com.myapp.lexicon.databinding.TestFragmentBinding
import com.myapp.lexicon.dialogs.DictListDialog
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.UiState
import com.myapp.lexicon.helpers.hideKeyboard
import com.myapp.lexicon.helpers.showCustomSnackBar
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.main.SpeechViewModel
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.getTestStateFromPref
import com.myapp.lexicon.settings.saveTestStateToPref
import com.myapp.lexicon.viewmodels.AnimViewModel
import com.myapp.lexicon.viewmodels.PageBackViewModel
import com.yandex.mobile.ads.interstitial.InterstitialAd
import io.reactivex.disposables.CompositeDisposable
import java.util.Locale
import java.util.concurrent.TimeUnit


class TestFragment : Fragment(R.layout.test_fragment), DictListDialog.ISelectItemListener,
    DialogWarning.Listener,
    DialogTestComplete.Listener
{
    companion object
    {
        val TAG = "${TestFragment::class.java.simpleName}.TAG"
        fun newInstance(): TestFragment {
            return TestFragment()
        }
    }

    private lateinit var binding: TestFragmentBinding

    private val testVM: TestViewModel by lazy {
        val factory = TestViewModel.Factory(requireActivity().application)
        ViewModelProvider(this, factory)[TestViewModel::class.java]
    }
    private val animVM: AnimViewModel by viewModels()

    private val speechVM: SpeechViewModel by lazy {
        val factory = SpeechViewModel.Factory(requireActivity().application)
        ViewModelProvider(this, factory)[SpeechViewModel::class.java]
    }
    private val pageBackVM: PageBackViewModel by lazy {
        ViewModelProvider(this)[PageBackViewModel::class.java]
    }
    private val adsVM: AdsViewModel by viewModels()

    private val revenueVM: RevenueViewModel by activityViewModels()

    private val composite = CompositeDisposable()
    private var dialogWarning: DialogWarning? = null
    private var interstitialAd: InterstitialAd? = null
    private val lockOrientation: LockOrientation by lazy {
        LockOrientation(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = TestFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("ObjectLiteralToLambda")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        lockOrientation.lock()

        with(binding) {
            if (savedInstanceState == null) {
                this@TestFragment.getTestStateFromPref(
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

            pageBackVM.imageBack.observe(viewLifecycleOwner) { img ->
                imgBack.setImageResource(img)
            }

            testVM.currentWord.observe(viewLifecycleOwner) {
                btnViewDict.text = it.dictName
            }

            testVM.wordsList.observe(viewLifecycleOwner) { list ->
                if (list.isNotEmpty()) {
                    btnViewDict.text = list[0].dictName
                    tvTargetWord.text = list[0].translate
                    tvTargetWord.apply {
                        tag = list[0]._id
                        scaleX = 1f
                        scaleY = 1f
                    }
                    tvTranslateWord.text = list[0].english
                    progressBar.apply {
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
                            testVM.testState.rightAnswers++
                            editTextView.hideKeyboard()
                            checkBtn.setBackgroundResource(R.drawable.text_btn_for_test_green)
                            tvTranslateWord.animate().scaleX(1f).scaleY(1f).apply {
                                duration = 500
                                interpolator = AnticipateOvershootInterpolator()
                                startDelay = 0
                                animIncreaseScaleListener(this)
                            }.start()
                            root.showCustomSnackBar(
                                getString(R.string.text_is_right)
                            )
                        }

                        else -> {
                            testVM.testState.rightAnswers--
                            root.showCustomSnackBar(
                                message = getString(R.string.text_wrong),
                                onLaunch = { b ->
                                    b.tvThumbsUp.visibility = View.GONE
                                    b.tvSmileyFace.text = getString(R.string.confused_face)
                                }
                            )

                            val animNotRight = AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.anim_not_right
                            )
                            animNotRight.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(p0: Animation?) {
                                    checkBtn.setBackgroundResource(R.drawable.text_btn_for_test_red)
                                }

                                override fun onAnimationEnd(p0: Animation?) {
                                    checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                                }

                                override fun onAnimationRepeat(p0: Animation?) {}
                            })
                            checkBtn.startAnimation(animNotRight)
                        }
                    }
                }
            }

            composite.add(
                RxTextView.textChanges(editTextView)
                    .debounce(250, TimeUnit.MILLISECONDS)
                    .subscribe({ chars ->
                        if (chars.isNotEmpty()) {
                            val dict = btnViewDict.text.toString()
                            testVM.getAllSimilarWords(dict, "%${chars}%")
                        }
                    }, { t ->
                        t.printStackTrace()
                    })
            )

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
                    editTextView.apply {
                        setAdapter(adapter)
                    }
                }
            }

            checkBtn.setOnClickListener {
                val translateText = editTextView.text.toString()
                if (translateText.isNotEmpty()) {
                    val enText = tvTargetWord.text.toString()
                    val searchedWord = Word(-1, "", translateText, enText, 1)
                    testVM.searchWord(searchedWord)
                    val wordId = tvTargetWord.tag.toString().toInt()
                    testVM.testState.studiedWordIds.add(wordId)
                }
            }

            clearBtnView.setOnClickListener {
                editTextView.text.clear()
            }

            animVM.apply {
                uiState.observe(viewLifecycleOwner) { state ->
                    if (state is UiState.TextViewCreated) {
                        tvTranslateWord.apply {
                            scaleX = state.scaleX
                            scaleY = state.scaleY
                        }
                    } else if (state is UiState.TextViewAfterAnim) {
                        tvTranslateWord.apply {
                            scaleX = state.scaleX
                            scaleY = state.scaleY
                        }
                    }
                }
            }

            testVM.wordsCount.observe(viewLifecycleOwner) {
                progressBar.max = it
                val progressValue = "1 / $it"
                progressValueTV.text = progressValue
            }

            testVM.wordIndex.observe(viewLifecycleOwner) {
                progressBar.progress = it
                val progressValue = "$it / ${testVM.wordsCount.value}"
                progressValueTV.text = progressValue
            }

            adsVM.interstitialAd.observe(viewLifecycleOwner) { result ->
                result.onSuccess { ad ->
                    interstitialAd = ad
                }
            }

            testVM.state.observe(viewLifecycleOwner) { state ->
                when (state) {
                    TestViewModel.State.Init -> {
                        adsVM.loadInterstitialAd(InterstitialAdIds.values().randomOrNull())
                    }

                    TestViewModel.State.NotShowAd -> {}
                    TestViewModel.State.ShowAd -> {
                        interstitialAd?.showAd(
                            requireActivity(),
                            onImpression = { data ->
                                if (data != null) {
                                    revenueVM.updateUserRevenueIntoCloud(data)
                                }
                                testVM.setState(TestViewModel.State.Init)
                            }
                        )
                    }

                    else -> {}
                }
            }

            animVM.animState.observe(viewLifecycleOwner) {
                when (it) {
                    is UiState.AnimStarted -> {}
                    is UiState.AnimEnded, is UiState.AnimCanceled -> {
                        checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                    }

                    else -> {
                        checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                    }
                }
            }

            microphoneBtnView.setOnClickListener {
                speechRecognize(Locale.US)
            }

            selectVariantBtn.setOnClickListener {

                val id = tvTargetWord.tag.toString().toInt()
                val enWord = tvTargetWord.text.toString()
                val ruWord = tvTranslateWord.text.toString()
                val dictName = btnViewDict.text.toString()
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

                if (!subList.isNullOrEmpty()) {
                    val hintDialog = HintDialogFragment.newInstance(
                        targetWord,
                        subList.toMutableList(),
                        onItemSelected = { item ->
                            editTextView.setText(item)
                        }
                    )
                    hintDialog.show(parentFragmentManager, HintDialogFragment.TAG)
                }
            }

            btnViewDict.setOnClickListener {
                val dictList = testVM.dictList.value?.toMutableList()
                dictList?.let {
                    val dictsDialog = DictListDialog.getInstance(it, this@TestFragment)
                    dictsDialog.show(parentFragmentManager, DictListDialog.TAG)
                    dictsDialog.selectedItem.observe(viewLifecycleOwner) { dict ->
                        if (dict.isNotEmpty()) {
                            testVM.getWordsByDictName(dict)
                        }
                    }
                }
            }

            speakerBtnView.setOnClickListener {
                val text = editTextView.text.toString()
                if (text.isNotEmpty()) {
                    speechVM.doSpeech(text, Locale.US)
                }
            }

            editTextView.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    editTextView.hideKeyboard()
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()

        with(binding){
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        parentFragmentManager.popBackStack()
                    }
                })
            toolBar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    // TODO ViewPropertyAnimation.Scale Step 6 слушатель анимации
    private fun animIncreaseScaleListener(animator: ViewPropertyAnimator)
    {
        with(binding){
            animator.setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {
                    animVM.setAnimState(UiState.AnimStarted)
                }

                override fun onAnimationEnd(p0: Animator) {
                    tvTargetWord.animate().scaleX(0f).scaleY(0f).apply {
                        duration = 500
                        interpolator = AnticipateOvershootInterpolator()
                        startDelay = 1000
                    }.start()

                    tvTranslateWord.animate().scaleX(0f).scaleY(0f).apply {
                        duration = 500
                        interpolator = AnticipateOvershootInterpolator()
                        animDecreaseScaleListener(this)
                        startDelay = 1000
                    }.start()
                }

                override fun onAnimationCancel(p0: Animator) {
                    checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                }

                override fun onAnimationRepeat(p0: Animator) {}
            })
        }
    }

    private fun animDecreaseScaleListener(animator: ViewPropertyAnimator)
    {
        with(binding){
            animator.setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}

                override fun onAnimationEnd(p0: Animator) {
                    editTextView.apply {
                        text.clear()
                        clearFocus()
                    }
                    val nextWord = testVM.getNextWords()
                    nextWord?.let {
                        tvTargetWord.text = nextWord.translate
                        tvTargetWord.tag = nextWord._id
                        tvTranslateWord.text = nextWord.english
                        tvTargetWord.animate().scaleX(1f).scaleY(1f).apply {
                            duration = 300
                            interpolator = AccelerateInterpolator()
                            setListener(object : Animator.AnimatorListener {
                                override fun onAnimationStart(p0: Animator) {}

                                override fun onAnimationEnd(p0: Animator) {
                                    animVM.setAnimState(UiState.AnimEnded())
                                }

                                override fun onAnimationCancel(p0: Animator) {
                                    animVM.setAnimState(UiState.AnimCanceled())
                                }

                                override fun onAnimationRepeat(p0: Animator) {}

                            })
                            startDelay = 0
                        }
                    } ?: run {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.text_test_completed),
                            Toast.LENGTH_SHORT
                        ).apply {
                            setGravity(Gravity.TOP, 0, 0)
                        }.show()

                        checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
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
    }

    //TODO Speech Recognizer Step 1 Готовим intent
    @Suppress("SameParameterValue")
    private fun speechRecognize(locale: Locale)
    {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            action = RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.text_do_speak))
        }
        speechRecognizer.launch(intent)
    }

    //TODO Speech Recognizer Step 2 Получение результата распознавания
    private val speechRecognizer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        with(binding){
            val data = result.data
            data?.let {
                val listExtra = it.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!listExtra.isNullOrEmpty()) {
                    val dict = btnViewDict.text.toString()
                    testVM.getAllSimilarWords(dict, "%${listExtra[0]}%")
                    editTextView.showDropDown()
                }
            }
        }
        return@registerForActivityResult
    }

    override fun onDestroyView() {

        lockOrientation.unLock()
        super.onDestroyView()
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

        with(binding){
            val testState = testVM.testState.apply {
                dict = btnViewDict.text.toString()
                wordId = tvTargetWord.tag.toString().toInt()
                progress = progressBar.progress
                progressMax = progressBar.max
            }
            if (BuildConfig.DEBUG) {
                when {
                    testState.studiedWordIds.size > 1 -> {
                        this@TestFragment.saveTestStateToPref(testState)
                    }
                }
            } else {
                when {
                    testState.studiedWordIds.size > 5 -> {
                        this@TestFragment.saveTestStateToPref(testState)
                    }
                }
            }
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
        with(binding){
            testVM.testState.reset()
            val dict = btnViewDict.text.toString()
            if (dict.isNotEmpty()) {
                testVM.getWordsByDictName(dict)
            }
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