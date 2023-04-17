package com.myapp.lexicon.cloudstorage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        private var instance: StorageDialog? = null

        fun newInstance(listener: Listener): StorageDialog {

            this.listener = listener
            return if (instance == null) {
                instance = StorageDialog()
                instance!!
            } else instance!!
        }
    }

    private lateinit var binding: DialogStorageBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {

        isCancelable = false
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

            listener?.onLaunch(this)

            btnCloudEnable.setOnClickListener {
                listener?.onPositiveClick()
                dismiss()
            }

            btnCancel.setOnClickListener {
                listener?.onCancelClick()
                dismiss()
            }
        }
    }

    override fun onDestroy() {
        listener?.onDestroy()
        super.onDestroy()
    }

    interface Listener {
        fun onLaunch(binding: DialogStorageBinding)
        fun onDestroy()
        fun onPositiveClick()
        fun onCancelClick()
    }

}
