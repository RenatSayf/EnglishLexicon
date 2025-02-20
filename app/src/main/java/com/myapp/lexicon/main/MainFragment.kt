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
import com.myapp.lexicon.aboutapp.showUpdateDialog
import com.myapp.lexicon.aboutapp.showUpdateSnackBar
import com.myapp.lexicon.ads.NATIVE_AD_MAIN
import com.myapp.lexicon.ads.NATIVE_AD_SERVICE
import com.myapp.lexicon.ads.NATIVE_AD_TEST
import com.myapp.lexicon.ads.NATIVE_AD_TRANS
import com.myapp.lexicon.ads.NATIVE_AD_VIDEO
import com.myapp.lexicon.common.IS_IMPORTANT_UPDATE
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.registerFinishReceiver
import com.myapp.lexicon.helpers.setServiceBroadcasts
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

        NATIVE_AD_MAIN
        NATIVE_AD_TRANS
        NATIVE_AD_TEST
        NATIVE_AD_VIDEO
        NATIVE_AD_SERVICE

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
                "********** ${MessagingService::class.simpleName} token = $token ***************".logIfDebug()
            }.addOnFailureListener { exception ->
                exception.printStackTraceIfDebug()
            }
        }

        requireContext().checkAppUpdate(
            onAvailable = {
                if (BuildConfig.IS_IMPORTANT_UPDATE == IS_IMPORTANT_UPDATE) {
                    requireActivity().showUpdateDialog( onClick = {
                        requireContext().setServiceBroadcasts()
                        requireActivity().finish()
                        requireContext().goToAppStore()
                    })
                }
                else {
                    parentFragmentManager.fragments[0].view?.showUpdateSnackBar( onClick = {
                        requireContext().goToAppStore()
                    })
                }
            }
        )

    }

    override fun onStart() {
        super.onStart()

        finishVM.cancelTimer()
        finishReceiver = FinishReceiver().apply {
            sendReferenceToActivity(requireActivity() as MainActivity)
        }
        requireContext().registerFinishReceiver(finishReceiver!!)
    }

    override fun onStop() {

        finishReceiver?.let {
            requireContext().unregisterReceiver(it)
        }
        super.onStop()
    }

}