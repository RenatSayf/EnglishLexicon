package com.myapp.lexicon.addword

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.billing.BillingViewModel
import com.myapp.lexicon.databinding.TranslateFragmentBinding
import com.myapp.lexicon.main.MainActivity
import java.net.URLDecoder

private const val TEXT = "translate_text"

class TranslateFragment : Fragment(R.layout.translate_fragment)
{
    private lateinit var binding: TranslateFragmentBinding
    private lateinit var billingVM: BillingViewModel
    private lateinit var adsVM: AdsViewModel
    private lateinit var mActivity: AppCompatActivity
    private var adsToken = ""

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
        billingVM = ViewModelProvider(this)[BillingViewModel::class.java]
        adsVM = ViewModelProvider(this)[AdsViewModel::class.java]
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.translate_fragment, container, false)

        billingVM.noAdsToken.observe(viewLifecycleOwner, {
            if (it != null && it.isEmpty())
            {
                adsToken = it
                val adLayout: LinearLayout = root.findViewById(R.id.adLayout)
                val banner = adsVM.getAddWordBanner()
                adLayout.addView(banner)
                banner.loadAd(AdRequest.Builder().build())
                adsVM.loadAd2()
            }
        })

        adsVM.isAdClosed2.observe(viewLifecycleOwner, {
            if (it)
            {
                when(mActivity)
                {
                    is MainActivity -> mActivity.supportFragmentManager.popBackStack()
                    is TranslateActivity -> mActivity.finish()
                }
            }
        })
        return root
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
            //todo parsing WebView: Step 7
            binding.webView.loadUrl("javascript:window.HtmlHandler.handleHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
        }

        //todo Отправка события в активити/фрагмент: Step 4. End
        AppJavaScriptInterface.parseEvent.observe(viewLifecycleOwner, {
            if (!it.hasBeenHandled)
            {
                val content = it.getContent()
                if (!content.isNullOrEmpty())
                {
                    AddWordDialog.getInstance(content).show(mActivity.supportFragmentManager, AddWordDialog.TAG)
                }
            }
            binding.loadProgress.visibility = View.GONE
        })
    }

    override fun onResume()
    {
        super.onResume()
        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                when
                {
                    adsToken.isEmpty() -> adsVM.showAd2(mActivity)
                    else ->
                    {
                        when(mActivity)
                        {
                            is MainActivity -> mActivity.supportFragmentManager.popBackStack()
                            is TranslateActivity -> mActivity.finish()
                        }
                    }
                }
                this.remove()
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        if (item.itemId == android.R.id.home)
        {
            when
            {
                adsToken.isEmpty() -> adsVM.showAd2(mActivity)
                else ->
                {
                    when(mActivity)
                    {
                        is MainActivity -> mActivity.supportFragmentManager.popBackStack()
                        is TranslateActivity -> mActivity.finish()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


}