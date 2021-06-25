package com.myapp.lexicon.addword

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.AdsViewModel
import com.myapp.lexicon.billing.BillingViewModel
import kotlinx.android.synthetic.main.translate_fragment.*
import kotlinx.android.synthetic.main.translate_fragment.view.*
import java.net.URLDecoder

private const val TEXT = "translate_text"

class TranslateFragment : Fragment(),View.OnKeyListener
{
    private lateinit var billingVM: BillingViewModel
    private lateinit var adsVM: AdsViewModel

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
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.translate_fragment, container, false)

        val inputText = arguments?.getString(TEXT) ?: ""

        root.webView.apply {
            settings.javaScriptEnabled = true //todo parsing WebView: Step 3
            settings.domStorageEnabled = true //todo parsing WebView: Step 4
            addJavascriptInterface(javaScriptInterface, "HtmlHandler") //todo parsing WebView: Step 5
            webViewClient = AppWebViewClient(this@TranslateFragment) //todo parsing WebView: Step 6
            loadUrl("https://translate.yandex.ru/?text=${inputText}")
        }

        billingVM.noAdsToken.observe(viewLifecycleOwner, {
            if (it != null && it.isEmpty())
            {
                val adLayout: LinearLayout = root.findViewById(R.id.adLayout)
                val banner = adsVM.getAddWordBanner()
                adLayout.addView(banner)
                banner.loadAd(AdRequest.Builder().build())
                adsVM.loadAd2()
            }
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        btnSave.setOnClickListener {
            loadProgress.visibility = View.VISIBLE
            val url = webView.url
            val decode = URLDecoder.decode(url, "UTF-8")
            javaScriptInterface.setInputText(decode)
            //todo parsing WebView: Step 7
            webView?.loadUrl("javascript:window.HtmlHandler.handleHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
        }

        //todo Отправка события в активити/фрагмент: Step 4. End
        AppJavaScriptInterface.parseEvent.observe(viewLifecycleOwner, {
            if (!it.hasBeenHandled)
            {
                val content = it.getContent()
                if (!content.isNullOrEmpty())
                {
                    activity?.supportFragmentManager?.let { a -> AddWordDialog.getInstance(content).apply {

                    }.show(a, AddWordDialog.TAG) }
                }
            }
            loadProgress.visibility = View.GONE
        })
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        adsVM.showAd2(requireActivity())
    }

    override fun onKey(p0: View?, p1: Int, p2: KeyEvent?): Boolean
    {
        return false
    }

}