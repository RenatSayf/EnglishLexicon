@file:Suppress("ObjectLiteralToLambda", "RedundantSamConstructor")

package com.myapp.lexicon.auth.agreement

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.fragment.app.DialogFragment
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.DialogUserAgreementBinding

class UserAgreementDialog: DialogFragment() {

    companion object {
        val TAG = "${UserAgreementDialog::class.java.simpleName}.TAG"
        private var onPositiveClick: () -> Unit = {}

        fun newInstance(
            onPositiveClick: () -> Unit
        ): UserAgreementDialog {

            this.onPositiveClick = onPositiveClick
            return UserAgreementDialog()
        }
    }

    private lateinit var binding: DialogUserAgreementBinding
    private lateinit var dialog: Dialog

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogUserAgreementBinding.inflate(layoutInflater)

        binding.webView.apply {
            settings.javaScriptEnabled = true
            addJavascriptInterface(JsInterface(), "Android")
            val url = "file:///android_asset/user_agreement/index.html"
            loadUrl(url)
        }

        val builder = AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
            isCancelable = false
            setStyle(STYLE_NO_TITLE, R.style.AppAlertDialog)
            setPositiveButton(getString(R.string.text_to_accept), object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    onPositiveClick.invoke()
                    dismiss()
                }
            })
            setNegativeButton(getString(R.string.text_cancel), object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    dismiss()
                }
            })
        }
        dialog = builder.create().apply {
            this.window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog_white)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }

    inner class JsInterface {

        @JavascriptInterface
        fun onCheckedListener(flag: Boolean) {

            requireActivity().runOnUiThread(Runnable {
                val positiveButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.isEnabled = flag
            })
        }
    }


}



















