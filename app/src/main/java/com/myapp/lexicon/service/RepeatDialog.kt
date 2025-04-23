@file:Suppress("ObjectLiteralToLambda", "UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.service

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.aboutapp.checkAppUpdate
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.ads.ext.showUserRewardAnimatedly
import com.myapp.lexicon.ads.loadBanner
import com.myapp.lexicon.auth.account.UserDataViewModel
import com.myapp.lexicon.databinding.SRepeatModalFragmentBinding
import com.myapp.lexicon.helpers.showToast
import com.myapp.lexicon.interfaces.IModalFragment
import com.myapp.lexicon.main.MainViewModel
import com.myapp.lexicon.main.SpeechViewModel
import com.myapp.lexicon.models.AdsReward
import com.myapp.lexicon.models.UserX
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.models.toWordList
import com.myapp.lexicon.settings.accessToken
import com.myapp.lexicon.settings.currentConfig
import com.myapp.lexicon.settings.disablePassiveWordsRepeat
import com.myapp.lexicon.settings.getOrderPlay
import com.myapp.lexicon.settings.isUserRegistered
import com.myapp.lexicon.settings.refreshToken
import java.util.Locale


class RepeatDialog: DialogFragment() {

    companion object {
        val TAG = "${RepeatDialog::class.java.simpleName}.TAG"

        private var listener: IModalFragment? = null

        fun newInstance(listener: IModalFragment): RepeatDialog {
            this.listener = listener
            return RepeatDialog()
        }
    }

    private lateinit var binding: SRepeatModalFragmentBinding
    private val mainVM: MainViewModel by lazy {
        val factory = MainViewModel.Factory(requireActivity().application)
        ViewModelProvider(this, factory)[MainViewModel::class.java]
    }
    private val speechVM: SpeechViewModel by lazy {
        val factory = SpeechViewModel.Factory(requireActivity().application)
        ViewModelProvider(this, factory)[SpeechViewModel::class.java]
    }

    private val userDataVM: UserDataViewModel by lazy {
        val factory = UserDataViewModel.Factory()
        ViewModelProvider(requireActivity(), factory)[UserDataViewModel::class].apply {
            this.setRefreshToken(requireContext().refreshToken)
        }
    }

    private val adsVM by activityViewModels<AdsViewModel>()

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
        binding = SRepeatModalFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accessToken = requireContext().accessToken
        if (savedInstanceState == null) {
            userDataVM.fetchUserData(accessToken)
        }

        with(binding) {

            val adId = requireContext().currentConfig.bannerIds.service
            bannerView.loadBanner(requireActivity(), adId)

            val extra = requireActivity().intent.getStringExtra(ServiceActivity.ARG_JSON)
            if (extra != null) {
                val words = extra.toWordList()
                if (!words.isNullOrEmpty()) {
                    nameDictTv.text = words[0].dictName
                    enTextView.text = words[0].english
                    ruTextView.text = words[0].translate

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
                    wordsNumberTvModalSv.text = concatText
                }
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
            btnClose.setOnClickListener {
                requireActivity().finish()
            }
            btnOpenApp.setOnClickListener {
                listener?.openApp()
            }
            btnSoundModal.setOnClickListener {
                val text = enTextView.text.toString()
                speechVM.doSpeech(text, Locale.US)
            }

            speechVM.isRuSpeech.observe(viewLifecycleOwner) { isCheck ->
                checkBoxRuSpeakModal.isChecked = isCheck
            }

            checkBoxRuSpeakModal.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                    speechVM.enableRuSpeech(isChecked)
                }
            })

            speechVM.speechDoneId.observe(viewLifecycleOwner) { id ->
                if (id == "En" && checkBoxRuSpeakModal.isChecked) {
                    speechVM.doSpeech(ruTextView.text.toString(), Locale.getDefault())
                }
            }

            speechVM.speechDoneId.observe(viewLifecycleOwner) { id: String ->
                if (id == "En") {
                    val isRuSpeech = speechVM.isRuSpeech.value
                    if (isRuSpeech != null && isRuSpeech) {
                        val ruText = ruTextView.text.toString()
                        speechVM.doSpeech(ruText, Locale.getDefault())
                    }
                }
            }

            requireContext().getOrderPlay(
                onCycle = { value ->
                    when (value) {
                        0 -> {
                            orderPlayIconIvModal.apply {
                                setImageResource(R.drawable.ic_repeat_white)
                                scaleX = 1f
                            }
                        }
                        1 -> {
                            orderPlayIconIvModal.apply {
                                setImageResource(R.drawable.ic_repeat_white)
                                scaleX = -1f
                            }
                        }
                    }
                },
                onRandom = {
                    orderPlayIconIvModal.setImageResource(R.drawable.ic_shuffle_white)
                }
            )

            userDataVM.userState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    is UserDataViewModel.UserDataState.ReceivedUserData -> {
                        val user = state.user
                        buildRewardText(user)
                    }
                    is UserDataViewModel.UserDataState.RevenueUpdated -> {
                        val reward = state.reward
                        buildRewardText(reward)
                    }
                    else -> {}
                }
            }

            adsVM.adState.observe(viewLifecycleOwner) { state ->
                if (state is AdsViewModel.AdState.Rewarded) {
                    adProgress.visibility = View.GONE
                    val reward = state.reward.rewardPerAd
                    if (reward > 0.0) {
                        val coordinates = Pair(btnOpenApp.right + 55, btnOpenApp.top + 15)
                        layoutRoot.showUserRewardAnimatedly(reward.toString(), coordinates)
                    }
                    requireContext().checkAppUpdate(
                        onAvailable = {
                            if (BuildConfig.IS_IMPORTANT_UPDATE == requireContext().currentConfig.isImportantUpdate) {
                                listener?.openApp()
                            }
                        }
                    )
                }
            }

        }
    }

    private fun buildRewardText(reward: AdsReward) {

        val userReward = reward.monthBalance.to2DigitsScale()
        val text = "${getString(R.string.coins_bag)} $userReward ${reward.currencySymbol}"
        binding.tvReward.text = text
    }

    private fun buildRewardText(user: UserX) {

        val userReward = user.monthBalance?.to2DigitsScale()
        val text = "${getString(R.string.coins_bag)} $userReward ${user.currencySymbol}"
        binding.tvReward.text = text
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
}




















