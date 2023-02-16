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
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.lexicon.R
import com.myapp.lexicon.adapters.OneFiveTestAdapter
import com.myapp.lexicon.ads.loadInterstitialAd
import com.myapp.lexicon.ads.showInterstitialAd
import com.myapp.lexicon.billing.BillingViewModel
import com.myapp.lexicon.databinding.OneOfFiveFragmNewBinding
import com.myapp.lexicon.dialogs.TestCompleteDialog
import com.myapp.lexicon.helpers.RandomNumberGenerator
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.models.Word
import com.yandex.mobile.ads.interstitial.InterstitialAd
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

const val ROWS: Int = 5


@AndroidEntryPoint
class OneOfFiveFragm : Fragment(R.layout.one_of_five_fragm_new), TestCompleteDialog.ITestCompleteDialogListener, OneFiveTestAdapter.ITestAdapterListener
{
    private lateinit var binding: OneOfFiveFragmNewBinding
    private lateinit var vm: OneOfFiveViewModel
    private lateinit var mActivity: MainActivity

    private lateinit var billingVM: BillingViewModel
    private var yandexAd2: InterstitialAd? = null


    companion object
    {
        private var wordList: List<Word>? = null
        private var instance: OneOfFiveFragm? = null

        @JvmStatic
        fun newInstance(list: MutableList<Word>): OneOfFiveFragm
        {
            val shuffledList = list.shuffled()
            wordList = shuffledList
            return if (instance == null)
            {
                OneOfFiveFragm()
            } else instance as OneOfFiveFragm
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity
        billingVM = ViewModelProvider(this)[BillingViewModel::class.java]
        vm = ViewModelProvider(this)[OneOfFiveViewModel::class.java]
        if (!wordList.isNullOrEmpty()) vm.initTest(wordList as MutableList<Word>)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        val root = inflater.inflate(R.layout.one_of_five_fragm_new, container, false)

        billingVM.noAdsToken.observe(viewLifecycleOwner) { t ->
            if (t.isNullOrEmpty()) {
                this.loadInterstitialAd(
                    index = 2,
                    success = { ad ->
                        yandexAd2 = ad
                    },
                    error = {
                        yandexAd2 = null
                    }
                )
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = OneOfFiveFragmNewBinding.bind(view)

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
                val dialog = errors?.let { err -> TestCompleteDialog.getInstance(err, this) }
                dialog?.show(mActivity.supportFragmentManager, TestCompleteDialog.TAG)
                mActivity.supportFragmentManager.popBackStack()
            }
        }
    }

    private lateinit var backPressedCallback: OnBackPressedCallback
    override fun onResume()
    {
        super.onResume()
        backPressedCallback = mActivity.onBackPressedDispatcher.addCallback {
            mActivity.supportFragmentManager.popBackStack()
            mActivity.mainViewModel.setMainControlVisibility(View.VISIBLE)
        }
    }

    override fun onDestroy()
    {
        backPressedCallback.remove()
        super.onDestroy()
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

    override fun onTestPassed()
    {
        yandexAd2?.showInterstitialAd(
            dismiss = {
                mActivity.supportFragmentManager.popBackStack()
                mActivity.testPassed()
            }
        )?: run {
            mActivity.supportFragmentManager.popBackStack()
            mActivity.testPassed()
        }
    }

    override fun onTestFailed(errors: Int)
    {
        yandexAd2?.showInterstitialAd(
            dismiss = {
                mActivity.supportFragmentManager.popBackStack()
                mActivity.testFailed(errors)
            }
        )?: run {
            mActivity.supportFragmentManager.popBackStack()
            mActivity.testFailed(errors)
        }
    }

}