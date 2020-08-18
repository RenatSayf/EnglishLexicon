package com.myapp.lexicon.addword

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import kotlinx.android.synthetic.main.translate_fragment.*
import kotlinx.android.synthetic.main.translate_fragment.view.*

private const val TEXT = "translate_text"

class TranslateFragment : Fragment()
{

    companion object
    {
        private var instance: TranslateFragment? = null
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

    private lateinit var viewModel: TranslateViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val root = inflater.inflate(R.layout.translate_fragment, container, false)

        val inputText = arguments?.getString(TEXT) ?: ""

        root.webView.apply {
            settings.javaScriptEnabled = true //todo parsing WebView: Step 3
            settings.domStorageEnabled = true //todo parsing WebView: Step 4
            addJavascriptInterface(AppJavaScriptInterface(), "HtmlHandler") //todo parsing WebView: Step 5
            webViewClient = AppWebViewClient(this@TranslateFragment) //todo parsing WebView: Step 6
            loadUrl("https://translate.yandex.ru/?text=${inputText}&lang=en-ru")
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TranslateViewModel::class.java)

        btnSave.setOnClickListener {
            loadProgress.visibility = View.VISIBLE
            //todo parsing WebView: Step 7
            webView?.loadUrl("javascript:window.HtmlHandler.handleHtml('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
        }

        //todo Отправка события в активити/фрагмент: Step 4. End
        AppJavaScriptInterface.parseEvent.observe(viewLifecycleOwner, Observer {
            if (!it.hasBeenHandled)
            {
                val content = it.getContent()
                loadProgress.visibility = View.GONE
            }
        })

    }

}