package com.myapp.lexicon.wordstests

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.adapters.OneFiveTestAdapter
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.ads.INTERSTITIAL_MAIN
import com.myapp.lexicon.ads.NATIVE_AD_MAIN
import com.myapp.lexicon.ads.REWARDED_MAIN_ID
import com.myapp.lexicon.ads.RevenueViewModel
import com.myapp.lexicon.ads.models.AD_MAIN
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.ads.models.AdName
import com.myapp.lexicon.ads.models.AdType
import com.myapp.lexicon.ads.showAd
import com.myapp.lexicon.ads.startBannersActivity
import com.myapp.lexicon.ads.startNativeAdsActivity
import com.myapp.lexicon.databinding.OneOfFiveFragmNewBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.RandomNumberGenerator
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.adsIsEnabled
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.rewarded.RewardedAd
import java.util.Date

const val ROWS: Int = 5



class OneOfFiveFragm : Fragment(), OneFiveTestAdapter.ITestAdapterListener
{
    private lateinit var binding: OneOfFiveFragmNewBinding
    private lateinit var vm: OneOfFiveViewModel
    private lateinit var mActivity: MainActivity
    private val adsVM: AdsViewModel by activityViewModels()
    private val revenueVM: RevenueViewModel by activityViewModels()
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val wordsAdapter: OneFiveTestAdapter by lazy {
        OneFiveTestAdapter().apply {
            setHasStableIds(true)
            setOnItemClickListener(this@OneOfFiveFragm)
        }
    }


