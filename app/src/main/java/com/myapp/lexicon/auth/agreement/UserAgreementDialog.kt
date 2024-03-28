@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.auth.agreement

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.DialogFragment
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.DialogUserAgreementBinding
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.helpers.toStringDateDDMonthYYYY
import com.parse.ParseUser

class UserAgreementDialog: DialogFragment() {

    companion object {
        val TAG = "${UserAgreementDialog::class.java.simpleName}.TAG"
        private var onPositiveClick: () -> Unit = {}
        private var isCancelable: Boolean = true

        fun newInstance(
            isCancelable: Boolean = true,
            onPositiveClick: () -> Unit
        ): UserAgreementDialog {

            this.isCancelable = isCancelable
            this.onPositiveClick = onPositiveClick
            return UserAgreementDialog()
        }
    }

    private lateinit var binding: DialogUserAgreementBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogUserAgreementBinding.inflate(layoutInflater)

        binding.progressBar.visibility = View.VISIBLE

        binding.webView.apply {
            settings.javaScriptEnabled = true
            val url = "file:///android_asset/user_agreement/index.html"
            loadUrl(url)

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    binding.progressBar.visibility = View.GONE
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        val currentUser = ParseUser.getCurrentUser()
        if (currentUser != null) {
            val strDate = "${binding.tvDateTitle.text} ${currentUser.createdAt.time.toStringDateDDMonthYYYY()}"
            binding.tvDateTitle.text = strDate
        }
        else {
            val strDate = "${binding.tvDateTitle.text} ${System.currentTimeMillis().toStringDateDDMonthYYYY()}"
            binding.tvDateTitle.text = strDate
        }

        val builder = AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
            isCancelable = false
            setStyle(STYLE_NO_TITLE, R.style.AppAlertDialog)

            val buttonText = if (currentUser != null) getString(R.string.text_close) else getString(R.string.text_to_accept)

            setPositiveButton(buttonText, object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    onPositiveClick.invoke()
                    dismiss()
                }
            })
            if (UserAgreementDialog.isCancelable) {
                if (currentUser == null) {
                    setNegativeButton(getString(R.string.text_cancel), object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            dismiss()
                        }
                    })
                }
            }
        }
        return builder.create().apply {
            this.window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog_white)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().orientationLock()
        return binding.root
    }

    override fun onDestroyView() {

        requireActivity().orientationUnLock()
        super.onDestroyView()
    }


}



















