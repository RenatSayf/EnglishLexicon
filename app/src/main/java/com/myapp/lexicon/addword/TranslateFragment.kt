package com.myapp.lexicon.addword

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.appodeal.ads.Appodeal
import com.myapp.lexicon.ads.showAdIfLoaded
import com.myapp.lexicon.ads.showInterstitial
import com.myapp.lexicon.databinding.TranslateFragmentBinding
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.settings.adsIsEnabled
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder


private const val TEXT = "translate_text"

@AndroidEntryPoint
class TranslateFragment : Fragment()
{
    private lateinit var binding: TranslateFragmentBinding
    private lateinit var mActivity: AppCompatActivity

    companion object
    {
        private var instance: TranslateFragment? = null
        private val javaScriptInterface = AppJavaScriptInterface()
        fun getInstance(text: String) : TranslateFragment = if (instance == null)
        {
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

        if (this.adsIsEnabled) {
            val initialized = Appodeal.isInitialized(Appodeal.BANNER)
            if (initialized) {
                val bannerView = Appodeal.getBannerView(requireContext())
                binding.adLayout.addView(bannerView)
                Appodeal.show(requireActivity(), Appodeal.BANNER_VIEW)
            }
        }
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = TranslateFragmentBinding.bind(view)

        val inputText = arguments?.getString(TEXT) ?: ""

        binding.webView.apply {
            settings.javaScriptEnabled = true //todo parsing WebView: Step 3
            settings.domStorageEnabled = true //todo parsing WebView: Step 4
            addJavascriptInterface(javaScriptInterface, "HtmlHandler") //todo parsing WebView: Step 5
            webViewClient = AppWebViewClient(this@TranslateFragment) //todo parsing WebView: Step 6
            loadUrl("https://translate.yandex.ru/?text=${inputText}")
        }

        binding.btnSave.setOnClickListener {
            binding.loadProgress.visibility = View.VISIBLE
            val url = binding.webView.url
            val decode = URLDecoder.decode(url, "UTF-8")
            javaScriptInterface.setInputText(decode)
            //Hint parsing WebView: Step 7
            binding.webView.loadUrl("javascript:window.HtmlHandler.handleHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
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
            binding.loadProgress.visibility = View.GONE
        }
    }

    override fun onResume()
    {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if (this@TranslateFragment.adsIsEnabled) {
                    showInterstitial(
                        onShown = {
                            parentFragmentManager.popBackStack()
                        }
                    )
                }
            }
        })

        binding.btnBack.setOnClickListener {
            if (this@TranslateFragment.adsIsEnabled) {
                showInterstitial(
                    onShown = {
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        }
    }

}