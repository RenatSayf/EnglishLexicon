@file:Suppress("RedundantSamConstructor")

package com.myapp.lexicon.auth.payout

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.common.base.Utf8
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentPayoutBinding
import io.ktor.http.headers
import java.net.URLEncoder
import java.util.Base64.Encoder

class PayoutFragment : Fragment() {

    companion object {
        fun newInstance() = PayoutFragment()
    }

    private lateinit var binding: FragmentPayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            val clientId = "XXXX"
            val redirectUrl = ""
            val scope = "account-info"
            val postData = "client_id=$clientId&response_type=code&redirect_uri=$redirectUrl&scope=$scope"

            webView.apply {
                settings.javaScriptEnabled = true
                addJavascriptInterface(JsInterface(), "Android")
                val byteArray = URLEncoder.encode(postData, "UTF-8").toByteArray()
                postUrl("https://yoomoney.ru/", byteArray)
            }
            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                }

                override fun onPageFinished(view: WebView?, url: String?) {

                }

                override fun onLoadResource(view: WebView?, url: String?) {

                }
            }
        }
    }

    inner class JsInterface {
        @JavascriptInterface
        fun onInitListener() {

            requireActivity().runOnUiThread(Runnable {

            })
        }
    }

}