package com.myapp.lexicon.cloudstorage

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.DialogStorageBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StorageDialog : BottomSheetDialogFragment()
{
    companion object
    {
        val TAG = "${StorageDialog::class.simpleName}.TAG"
        private var listener: Listener? = null
        fun newInstance(listener: Listener): StorageDialog {

            this.listener = listener
            return StorageDialog()
        }
    }

    private lateinit var binding: DialogStorageBinding
    private val viewModel: StorageViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {

        setStyle(STYLE_NO_TITLE, R.style.AppBottomDialog)
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            tvPriceValue.text = "RUB 100"

            btnCloudEnable.setOnClickListener {
                listener?.onRestoreClick()
                dismiss()
            }

            btnCancel.setOnClickListener {
                listener?.onCancelClick()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {

        listener?.onCancelClick()
        super.onDestroyView()
    }

    interface Listener {
        fun onRestoreClick()
        fun onCancelClick()
    }

}
