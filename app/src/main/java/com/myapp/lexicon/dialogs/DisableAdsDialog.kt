package com.myapp.lexicon.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.DialogDisableAdsBinding
import com.myapp.lexicon.helpers.LockOrientation
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DisableAdsDialog : BottomSheetDialogFragment()
{
    companion object {

        val TAG = "${DisableAdsDialog::class.simpleName}.TAG"
        private var productName: String? = null
        private var productPrice: String? = null
        private var onPurchase: () -> Unit = {}
        private var onCancel: () -> Unit = {}

        fun newInstance(
            productName: String?,
            productPrice: String?,
            onPurchase: () -> Unit,
            onCancel: () -> Unit
        ): DisableAdsDialog {

            this.productName = productName
            this.productPrice = productPrice
            this.onPurchase = onPurchase
            this.onCancel = onCancel
            return DisableAdsDialog()
        }
    }

    private lateinit var binding: DialogDisableAdsBinding
    private var locker: LockOrientation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locker = LockOrientation(requireActivity())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog
    {
        setStyle(STYLE_NO_TITLE, R.style.AppBottomDialog)
        isCancelable = false
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = DialogDisableAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            tvProductName.text = productName
            tvPriceValue.text = productPrice

            btnDisableAds.setOnClickListener {
                onPurchase.invoke()
                dismiss()
            }
            btnCancel.setOnClickListener {
                onCancel.invoke()
                dismiss()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        locker?.lock()
    }

    override fun onDestroy() {

        locker?.unLock()
        super.onDestroy()
    }
}