    companion object
    {
        const val TEST_START = "TEST_START_15897423"
        private var wordList: List<Word>? = null

        @JvmStatic
        fun newInstance(
            list: MutableList<Word>
        ): OneOfFiveFragm
        {
            val shuffledList = list.shuffled().toList()
            wordList = shuffledList
            return OneOfFiveFragm()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity
        vm = ViewModelProvider(this)[OneOfFiveViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OneOfFiveFragmNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.answersRecyclerView.apply {
            this.adapter = wordsAdapter
        }

        if (!wordList.isNullOrEmpty()) vm.initTest(wordList!!.toList())
        when(AD_MAIN) {
            AdType.INTERSTITIAL.type -> {
                adsVM.loadInterstitialAd(INTERSTITIAL_MAIN)
                adsVM.interstitialAd.observe(viewLifecycleOwner) { result ->
                    result.onSuccess { ad: InterstitialAd ->
                        interstitialAd = ad
                    }
                }
            }
            AdType.REWARDED.type -> {
                adsVM.loadRewardedAd(REWARDED_MAIN_ID)
                adsVM.rewardedAd.observe(viewLifecycleOwner) { result ->
                    result.onSuccess { ad: RewardedAd ->
                        rewardedAd = ad
                    }
                }
            }
        }

        vm.adapterList.observe(viewLifecycleOwner) { list ->
            wordsAdapter.addItems(list)
        }

        vm.mysteryWord.observe(viewLifecycleOwner) {
            binding.mysteryWordView.text = it
        }

        vm.progressMax.observe(viewLifecycleOwner) {
            binding.progressView1of5.max = it
        }

        vm.progress.observe(viewLifecycleOwner) {
            binding.progressView1of5.progress = it
            val progressValue = "$it/${vm.progressMax.value}"
            binding.progressValueView.text = progressValue
            if (it == binding.progressView1of5.max) {
                val errors = vm.wrongAnswerCount.value
                errors?.let { err ->
                    when(err) {
                        0 -> {
                            ConfirmDialog.newInstance( onLaunch = { dialog, binding ->
                                with(binding) {
                                    ivIcon.visibility = View.INVISIBLE
                                    tvEmoji.text = getString(R.string.slightly_smiling_face)
                                    tvEmoji2.apply {
                                        text = getString(R.string.thumbs_up)
                                        visibility = View.VISIBLE
                                    }
                                    tvMessage.text = getString(R.string.text_test_passed)
                                    btnCancel.visibility = View.INVISIBLE
                                    btnOk.text = getString(R.string.text_ok)
                                    btnOk.setOnClickListener {
                                        dialog.dismiss()
                                        onTestPassed()
                                    }
                                }
                            }).show(parentFragmentManager, ConfirmDialog.TAG)
                        }
                        else -> {
                            ConfirmDialog.newInstance( onLaunch = { dialog, binding ->
                                with(binding) {
                                    ivIcon.visibility = View.INVISIBLE
                                    tvEmoji.text = getString(R.string.confused_face)
                                    tvEmoji2.apply {
                                        text = getString(R.string.thumbs_up)
                                        visibility = View.GONE
                                    }
                                    tvMessage.text = getString(R.string.text_test_not_passed)
                                    btnCancel.visibility = View.INVISIBLE
                                    btnOk.text = getString(R.string.text_repeat)
                                    btnOk.setOnClickListener {
                                        dialog.dismiss()
                                        onTestFailed(err)
                                    }
                                }
                            }).show(parentFragmentManager, ConfirmDialog.TAG)
                        }
                    }
                }
            }
        }
    }

    override fun onResume()
    {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mActivity.supportFragmentManager.popBackStack()
                mActivity.mainVM.setMainControlVisibility(View.VISIBLE)
            }
        })

        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
            mActivity.mainVM.setMainControlVisibility(View.VISIBLE)
        }

        setFragmentResult(TEST_START, result = Bundle.EMPTY)
    }

    override fun onItemClickListener(position: Int, word: Word, view: Button)
    {
        val testAdapter = binding.answersRecyclerView.adapter as OneFiveTestAdapter
        val items = testAdapter.getItems()
        if (items.isNotEmpty())
        {
            val translate = binding.mysteryWordView.text.toString()
            val english = view.text.toString()
            val result = items.any { w -> w.english == english && w.translate == translate }
            if (result)
            {
                view.setBackgroundResource(R.drawable.text_btn_for_test_green)
                testAdapter.removeItem(english, translate)
                val animRight = AnimationUtils.loadAnimation(requireContext(), R.anim.from_right_to_left_anim)
                if (testAdapter.getItems().isNotEmpty())
                {
                    val itemCount = testAdapter.itemCount
                    val randomIndex = RandomNumberGenerator(itemCount, (Date().time.toInt())).generate()
                    vm.setMysteryWord(testAdapter.getItems()[randomIndex].translate)
                }
                else
                {
                    vm.mysteryWord = MutableLiveData()
                }
                vm.setProgress(binding.progressView1of5.progress + 1)
                binding.mysteryWordView.startAnimation(animRight.apply {
                    setAnimationListener(object : Animation.AnimationListener
                    {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onAnimationStart(p0: Animation?)
                        {
                            if (!vm.wordsList.value.isNullOrEmpty())
                            {
                                val nextItem = vm.takeNextWord()
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
                vm.increaseWrongAnswerCount()
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

    private fun onTestPassed()
    {
        showAd(
            onComplete = {
                try {
                    parentFragmentManager.popBackStack()
                    mActivity.testPassed()
                } catch (e: Exception) {
                    e.printStackTraceIfDebug()
                }
            }
        )
    }

    private fun onTestFailed(errors: Int)
    {
        showAd(
            onComplete = {
                try {
                    parentFragmentManager.popBackStack()
                    mActivity.testFailed(errors)
                } catch (e: Exception) {
                    e.printStackTraceIfDebug()
                }
            }
        )
    }

    private fun showAd(
        onComplete: () -> Unit = {}
    ) {
        if (this.adsIsEnabled) {

            when(AD_MAIN) {
                AdType.BANNER.type -> {
                    requireActivity().startBannersActivity(
                        onImpression = {data: AdData? ->
                            if (data != null) {
                                data.adCount = mapOf(AdName.FULL_MAIN.name to 1)
                                revenueVM.updateUserRevenueIntoCloud(data)
                            }
                        },
                        onDismissed = {bonus: Double ->
                            adsVM.setInterstitialAdState(AdsViewModel.AdState.Dismissed(bonus))
                            onComplete.invoke()
                        }
                    )
                }
                AdType.NATIVE.type -> {
                    requireActivity().startNativeAdsActivity(
                        adId = NATIVE_AD_MAIN,
                        onImpression = {data: AdData? ->
                            if (data != null) {
                                try {
                                    data.adCount = mapOf(AdName.FULL_MAIN.name to 1)
                                    revenueVM.updateUserRevenueIntoCloud(data)
                                } catch (e: Exception) {
                                    e.printStackTraceIfDebug()
                                }
                            }
                        },
                        onDismissed = {bonus: Double ->
                            adsVM.setInterstitialAdState(AdsViewModel.AdState.Dismissed(bonus))
                            try {
                                onComplete.invoke()
                            } catch (e: Exception) {
                                e.printStackTraceIfDebug()
                            }
                        }
                    )
                }
                AdType.INTERSTITIAL.type -> {
                    interstitialAd?.showAd(
                        requireActivity(),
                        onImpression = { data ->
                            if (data != null) {
                                data.adCount = mapOf(AdName.FULL_MAIN.name to 1)
                                revenueVM.updateUserRevenueIntoCloud(data)
                            }
                        }, onDismissed = { bonus: Double ->
                            adsVM.setInterstitialAdState(AdsViewModel.AdState.Dismissed(bonus))
                            onComplete.invoke()
                        }
                    )
                    if (interstitialAd == null) {
                        onComplete.invoke()
                    }
                }
                AdType.REWARDED.type -> {
                    rewardedAd?.showAd(
                        requireActivity(),
                        onImpression = {data: AdData? ->
                            if (data != null) {
                                data.adCount = mapOf(AdName.FULL_MAIN.name to 1)
                                revenueVM.updateUserRevenueIntoCloud(data)
                            }
                        },
                        onDismissed = {bonus: Double ->
                            adsVM.setInterstitialAdState(AdsViewModel.AdState.Dismissed(bonus))
                            onComplete.invoke()
                        }
                    )?: run {
                        onComplete.invoke()
                    }
                }
            }
        }
        else {
            onComplete.invoke()
        }
    }


}