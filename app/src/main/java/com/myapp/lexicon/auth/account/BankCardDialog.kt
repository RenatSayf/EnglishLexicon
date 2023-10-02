package com.myapp.lexicon.auth.account

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.DialogBankCardBinding

class BankCardDialog: DialogFragment() {

    companion object {
        val TAG = "${BankCardDialog::class.java.simpleName}.Tag"
        private var onLaunch: (BankCardDialog, DialogBankCardBinding) -> Unit = { _, _ ->  }

        fun newInstance(onLaunch: (BankCardDialog, DialogBankCardBinding) -> Unit): BankCardDialog {
            this.onLaunch = onLaunch
            return BankCardDialog()
        }
    }

    private lateinit var binding: DialogBankCardBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        setStyle(STYLE_NO_TITLE, R.style.AppAlertDialog)
        isCancelable = false
        return super.onCreateDialog(savedInstanceState).apply {
            this.window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBankCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onLaunch.invoke(this, this.binding)

    }

}