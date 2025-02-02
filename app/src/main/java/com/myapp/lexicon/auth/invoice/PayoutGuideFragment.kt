package com.myapp.lexicon.auth.invoice

import android.graphics.Bitmap
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.myapp.lexicon.databinding.FragmentPayoutGuideBinding
import com.myapp.lexicon.settings.isAppInstalled

class PayoutGuideFragment : Fragment() {

    companion object {

        private var amount: Int = 0

        fun newInstance(amount: Int): PayoutGuideFragment {
            this.amount = amount
            return PayoutGuideFragment()
        }
    }

    private var binding: FragmentPayoutGuideBinding? = null

    private val viewModel: PayoutGuideViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayoutGuideBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {

            webView.apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                    }

                    override fun onPageFinished(view: WebView?, url: String?) {

                    }
                }
                loadUrl("https://api.englishlexicon.ru/get-self-employed-guide")
            }
        }
    }

    override fun onDestroy() {

        binding = null

        super.onDestroy()
    }
}