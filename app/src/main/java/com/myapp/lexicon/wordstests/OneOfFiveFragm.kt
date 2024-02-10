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
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.lexicon.R
import com.myapp.lexicon.adapters.OneFiveTestAdapter
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.ads.InterstitialAdIds
import com.myapp.lexicon.ads.RevenueViewModel
import com.myapp.lexicon.ads.showAd
import com.myapp.lexicon.databinding.OneOfFiveFragmNewBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.RandomNumberGenerator
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.settings.adsIsEnabled
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.rewarded.RewardedAd
import java.util.*

const val ROWS: Int = 5



class OneOfFiveFragm : Fragment(R.layout.one_of_five_fragm_new), OneFiveTestAdapter.ITestAdapterListener
{
    private lateinit var binding: OneOfFiveFragmNewBinding
    private lateinit var vm: OneOfFiveViewModel
    private lateinit var mActivity: MainActivity
    private val adsVM: AdsViewModel by viewModels()
    private val revenueVM: RevenueViewModel by activityViewModels()
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null


    companion object
    {
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
        return inflater.inflate(R.layout.one_of_five_fragm_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = OneOfFiveFragmNewBinding.bind(view)

        if (!wordList.isNullOrEmpty()) vm.initTest(wordList!!.toList())

        adsVM.loadInterstitialAd(InterstitialAdIds.INTERSTITIAL_2)
        adsVM.interstitialAd.observe(viewLifecycleOwner) { result ->
            result.onSuccess { ad ->
                interstitialAd = ad
            }
        }

        vm.adapterList.observe(viewLifecycleOwner) {
            binding.answersRecyclerView.apply {
                this.adapter = OneFiveTestAdapter(it).apply {
                    setHasStableIds(true)
                    layoutManager = LinearLayoutManager(requireContext())
                    setOnItemClickListener(this@OneOfFiveFragm)
                }
            }
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
                parentFragmentManager.popBackStack()
                mActivity.testPassed()
            }
        )
    }

    private fun onTestFailed(errors: Int)
    {
        showAd(
            onComplete = {
                parentFragmentManager.popBackStack()
                mActivity.testFailed(errors)
            }
        )
    }

    private fun showAd(
        onComplete: () -> Unit = {}
    ) {
        if (this.adsIsEnabled) {

            interstitialAd?.showAd(
                requireActivity(),
                onImpression = { data ->
                    if (data != null) {
                        revenueVM.updateUserRevenueIntoCloud(data)
                    }
                }, onDismissed = {
                    onComplete.invoke()
                }
            )

            rewardedAd?.showAd(
                requireActivity(),
                onImpression = { data ->
                    if (data != null) {
                        revenueVM.updateUserRevenueIntoCloud(data)
                    }
                }, onDismissed = {
                    onComplete.invoke()
                }
            )

            if (interstitialAd == null && rewardedAd == null) {
                onComplete.invoke()
            }
        }
        else {
            onComplete.invoke()
        }
    }


}