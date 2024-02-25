package com.myapp.lexicon.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.DialogConfirmationBinding
import com.myapp.lexicon.helpers.LockOrientation

class ConfirmDialog : DialogFragment() {

    companion object {
        val TAG = "${ConfirmDialog::class.java.simpleName}.TAG"
        private var onLaunch: (ConfirmDialog, DialogConfirmationBinding) -> Unit = { _, _ -> }

        fun newInstance(onLaunch: (ConfirmDialog, DialogConfirmationBinding) -> Unit): ConfirmDialog {

            this.onLaunch = onLaunch
            return ConfirmDialog()
        }
    }

    private lateinit var binding: DialogConfirmationBinding
    private val locker: LockOrientation by lazy {
        LockOrientation(requireActivity())
    }

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

        locker.lock()
        binding = DialogConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onLaunch.invoke(this, this.binding)
    }

    override fun onDestroyView() {

        locker.unLock()
        super.onDestroyView()
    }


}