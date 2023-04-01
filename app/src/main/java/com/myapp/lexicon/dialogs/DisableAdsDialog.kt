package com.myapp.lexicon.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.R
import com.myapp.lexicon.billing.BillingViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DisableAdsDialog : DialogFragment()
{
    private lateinit var dialogView: View
    private var btnYes: Button? = null
    private var btnNo: Button? = null
    private var titleView: TextView? = null

    private val billingVM: BillingViewModel by activityViewModels()
    private var _isCancel = MutableLiveData(false)
    var isCancel: LiveData<Boolean> = _isCancel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        dialogView = layoutInflater.inflate(R.layout.disable_ads_layout, ConstraintLayout(requireContext()), false)
        btnYes = dialogView.findViewById(R.id.btnYes)
        btnNo = dialogView.findViewById(R.id.btnNo)
        titleView = dialogView.findViewById(R.id.titleTV)

        return AlertDialog.Builder(requireContext()).apply {
            setView(dialogView)
        }.create().apply {
            this.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)

            btnYes?.setOnClickListener {

                billingVM.noAdsProduct.value?.let {
                    it.onSuccess { details ->
                        billingVM.purchaseProduct(requireActivity(), details)
                    }
                }
                dismiss()
            }
            btnNo?.setOnClickListener {
                _isCancel.value = true
                dismiss()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        return dialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnYes?.isEnabled = false

        billingVM.noAdsProduct.observe(viewLifecycleOwner) { result ->
            result.onSuccess { details ->
                val price = details.oneTimePurchaseOfferDetails?.formattedPrice
                val text = titleView?.text.toString() + " $price"
                titleView?.text = text

                btnYes?.isEnabled = true
            }


        }
    }

}