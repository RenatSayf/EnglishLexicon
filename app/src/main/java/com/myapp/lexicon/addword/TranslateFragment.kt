package com.myapp.lexicon.addword

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.myapp.lexicon.R
import kotlinx.android.synthetic.main.translate_fragment.*
import kotlinx.android.synthetic.main.translate_fragment.view.*
import java.net.URLDecoder

private const val TEXT = "translate_text"

class TranslateFragment : Fragment()
{

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

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
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

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        btnSave.setOnClickListener {
            loadProgress.visibility = View.VISIBLE
            val url = webView.url
            val decode = URLDecoder.decode(url, "UTF-8")
            javaScriptInterface.setInputText(decode)
            //todo parsing WebView: Step 7
            webView?.loadUrl("javascript:window.HtmlHandler.handleHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
        }

        //todo Отправка события в активити/фрагмент: Step 4. End
        AppJavaScriptInterface.parseEvent.observe(viewLifecycleOwner, Observer {
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

}