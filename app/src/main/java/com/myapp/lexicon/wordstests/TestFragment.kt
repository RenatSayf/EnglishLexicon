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
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.widget.RxTextView
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.billing.BillingViewModel
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.databinding.TestFragmentBinding
import com.myapp.lexicon.dialogs.DictListDialog
import com.myapp.lexicon.helpers.Keyboard
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.UiState
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.main.SpeechViewModel
import com.myapp.lexicon.viewmodels.AnimViewModel
import com.myapp.lexicon.viewmodels.PageBackViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class TestFragment : Fragment(R.layout.test_fragment), DictListDialog.ISelectItemListener, DialogWarning.IDialogResult,
    DialogTestComplete.IDialogComplete_Result
{

    companion object
    {
        fun newInstance() = TestFragment()
    }

    private lateinit var binding: TestFragmentBinding
    private lateinit var mActivity: MainActivity
    private val testVM: TestViewModel by viewModels()
    private val animVM: AnimViewModel by viewModels()
    private val speechVM: SpeechViewModel by viewModels()
    private val pageBackVM: PageBackViewModel by viewModels()
    private val billingVM: BillingViewModel by viewModels()
    private val adsVM: AdsViewModel by viewModels()
    private val composite = CompositeDisposable()
    private var completeDialog: DialogTestComplete? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity
        mActivity.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
            override fun handleOnBackPressed()
            {
                mActivity.supportFragmentManager.popBackStack()
            }
        })
    }

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

        billingVM.noAdsToken.observe(viewLifecycleOwner, { t ->
            t?.let {
                if (t.isEmpty()) adsVM.loadAd1()
            }
        })

        pageBackVM.imageBack.observe(viewLifecycleOwner, { img ->
            binding.imgBack.setImageResource(img)
        })

        testVM.currentWord.observe(viewLifecycleOwner, {
            binding.btnViewDict.text = it.dictName
        })

        testVM.wordsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty())
            {
                binding.btnViewDict.text = testVM.wordsList.value!![0].dictName
                binding.enWordTV.text = testVM.wordsList.value!![0].english
                binding.enWordTV.apply {
                    scaleX = 1f
                    scaleY = 1f
                }
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
                        testVM.rightAnswerCounter++
                        Keyboard.getInstance().forceHide(requireContext(), binding.editTextView)
                        binding.checkBtn.setBackgroundResource(R.drawable.text_btn_for_test_green)
                        binding.ruWordTV.animate().scaleX(1f).scaleY(1f).apply {
                            duration = 500
                            interpolator = AnticipateOvershootInterpolator()
                            startDelay = 0
                            animIncreaseScaleListener(this)
                        }.start()
                        Toast.makeText(requireContext(), getString(R.string.text_is_right), Toast.LENGTH_LONG).apply {
                            setGravity(Gravity.TOP, 0, 0)
                        }.show()
                    }
                    else ->
                    {
                        testVM.rightAnswerCounter--
                        Toast.makeText(requireContext(), getString(R.string.text_wrong), Toast.LENGTH_LONG).apply {
                            setGravity(Gravity.TOP, 0, 0)
                        }.show()
                        val animNotRight = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_not_right)
                        animNotRight.setAnimationListener(object : Animation.AnimationListener
                        {
                            override fun onAnimationStart(p0: Animation?)
                            {
                                binding.checkBtn.setBackgroundResource(R.drawable.text_btn_for_test_red)
                            }

                            override fun onAnimationEnd(p0: Animation?)
                            {
                                binding.checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                                adsVM.showAd1(requireActivity())
                            }

                            override fun onAnimationRepeat(p0: Animation?){}
                        })
                        binding.checkBtn.startAnimation(animNotRight)
                    }
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
                is UiState.AnimEnded, is UiState.AnimCanceled ->
                {
                    binding.checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                    LockOrientation(requireActivity()).unLock()
                }
                else ->
                {
                    binding.checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
                    LockOrientation(requireActivity()).unLock()
                }
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
            val shuffledList = testVM.wordsList.value?.shuffled()
            var subList: List<Word> = arrayListOf()
            if (shuffledList != null && shuffledList.size > 4)
            {
                subList = shuffledList.subList(0, 4)
            }
            else if (shuffledList != null)
            {
                subList = shuffledList
            }
            val enWord = binding.enWordTV.text.toString()
            val ruWord = binding.ruWordTV.text.toString()
            val dictName = binding.btnViewDict.text.toString()
            val targetWord = Word(-1, dictName, enWord, ruWord, 1)
            if (!subList.isNullOrEmpty())
            {
                val hintDialog = HintDialogFragment.newInstance(targetWord, subList.toMutableList())
                hintDialog.show(parentFragmentManager, HintDialogFragment.TAG)
                hintDialog.selectedItem.observe(viewLifecycleOwner, {
                    binding.editTextView.setText(it)
                })
            }
        }

        binding.btnViewDict.setOnClickListener {
            val dictList = testVM.dictList.value?.toMutableList()
            dictList?.let {
                val dictsDialog = DictListDialog.getInstance(it, this)
                dictsDialog.show(parentFragmentManager, DictListDialog.TAG)
                dictsDialog.selectedItem.observe(viewLifecycleOwner, { dict ->
                    if (dict.isNotEmpty())
                    {
                        testVM.getWordsByDictName(dict)
                    }
                })
            }
        }

        binding.speakerBtnView.setOnClickListener {
            val text = binding.enWordTV.text.toString()
            speechVM.doSpeech(text, Locale.US)
        }

        binding.editTextView.onItemClickListener = object : AdapterView.OnItemClickListener
        {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long)
            {
                Keyboard.getInstance().forceHide(requireContext(), binding.editTextView)
            }
        }


    }

    override fun onStart()
    {
        super.onStart()
        testVM.liveState.observe(viewLifecycleOwner, {
            when(it)
            {
                is UiState.Initial ->
                {
                    testVM.setLiveState(UiState.Active())
                    val wordIds = testVM.getWordIdsFromPref()
                    if (wordIds.isNotEmpty())
                    {
                        DialogWarning().apply {
                            setListener(this@TestFragment)
                        }.show(requireActivity().supportFragmentManager.beginTransaction(), DialogWarning.DIALOG_TAG)
                    }
                    else
                    {
                        val dictName = testVM.currentWord.value?.dictName
                        dictName?.let { dict ->
                            testVM.getWordsByDictName(dict)
                        }
                    }
                }
                is UiState.Active -> {}
                else -> {}
            }
        })

    }

    override fun onDestroyView()
    {
        testVM.wordsList.value?.let { list ->
            if (list.isNotEmpty())
            {
                val count = testVM.wordsCount.value
                count?.let {
                    if (it.minus(list.size) > 5)  testVM.saveWordIdsToPref(list)
                }
            }
        }
        adsVM.showAd1(requireActivity())
        super.onDestroyView()
    }

    // TODO ViewPropertyAnimation.Scale Step 6 слушатель анимации
    private fun animIncreaseScaleListener(animator: ViewPropertyAnimator)
    {
        animator.setListener(object : Animator.AnimatorListener
        {
            override fun onAnimationStart(p0: Animator?)
            {
                animVM.setAnimState(UiState.AnimStarted)
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
                binding.checkBtn.setBackgroundResource(R.drawable.text_button_for_test)
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
                    Toast.makeText(requireContext(), getString(R.string.text_test_completed), Toast.LENGTH_SHORT).apply {
                        setGravity(Gravity.TOP, 0, 0)
                    }.show()

                    val total = testVM.wordsCount.value!!
                    val correctly = testVM.rightAnswerCounter
                    if (completeDialog == null)
                    {
                        completeDialog = DialogTestComplete().apply {
                            arguments = Bundle().apply {
                                putInt(DialogTestComplete.TOTAL_NUM, total)
                                putInt(DialogTestComplete.CORRECTLY_NUM, correctly)
                            }
                            setListener(this@TestFragment)
                        }
                        completeDialog?.show(requireActivity().supportFragmentManager, DialogTestComplete.TAG)
                    }
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

    override fun dictListItemOnSelected(dict: String)
    {

    }

    override fun dialogListener(result: Boolean)
    {
        if (result)
        {
            val wordIds = testVM.getWordIdsFromPref()
            testVM.getWordsByIds(wordIds)
        }
        else
        {
            testVM.saveWordIdsToPref(arrayListOf())
            val dict = testVM.currentWord.value?.dictName
            dict?.let {
                testVM.getWordsByDictName(it)
            } ?: run {
                val dictList = testVM.dictList.value
                if (dictList != null)
                {
                    if (dictList.isNotEmpty())
                    testVM.getWordsByDictName(dictList[0])
                }
            }
        }
    }

    override fun dialogCompleteResult(res: Int)
    {
        when(res)
        {
            1 ->
            {
                val dictList = testVM.dictList.value?.toMutableList()
                dictList?.let {
                    val dictsDialog = DictListDialog.getInstance(it, this)
                    dictsDialog.show(parentFragmentManager, DictListDialog.TAG)
                    dictsDialog.selectedItem.observe(viewLifecycleOwner, { dict ->
                        if (dict.isNotEmpty())
                        {
                            testVM.getWordsByDictName(dict)
                        }
                    })
                }
            }
            -1 ->
            {
                requireActivity().onBackPressed()
            }
            else ->
            {
                val dict = binding.btnViewDict.text.toString()
                if (dict.isNotEmpty())
                {
                    testVM.getWordsByDictName(dict)
                }
            }
        }
    }

}