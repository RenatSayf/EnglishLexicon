package com.myapp.lexicon.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.aboutapp.checkAppUpdate
import com.myapp.lexicon.aboutapp.showUpdateDialog
import com.myapp.lexicon.aboutapp.showUpdateSnackBar
import com.myapp.lexicon.ads.models.AD_MAIN
import com.myapp.lexicon.ads.models.AD_SERVICE
import com.myapp.lexicon.ads.models.AD_TEST
import com.myapp.lexicon.ads.models.AD_TRANSLATE
import com.myapp.lexicon.ads.models.AD_VIDEO
import com.myapp.lexicon.auth.AuthViewModel
import com.myapp.lexicon.auth.account.UserDataViewModel
import com.myapp.lexicon.common.IS_IMPORTANT_UPDATE
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.registerFinishReceiver
import com.myapp.lexicon.helpers.setServiceBroadcasts
import com.myapp.lexicon.helpers.throwIfDebug
import com.myapp.lexicon.main.viewmodels.FinishViewModel
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.models.UserX
import com.myapp.lexicon.push.MessagingService
import com.myapp.lexicon.service.FinishReceiver
import com.myapp.lexicon.settings.accessToken
import com.myapp.lexicon.settings.askForPermission
import com.myapp.lexicon.settings.clearEmailPasswordInPref
import com.myapp.lexicon.settings.emailIntoPref
import com.myapp.lexicon.settings.getAuthDataFromPref
import com.myapp.lexicon.settings.goToAppStore
import com.myapp.lexicon.settings.passwordIntoPref
import com.myapp.lexicon.settings.saveAuthTokens
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
        fun onMigrationFromBack4AppCompleted(user: UserX)
        fun onFetchUserData(user: UserX)
        fun onFirstLaunch()
        fun onAuthError(message: String)
    }

    val finishVM: FinishViewModel by viewModels()
    private var finishReceiver: FinishReceiver? = null

    private val authVM: AuthViewModel by lazy {
        val authFactory = AuthViewModel.Factory()
        ViewModelProvider(this, authFactory)[AuthViewModel::class]
    }

    private val userDataVM: UserDataViewModel by lazy {
        val factory = UserDataViewModel.Factory()
        ViewModelProvider(this, factory)[UserDataViewModel::class]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AD_MAIN
        AD_TRANSLATE
        AD_TEST
        AD_VIDEO
        AD_SERVICE

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

        authVM.state.observe(this) { state ->
            when(state) {
                is UserState.Failure -> {

                }
                is UserState.HttpFailure -> {

                }
                is UserState.SignIn -> { // after login in back4app
                    requireContext().getAuthDataFromPref(
                        onSuccess = { email, password ->
                            authVM.registerForNewUser(email, password) // log up in timeweb
                        }
                    )
                }
                is UserState.LogUp -> { // after log up in timeweb
                    requireContext().saveAuthTokens(state.tokens)
                    val user = authVM.user
                    if (user != null) {
                        userDataVM.updateUserData(
                            token = state.tokens.accessToken,
                            data = user.toUserX()
                        )
                    } else {
                        Exception("***** UserX is Null *******").throwIfDebug()
                    }
                }
                else -> {}
            }
        }

        userDataVM.userState.observe(this) { state ->
            when(state) {
                is UserDataViewModel.UserDataState.Error -> {
                    val error = state.message
                    listener?.onAuthError(message = error)
                }
                is UserDataViewModel.UserDataState.ReceivedUserData -> {
                    val user = state.user
                    listener?.onFetchUserData(user)
                }
                UserDataViewModel.UserDataState.Init -> {}
                is UserDataViewModel.UserDataState.UserDataUpdated -> {
                    requireContext().clearEmailPasswordInPref()
                    val userX = state.userX
                    listener?.onMigrationFromBack4AppCompleted(userX)
                }
                else -> {}
            }
        }

        requireContext().getAuthDataFromPref(
            onSuccess = { email, password ->
                requireContext().emailIntoPref = email
                requireContext().passwordIntoPref = password
                authVM.signInWithEmailAndPassword(email = email, password = password) //login in back4app
            },
            onNotRegistered = {
                val token = requireContext().accessToken
                if (token.isNotEmpty()) {
                    userDataVM.fetchUserData(token)
                } else {
                    listener?.onFirstLaunch()
                }
            },
            onFailure = { exception ->
                exception.printStackTraceIfDebug()
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

    override fun onResume() {
        super.onResume()


    }

}