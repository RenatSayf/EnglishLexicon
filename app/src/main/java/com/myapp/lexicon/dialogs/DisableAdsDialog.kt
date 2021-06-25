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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.billing.BillingViewModel

class DisableAdsDialog : DialogFragment()
{
    private lateinit var dialogView: View
    private var btnYes: Button? = null
    private var btnNo: Button? = null
    private var titleView: TextView? = null

    private lateinit var billingVM: BillingViewModel
    private var _isCancel = MutableLiveData(false)
    var isCancel: LiveData<Boolean> = _isCancel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        billingVM = ViewModelProvider(this)[BillingViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.disable_ads_layout, ConstraintLayout(requireContext()), false)
        btnYes = dialogView.findViewById(R.id.btnYes)
        btnNo = dialogView.findViewById(R.id.btnNo)
        titleView = dialogView.findViewById(R.id.titleTV)

        return AlertDialog.Builder(requireContext()).apply {
            setView(dialogView)
        }.create().apply {
            this.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            btnYes?.setOnClickListener {
                billingVM.disableAds(requireActivity())
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
        billingVM.priceText.observe(viewLifecycleOwner, {
            val text = titleView?.text.toString() + " $it"
            titleView?.text = text
        })
        return dialogView
    }

}