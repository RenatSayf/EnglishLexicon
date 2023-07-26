@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.myapp.lexicon.R
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.settings.askForPermission

class MainFragment : Fragment() {

    companion object {

        private var instance: MainFragment? = null
        private var listener: Listener? = null

        fun getInstance(listener: Listener): MainFragment {

            this.listener = listener
            return if (instance == null) {
                instance = MainFragment()
                instance!!
            }
            else {
                instance!!
            }
        }
    }

    interface Listener {
        fun refreshMainScreen(isAdShow: Boolean)
        fun onVisibleMainScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(getString(R.string.KEY_NEED_REFRESH), listener = { requestKey, bundle ->
            if (requestKey == getString(R.string.KEY_NEED_REFRESH)) {
                listener?.refreshMainScreen(false)
            }
        })

        val launcher = this.registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askForPermission(
                Manifest.permission.POST_NOTIFICATIONS,
                onInit = {
                    ConfirmDialog.newInstance(
                        onLaunch = {dialog, binding ->
                            with(binding) {
                                val message = getString(R.string.text_notification_permission)
                                tvMessage.text = message
                                btnOk.setOnClickListener {
                                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    dialog.dismiss()
                                }
                                btnCancel.setOnClickListener {
                                    dialog.dismiss()
                                }
                            }
                        }
                    ).show(parentFragmentManager, ConfirmDialog.TAG)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()

        listener?.onVisibleMainScreen()
    }

}