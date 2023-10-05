@file:Suppress("ObjectLiteralToLambda")

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.ads.BannerAdIds
import com.myapp.lexicon.ads.loadBanner
import com.myapp.lexicon.databinding.SRepeatModalFragmentBinding
import com.myapp.lexicon.helpers.showToast
import com.myapp.lexicon.interfaces.IModalFragment
import com.myapp.lexicon.main.SpeechViewModel
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.models.toWordList
import com.myapp.lexicon.settings.AppSettings
import com.myapp.lexicon.settings.disablePassiveWordsRepeat
import com.myapp.lexicon.settings.isUserRegistered
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale


@AndroidEntryPoint
class RepeatDialog: DialogFragment() {

    companion object {
        val TAG = "${RepeatDialog::class.java.simpleName}.TAG"

        private var json: String = ""
        private var counters: List<Int> = listOf()
        private var listener: IModalFragment? = null

        fun newInstance(json: String, counters: List<Int>, listener: IModalFragment): RepeatDialog {
            this.json = json
            this.counters = counters
            this.listener = listener
            return RepeatDialog()
        }
    }

    private lateinit var binding: SRepeatModalFragmentBinding
    private val speechVM: SpeechViewModel by viewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val adsVM: AdsViewModel by activityViewModels()

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

        with(binding) {

            bannerView.loadBanner(BannerAdIds.BANNER_3)

            val words = json.toWordList()
            if (words.isNotEmpty()) {
                nameDictTv.text = words[0].dictName
                enTextView.text = words[0].english
                ruTextView.text = words[0].translate
            }

            if (counters.size > 2) {
                val text = "${counters[0]} / ${counters[1]} ${getString(R.string.text_studied)} ${counters[2]}"
                wordsNumberTvModalSv.text = text
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
                    speechVM.setRuSpeech(isChecked)
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

            val appSettings = AppSettings(requireContext())
            when (appSettings.orderPlay) {
                0 -> {
                    orderPlayIconIvModal.setImageResource(R.drawable.ic_repeat_white)
                }
                1 -> {
                    orderPlayIconIvModal.setImageResource(R.drawable.ic_shuffle_white)
                }
            }

            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    userVM.stateFlow.collect { state ->
                        when(state) {
                            is UserViewModel.State.ReceivedUserData -> {
                                val user = state.user
                                buildRewardText(user)
                            }
                            is UserViewModel.State.RevenueUpdated -> {
                                val user = state.user
                                buildRewardText(user)
                            }
                            else -> {}
                        }
                    }
                }
            }

            adsVM.interstitialAd.observe(viewLifecycleOwner) { result ->
                if (result != null) {
                    adProgress.visibility = View.GONE
                }
            }

        }
    }

    private fun buildRewardText(user: User) {

        val userReward = user.userReward.to2DigitsScale()
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




















