package com.myapp.lexicon.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.billing.BillingViewModel

class DisableAdsDialog : DialogFragment()
{
    private lateinit var dialogView: View
    private var btnYes: Button? = null
    private var btnNo: Button? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.disable_ads_layout, ConstraintLayout(requireContext()), false)
        btnYes = dialogView.findViewById(R.id.btnYes)
        btnNo = dialogView.findViewById(R.id.btnNo)



        return AlertDialog.Builder(requireContext()).apply {
            setView(dialogView)
        }.create().apply {
            this.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val billingVM = ViewModelProvider(this)[BillingViewModel::class.java]

        btnYes?.setOnClickListener {
            billingVM.disableAds(requireActivity())
            dismiss()
        }
        btnNo?.setOnClickListener {
            dismiss()
        }
    }

}