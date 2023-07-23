@file:Suppress("ObjectLiteralToLambda", "UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.service

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CompoundButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.STestModalFragmentBinding
import com.myapp.lexicon.helpers.RandomNumberGenerator
import com.myapp.lexicon.helpers.showToast
import com.myapp.lexicon.interfaces.IModalFragment
import com.myapp.lexicon.main.MainViewModel
import com.myapp.lexicon.main.SpeechViewModel
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.main.viewmodels.UserViewModel.State.ReceivedUserData
import com.myapp.lexicon.main.viewmodels.UserViewModel.State.RevenueUpdated
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.models.toWordList
import com.myapp.lexicon.settings.AppSettings
import com.myapp.lexicon.settings.disablePassiveWordsRepeat
import com.myapp.lexicon.settings.getExchangeRateFromPref
import com.myapp.lexicon.settings.isUserRegistered
import com.myapp.lexicon.settings.saveWordToPref
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class TestModeDialog : DialogFragment() {

    companion object {
        val TAG = "${TestModeDialog::class.java.simpleName}.TAG"
        private var json: String = ""
        private var counters: List<Int> = listOf()
        private var listener: IModalFragment? = null

        fun newInstance(json: String, counters: List<Int>, listener: IModalFragment): TestModeDialog {
            this.json = json
            this.counters = counters
            this.listener = listener
            return TestModeDialog()
        }
    }

    private lateinit var binding: STestModalFragmentBinding
    private val mainVM: MainViewModel by viewModels()
    private val speechVM: SpeechViewModel by viewModels()
    private val userVM: UserViewModel by activityViewModels()
    private var compareList: List<Word> = listOf()
    private var wordIsStudied = false
    private var words: List<Word> = listOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        isCancelable = false
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = STestModalFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            words = json.toWordList()
            if (words.isNotEmpty()) {
                enTextView.text = words[0].english
                nameDictTvTestModal.text = words[0].dictName

                mainVM.getRandomWord(words[0]).observe(viewLifecycleOwner) { word ->
                    val wordList = listOf<Word>(words[0], word)
                    val numberGenerator = RandomNumberGenerator(2, Date().time.toInt())
                    val i = numberGenerator.generate()
                    val j = numberGenerator.generate()
                    ruBtn1.text = wordList[i].translate
                    ruBtn2.text = wordList[j].translate
                    compareList = wordList
                }
            }
            if (counters.size >= 3) {
                val concatText = "${counters[0]} / ${counters[1]} ${getString(R.string.text_studied)} ${counters[2]}"
                wordsNumberTvTestModal.text = concatText
            }

            btnSoundModal.setOnClickListener {
                speechVM.doSpeech(enTextView.text.toString(), Locale.US )
            }

            modalBtnClose.setOnClickListener {
                requireActivity().finish()
            }

            btnOpenApp.setOnClickListener {
                listener?.openApp()
            }

            btnStopService.setOnClickListener {
                disablePassiveWordsRepeat(
                    onDisabled = {
                        val message = "${getString(R.string.text_app_is_closed)} ${getString(R.string.app_name)} ${getString(R.string.text_app_is_closed_end)}"
                        showToast(message)
                        requireActivity().finish()
                    },
                    onError = { er ->
                        showToast(er)
                        requireActivity().finish()
                    }
                )
            }

            checkBoxStudied.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
                    wordIsStudied = isChecked
                }
            })

            mainVM.countRepeat.observe(viewLifecycleOwner) { id ->
                if (id > 0) {
                    showToast(getString(R.string.text_word_is_not_show))
                }
            }

            val appSettings = AppSettings(requireContext())
            when (appSettings.orderPlay) {
                0 -> {
                    orderPlayIconIvTestModal.setImageResource(R.drawable.ic_repeat_white)
                }
                1 -> {
                    orderPlayIconIvTestModal.setImageResource(R.drawable.ic_shuffle_white)
                }
            }

            ruBtn1OnClick(ruBtn1)
            ruBtn2OnClick(ruBtn2)

        }
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            requireContext().isUserRegistered(
                onYes = {
                    btnStopService.visibility = View.GONE
                    tvReward.visibility = View.VISIBLE
                },
                onNotRegistered = {
                    btnStopService.visibility = View.VISIBLE
                    tvReward.visibility = View.GONE
                }
            )

            userVM.state.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is RevenueUpdated -> {
                        val user = state.user
                        buildRewardText(user)
                    }
                    is ReceivedUserData -> {
                        val user = state.user
                        buildRewardText(user)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun buildRewardText(user: User) {
        val userReward = BigDecimal.valueOf(user.defaultCurrencyReward).setScale(2, RoundingMode.DOWN)
        requireContext().getExchangeRateFromPref(
            onInit = {},
            onSuccess = { date: String?, symbol: String, rate: Double? ->
                val text = "${getString(R.string.coins_bag)} $userReward $symbol"
                binding.tvReward.text = text
            },
            onFailure = { e: Exception ->
                val message = if (e.message == null) this.javaClass.simpleName + ": Unknown error" else e.message!!
                showToast(message)
            }
        )
    }

    private fun ruBtn1OnClick(view: View) {
        view.setOnClickListener { v: View ->
            val button = v as Button
            val translate =
                button.text.toString().lowercase(Locale.getDefault())
            val english: String = binding.enTextView.text.toString().lowercase(Locale.getDefault())
            val result: Boolean = compareWords(compareList, english, translate)
            if (result) {
                rightAnswerAnim(button)
            } else {
                noRightAnswerAnim(button)
            }
        }
    }

    private fun ruBtn2OnClick(view: View) {
        view.setOnClickListener { v: View ->
            val button = v as Button
            val translate =
                button.text.toString().lowercase(Locale.getDefault())
            val english: String = binding.enTextView.text.toString().lowercase(Locale.getDefault())
            val result = compareWords(compareList, english, translate)
            if (result) {
                rightAnswerAnim(button)
            } else {
                noRightAnswerAnim(button)
            }
        }
    }

    private fun compareWords(
        compareList: List<Word>?,
        english: String,
        translate: String
    ): Boolean {
        var result = false
        if (!compareList.isNullOrEmpty()) {
            compareList.indices.forEach { i ->
                val enText = compareList[i].english.lowercase(Locale.getDefault())
                val ruText = compareList[i].translate.lowercase(Locale.getDefault())
                if (enText == english.lowercase(Locale.getDefault()) && ruText == translate.lowercase(Locale.getDefault())) {
                    result = true
                    return@forEach
                }
            }
        } else {
            result = true
        }
        return result
    }

    private fun rightAnswerAnim(button: Button) {
        val animRight = AnimationUtils.loadAnimation(activity, R.anim.anim_right)
        animRight.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                button.setBackgroundResource(R.drawable.btn_for_test_modal_green)
                button.setTextColor(requireContext().resources.getColor(R.color.colorWhite, null))
                if (wordIsStudied) {
                    if (words.isNotEmpty()) {
                        val wordId: Int = words[0]._id
                        mainVM.setCountRepeat(0, wordId, wordId)
                    }
                }
            }

            override fun onAnimationEnd(animation: Animation) {
                button.setBackgroundResource(R.drawable.btn_for_test_modal_transp)
                button.setTextColor(
                    requireContext().resources.getColor(
                        R.color.colorLightGreen,
                        null
                    )
                )
                requireActivity().finish()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        button.startAnimation(animRight)
    }

    private fun noRightAnswerAnim(button: Button) {
        val animNotRight = AnimationUtils.loadAnimation(activity, R.anim.anim_not_right)
        animNotRight.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                button.setBackgroundResource(R.drawable.btn_for_test_modal_red)
                button.setTextColor(requireContext().resources.getColor(R.color.colorWhite, null))
                wordIsStudied = false
            }

            override fun onAnimationEnd(animation: Animation) {
                button.setBackgroundResource(R.drawable.btn_for_test_modal_transp)
                button.setTextColor(
                    requireContext().resources.getColor(
                        R.color.colorLightGreen,
                        null
                    )
                )
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        button.startAnimation(animNotRight)
    }

    override fun onDetach() {

        when (words.size) {
            2 -> {
                requireContext().saveWordToPref(words[1])
            }
            1 -> {
                requireContext().saveWordToPref(
                    Word(
                        _id = 0,
                        dictName = words[0].dictName,
                        english = words[0].english,
                        translate = words[0].translate,
                        countRepeat = 1
                    )
                )
            }
        }
        super.onDetach()
    }
}