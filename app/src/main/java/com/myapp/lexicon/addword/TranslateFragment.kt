package com.myapp.lexicon.addword

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.ads.BannerAdIds
import com.myapp.lexicon.ads.InterstitialAdIds
import com.myapp.lexicon.ads.intrefaces.AdEventListener
import com.myapp.lexicon.ads.loadBanner
import com.myapp.lexicon.ads.showAd
import com.myapp.lexicon.databinding.TranslateFragmentBinding
import com.myapp.lexicon.main.MainActivity
import com.yandex.mobile.ads.interstitial.InterstitialAd
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder


private const val TEXT = "translate_text"

@AndroidEntryPoint
class TranslateFragment : Fragment()
{
    private lateinit var binding: TranslateFragmentBinding
    private lateinit var mActivity: AppCompatActivity
    private var interstitialAd: InterstitialAd? = null
    private val adsVM: AdsViewModel by viewModels()

    companion object
    {
        private var instance: TranslateFragment? = null
        private val javaScriptInterface = AppJavaScriptInterface()
        private var adListener: AdEventListener? = null
        fun getInstance(text: String, listener: AdEventListener?) : TranslateFragment = if (instance == null)
        {
            this.adListener = listener
            TranslateFragment().apply {
                arguments = Bundle().apply {
                    putString(TEXT, text)
                }
            }
        }
        else
        {
            instance as TranslateFragment
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

        adsVM.loadInterstitialAd(adId = InterstitialAdIds.INTERSTITIAL_2)
        adsVM.interstitialAd.observe(viewLifecycleOwner) { result ->
            result.onSuccess { ad ->
                interstitialAd = ad
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
                        AddWordDialog.newInstance(content)
                            .show(mActivity.supportFragmentManager, AddWordDialog.TAG)
                    }
                }
                loadProgress.visibility = View.GONE
            }

            bannerView.loadBanner(
                adId = BannerAdIds.BANNER_3
            )
        }

    }

    override fun onResume()
    {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                when(mActivity)
                {
                    is MainActivity -> {
                        parentFragmentManager.popBackStack()
                       (mActivity as MainActivity).refreshMainScreen(true)
                    }
                    is TranslateActivity -> {
                        requireActivity().finish()
                    }
                }
            }
        })

        binding.btnBack.setOnClickListener {

            when(mActivity)
            {
                is MainActivity -> {
                    (mActivity as MainActivity).refreshMainScreen(true)
                    interstitialAd?.showAd(
                        requireActivity(),
                        onImpression = { data ->
                            adListener?.onAdImpression(data)
                        },
                        onFailed = {
                            parentFragmentManager.popBackStack()
                        },
                        onDismissed = {
                            parentFragmentManager.popBackStack()
                        }
                    )
                }
                is TranslateActivity -> {
                    interstitialAd?.showAd(
                        requireActivity(),
                        onImpression = { data ->
                            adListener?.onAdImpression(data)
                        },
                        onFailed = {
                            requireActivity().finish()
                        },
                        onDismissed = {
                            requireActivity().finish()
                        }
                    )
                }
            }
        }
    }


}