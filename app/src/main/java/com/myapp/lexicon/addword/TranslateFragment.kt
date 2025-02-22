@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.addword

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.ads.BANNER_TRANSLATE
import com.myapp.lexicon.ads.INTERSTITIAL_TRANSLATE
import com.myapp.lexicon.ads.NATIVE_AD_TRANS
import com.myapp.lexicon.ads.RevenueViewModel
import com.myapp.lexicon.ads.loadBanner
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.ads.models.AdName
import com.myapp.lexicon.ads.models.AdType
import com.myapp.lexicon.ads.showAd
import com.myapp.lexicon.ads.startBannersActivity
import com.myapp.lexicon.ads.startNativeAdsActivity
import com.myapp.lexicon.common.AD_TYPE
import com.myapp.lexicon.databinding.TranslateFragmentBinding
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.main.MainViewModel
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.Word
import com.myapp.lexicon.models.toWord
import com.myapp.lexicon.settings.getAuthDataFromPref
import com.myapp.lexicon.settings.getWordFromPref
import com.myapp.lexicon.settings.orderPlayFromPref
import com.yandex.mobile.ads.interstitial.InterstitialAd
import java.net.URLDecoder


private const val TEXT = "translate_text"


class TranslateFragment : Fragment()
{
    private lateinit var binding: TranslateFragmentBinding
    private lateinit var mActivity: AppCompatActivity
    private var interstitialAd: InterstitialAd? = null
    private val adsVM: AdsViewModel by activityViewModels()
    private val addWordVM: AddWordViewModel by lazy {
        val factory = AddWordViewModel.Factory(requireContext())
        ViewModelProvider(this, factory)[AddWordViewModel::class.java]
    }
    private val mainVM: MainViewModel by lazy {
        val factory = MainViewModel.Factory(requireActivity().application)
        ViewModelProvider(this, factory)[MainViewModel::class.java]
    }
    private val revenueVM: RevenueViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()

