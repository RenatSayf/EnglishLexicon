package com.myapp.lexicon.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.aboutapp.checkAppUpdate
import com.myapp.lexicon.aboutapp.showUpdateSnackBar
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.setServiceBroadcasts
import com.myapp.lexicon.helpers.printLogIfDebug
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.registerFinishReceiver
import com.myapp.lexicon.main.viewmodels.FinishViewModel
import com.myapp.lexicon.push.MessagingService
import com.myapp.lexicon.service.FinishReceiver
import com.myapp.lexicon.settings.askForPermission
import com.myapp.lexicon.settings.goToAppStore
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    companion object {

        val TAG = "${MainFragment::class.java.simpleName}.TAG235489"
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
        fun onVisibleMainScreen()
    }

    val finishVM: FinishViewModel by viewModels()
    private var finishReceiver: FinishReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        lifecycleScope.launch {
            finishVM.timeIsUp.collect { result ->
                result.onSuccess { value ->
                    if (value) {
                        requireContext().setServiceBroadcasts()
                        requireActivity().finish()
                    }
                }
            }
        }

        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                printLogIfDebug("********** ${MessagingService::class.simpleName} token = $token ***************")
            }.addOnFailureListener { exception ->
                exception.printStackTraceIfDebug()
            }
        }

        requireContext().checkAppUpdate(
            onAvailable = {
                parentFragmentManager.fragments[0].view?.showUpdateSnackBar(onClick = {
                    requireContext().goToAppStore()
                })
            }
        )

    }

    override fun onStart() {
        super.onStart()

        finishVM.cancelTimer()
        finishReceiver = FinishReceiver(requireActivity() as MainActivity)
        requireContext().registerFinishReceiver(finishReceiver!!)
    }

    override fun onStop() {

        finishReceiver?.let {
            requireContext().unregisterReceiver(it)
        }
        super.onStop()
    }

}