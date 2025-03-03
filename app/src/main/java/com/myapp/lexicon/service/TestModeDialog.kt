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
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.aboutapp.checkAppUpdate
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.ads.BANNER_SERVICE
import com.myapp.lexicon.ads.RevenueViewModel
import com.myapp.lexicon.ads.ext.showUserRewardAnimatedly
import com.myapp.lexicon.ads.loadBanner
import com.myapp.lexicon.ads.models.AdName
import com.myapp.lexicon.common.IS_IMPORTANT_UPDATE
import com.myapp.lexicon.databinding.STestModalFragmentBinding
import com.myapp.lexicon.helpers.RandomNumberGenerator
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showToast
import com.myapp.lexicon.interfaces.IModalFragment
import com.myapp.lexicon.main.MainViewModel
import com.myapp.lexicon.main.SpeechViewModel
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.main.viewmodels.UserViewModel.State.ReceivedUserData
import com.myapp.lexicon.models.Revenue
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.models.toWordList
import com.myapp.lexicon.settings.disablePassiveWordsRepeat
import com.myapp.lexicon.settings.getAuthDataFromPref
import com.myapp.lexicon.settings.getOrderPlay
import com.myapp.lexicon.settings.isUserRegistered
import java.util.Date
import java.util.Locale


class TestModeDialog : DialogFragment() {

    companion object {
        val TAG = "${TestModeDialog::class.java.simpleName}.TAG"
        private var listener: IModalFragment? = null

        fun newInstance(listener: IModalFragment): TestModeDialog {
            this.listener = listener
            return TestModeDialog()
        }
    }

    private lateinit var binding: STestModalFragmentBinding

    private val mainVM: MainViewModel by lazy {
        val factory = MainViewModel.Factory(requireActivity().application)
        ViewModelProvider(this, factory)[MainViewModel::class.java]
    }
    private val speechVM: SpeechViewModel by lazy {
        val factory = SpeechViewModel.Factory(requireActivity().application)
        ViewModelProvider(this, factory)[SpeechViewModel::class.java]
    }
    private val userVM: UserViewModel by viewModels()
    private val revenueVM by activityViewModels<RevenueViewModel>()
    private val adsVM by activityViewModels<AdsViewModel>()

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

        if (savedInstanceState == null) {
            userVM.getUserFromCloud()
        }

        with(binding) {

            bannerView.loadBanner(BANNER_SERVICE, onImpression = {
                requireContext().getAuthDataFromPref(onSuccess = { email: String, p: String ->
                    userVM.updateUserDataIntoCloud(
                        userMap = mapOf(
                            User.KEY_EMAIL to email,
                            AdName.BANNER_SERVICE.name to 1
                        )
                    )
                })
            })

            val extra = requireActivity().intent.getStringExtra(ServiceActivity.ARG_JSON)
            if (extra != null) {
                words = extra.toWordList()?: listOf()
                if (words.isNotEmpty()) {
                    enTextView.text = words[0].english
                    nameDictTvTestModal.text = words[0].dictName

                    mainVM.getRandomWord(
                        word = words[0],
                        onComplete = { throwable: Throwable? ->
                            throwable?.printStackTraceIfDebug()
                        },
                        onSuccess = { word: Word ->
                            val wordList = listOf(words[0], word)
                            val numberGenerator = RandomNumberGenerator(2, Date().time.toInt())
                            val i = numberGenerator.generate()
                            val j = numberGenerator.generate()
                            ruBtn1.text = wordList[i].translate
                            ruBtn2.text = wordList[j].translate
                            compareList = wordList
                        }
                    )

                    if (savedInstanceState == null) {
                        mainVM.getCountersById(words[0]._id)
                    }
                }
                else {
                    showToast("${TestModeDialog::class.simpleName}: Json error")
                    requireActivity().finish()
                }
            }
            else {
                showToast("${TestModeDialog::class.simpleName}: Json error")
                requireActivity().finish()
            }

            mainVM.counters.observe(viewLifecycleOwner) { result ->
                result.onSuccess { counters ->
                    val concatText = "${counters.rowNum} / ${counters.count} ${getString(R.string.text_studied)} ${counters.unUsed}"
                    wordsNumberTvTestModal.text = concatText
                }
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

            requireContext().getOrderPlay(
                onCycle = { value ->
                    when (value) {
                        0 -> {
                            orderPlayIconIvTestModal.apply {
                                setImageResource(R.drawable.ic_repeat_white)
                                scaleX = 1f
                            }
                        }
                        1 -> {
                            orderPlayIconIvTestModal.apply {
                                setImageResource(R.drawable.ic_repeat_white)
                                scaleX = -1f
                            }
                        }
                    }
                },
                onRandom = {
                    orderPlayIconIvTestModal.setImageResource(R.drawable.ic_shuffle_white)
                }
            )

            ruBtn1OnClick(ruBtn1)
            ruBtn2OnClick(ruBtn2)

            userVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    is ReceivedUserData -> {
                        val user = state.user
                        buildRewardText(user)
                    }
                    else -> {}
                }
            }

            revenueVM.userRevenueLD.observe(viewLifecycleOwner) { result ->
                result.onSuccess<Revenue> { revenue ->
                    buildRewardText(revenue)
                    adProgress.visibility = View.GONE
                }
                result.onError { throwable ->
                    throwable.printStackTraceIfDebug()
                    adProgress.visibility = View.GONE
                }
            }

            adsVM.interstitialAdState.observe(viewLifecycleOwner) { state ->
                if (state is AdsViewModel.AdState.Dismissed) {
                    adProgress.visibility = View.GONE
                    val bonus = state.bonus
                    if (bonus > 0.0) {
                        val coordinates = Pair(btnOpenApp.right + 55, btnOpenApp.top + 15)
                        layoutRoot.showUserRewardAnimatedly(bonus.toString(), coordinates)
                    }
                    requireContext().checkAppUpdate(
                        onAvailable = {
                            if (BuildConfig.IS_IMPORTANT_UPDATE == IS_IMPORTANT_UPDATE) {
                                listener?.openApp()
                            }
                        }
                    )
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            requireContext().isUserRegistered(
                onYes = {
                    btnStopService.visibility = View.GONE
                    rewardBlock.visibility = View.VISIBLE
                },
                onNotRegistered = {
                    btnStopService.visibility = View.VISIBLE
                    rewardBlock.visibility = View.GONE
                }
            )
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    private fun buildRewardText(user: User) {

        val userReward = user.userReward.to2DigitsScale()
        val text = "${getString(R.string.coins_bag)} $userReward ${user.currencySymbol}"
        binding.tvReward.text = text
    }

    private fun buildRewardText(revenue: Revenue) {

        val userReward = revenue.reward.to2DigitsScale()
        val text = "${getString(R.string.coins_bag)} $userReward ${revenue.currencySymbol}"
        binding.tvReward.text = text
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

}