    companion object
    {
        val KEY_FRAGMENT_START = "${TranslateFragment::class.simpleName}.fragment_start"
        const val KEY_ADD_WORD = "add_word_key"
        const val KEY_NEW_WORD = "new_word_key"

        private val javaScriptInterface = AppJavaScriptInterface()
        fun getInstance(text: String) : TranslateFragment
        {
            return TranslateFragment().apply {
                arguments = Bundle().apply {
                    putString(TEXT, text)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        when (activity)
        {
            is MainActivity -> mActivity = activity as MainActivity
            is TranslateActivity ->
            {
                mActivity = activity as TranslateActivity
                mActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                mActivity.supportActionBar?.setHomeButtonEnabled(true)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = TranslateFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = TranslateFragmentBinding.bind(view)

        if (AD_TYPE == AdType.INTERSTITIAL) {
            adsVM.loadInterstitialAd(INTERSTITIAL_TRANSLATE)
            adsVM.interstitialAd.observe(viewLifecycleOwner) { result ->
                result.onSuccess { ad ->
                    interstitialAd = ad
                }
            }
        }

        val inputText = arguments?.getString(TEXT) ?: ""

        with(binding) {

            webView.apply {
                settings.javaScriptEnabled = true //todo parsing WebView: Step 3
                settings.domStorageEnabled = true //todo parsing WebView: Step 4
                addJavascriptInterface(javaScriptInterface, "HtmlHandler") //todo parsing WebView: Step 5
                webViewClient = AppWebViewClient(this@TranslateFragment) //todo parsing WebView: Step 6
                loadUrl("https://translate.yandex.ru/?text=${inputText}")
            }

            btnSave.setOnClickListener {
                loadProgress.visibility = View.VISIBLE
                val url = webView.url
                val decode = URLDecoder.decode(url, "UTF-8")
                javaScriptInterface.setInputText(decode)
                //Hint parsing WebView: Step 7
                webView.loadUrl("javascript:window.HtmlHandler.handleHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
            }

            //Hint Отправка события в активити/фрагмент: Step 4. End
            AppJavaScriptInterface.parseEvent.observe(viewLifecycleOwner) {
                if (!it.hasBeenHandled) {
                    val content = it.getContent()
                    if (!content.isNullOrEmpty()) {
                        val inputList = if (!content[0].contains(Regex("[A-Za-z]"))) {
                            content.reversed()
                        } else content
                        AddWordDialog.newInstance(inputList).show(mActivity.supportFragmentManager, AddWordDialog.TAG)
                    }
                }
                loadProgress.visibility = View.GONE
            }

            setFragmentResultListener(KEY_ADD_WORD, listener = {requestKey, bundle ->
                val json = bundle.getString(KEY_NEW_WORD)
                val newWord = json?.toWord()
                if (newWord is Word) {
                    addWordVM.insertWord(newWord)
                }
            })

            addWordVM.insertedWord.observe(viewLifecycleOwner) { pair ->
                if (pair.first is Word) {
                    requireContext().getWordFromPref(
                        onSuccess = { word, i ->
                            if (pair?.first?.dictName == word.dictName) {
                                val playOrder = requireContext().orderPlayFromPref
                                mainVM.updatePlayList(word, i, playOrder)
                            }
                        },
                        onFailure = { exception ->
                            exception.printStackTraceIfDebug()
                        }
                    )
                    val message = "${getString(R.string.in_dictionary)}  ${pair.first?.dictName}  ${getString(R.string.new_word_is_added)}"
                    showSnackBar(message)
                }
                else if (pair.second is Throwable) {
                    showSnackBar(pair.second?.message?: getString(R.string.text_unknown_error_message))
                }
            }

            bannerView.loadBanner(
                adId = BANNER_TRANSLATE,
                onImpression = {
                    requireContext().getAuthDataFromPref(
                        onSuccess = {email: String, password: String ->
                            userVM.updateUserDataIntoCloud(
                                userMap = mapOf(
                                    User.KEY_EMAIL to email,
                                    AdName.BANNER_TRANSLATE.name to 1
                                )
                            )
                        }
                    )
                }
            )
        }

    }

    override fun onResume()
    {
        super.onResume()

        setFragmentResult(KEY_FRAGMENT_START, Bundle.EMPTY)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                selectAndShowAd()
            }
        })

        binding.btnBack.setOnClickListener {
            selectAndShowAd()
        }
    }

    private fun selectAndShowAd() {
        when(mActivity)
        {
            is MainActivity -> {

                when(AD_TYPE) {
                    AdType.BANNER -> {
                        requireActivity().startBannersActivity(
                            onImpression = {data: AdData? ->
                                if (data != null) {
                                    data.adCount = mapOf(AdName.BANNER_TRANSLATE.name to 1)
                                    revenueVM.updateUserRevenueIntoCloud(data)
                                }
                            },
                            onDismissed = {bonus: Double ->
                                adsVM.setInterstitialAdState(AdsViewModel.AdState.Dismissed(bonus))
                                parentFragmentManager.popBackStack()
                            }
                        )
                    }
                    AdType.NATIVE -> {
                        requireActivity().startNativeAdsActivity(
                            adId = NATIVE_AD_TRANS,
                            onImpression = {data: AdData? ->
                                if (data != null) {
                                    data.adCount = mapOf(AdName.NATIVE_TRANSLATE.name to 1)
                                    revenueVM.updateUserRevenueIntoCloud(data)
                                }
                            },
                            onDismissed = {bonus: Double ->
                                adsVM.setInterstitialAdState(AdsViewModel.AdState.Dismissed(bonus))
                                parentFragmentManager.popBackStack()
                            }
                        )
                    }
                    AdType.INTERSTITIAL -> {
                        interstitialAd?.showAd(
                            requireActivity(),
                            onImpression = { data ->
                                if (data is AdData) {
                                    data.adCount = mapOf(AdName.INTERSTITIAL_TRANSLATE.name to 1)
                                    revenueVM.updateUserRevenueIntoCloud(data)
                                }
                            },
                            onDismissed = {
                                adsVM.setInterstitialAdState(AdsViewModel.AdState.Dismissed(it))
                                parentFragmentManager.popBackStack()
                            }
                        )?: run {
                            parentFragmentManager.popBackStack()
                        }
                    }
                }
            }
            is TranslateActivity -> {

                when(AD_TYPE) {
                    AdType.BANNER -> {
                        requireActivity().startBannersActivity(
                            onImpression = {data: AdData? ->
                                if (data != null) {
                                    data.adCount = mapOf(AdName.BANNER_TRANSLATE.name to 1)
                                    revenueVM.updateUserRevenueIntoCloud(data)
                                }
                            },
                            onDismissed = {bonus: Double ->
                                requireActivity().finish()
                            }
                        )
                    }
                    AdType.NATIVE -> {
                        requireActivity().startNativeAdsActivity(
                            onImpression = {data: AdData? ->
                                if (data != null) {
                                    data.adCount = mapOf(AdName.NATIVE_TRANSLATE.name to 1)
                                    revenueVM.updateUserRevenueIntoCloud(data)
                                }
                            },
                            onDismissed = {bonus: Double ->
                                requireActivity().finish()
                            }
                        )
                    }
                    AdType.INTERSTITIAL -> {
                        interstitialAd?.showAd(
                            requireActivity(),
                            onImpression = { data ->
                                if (data is AdData) {
                                    data.adCount = mapOf(AdName.INTERSTITIAL_TRANSLATE.name to 1)
                                    revenueVM.updateUserRevenueIntoCloud(data)
                                }
                            },
                            onDismissed = {
                                requireActivity().finish()
                            }
                        )?: run {
                            requireActivity().finish()
                        }
                    }
                }
            }
        }
    }


}