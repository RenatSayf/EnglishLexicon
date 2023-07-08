package com.myapp.lexicon.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentAuthBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.isItEmail
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.settings.saveUserToPref

class AuthFragment : Fragment() {

    companion object {

        val TAG = "${AuthFragment::class.java.simpleName}.TAG"
        private var listener: AuthListener? = null
        fun newInstance(listener: AuthListener): AuthFragment {
            this.listener = listener
            return AuthFragment()
        }
    }

    private lateinit var binding: FragmentAuthBinding
    private val authVM: AuthViewModel by viewModels()
    private val userVM: UserViewModel by viewModels()

    interface AuthListener {
        fun refreshAuthState(user: User)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            btnRegistration.setOnClickListener {
                validateEmailAndPassword(
                    onSuccess = { email, password ->
                        authVM.registerWithEmailAndPassword(email, password)
                    },
                    onEmailNotValid = { message ->
                        showSnackBar(message)
                        authVM.setState(UserState.EmailValid(false))
                    },
                    onPasswordNotValid = { message ->
                        showSnackBar(message)
                        authVM.setState(UserState.PasswordValid(false))
                    }
                )
            }

            etEmail.doOnTextChanged { text, _, _, _ ->
                val result = authVM.isValidEmail(text.toString())
                if (result) {
                    authVM.setState(UserState.EmailValid(true))
                }
                else authVM.setState(UserState.EmailValid(false))
            }
            etPassword.doOnTextChanged { text, _, _, _ ->
                if (text.toString().length >= 6) {
                    authVM.setState(UserState.PasswordValid(true))
                }
                else {
                    authVM.setState(UserState.PasswordValid(false))
                }
            }

            btnSignIn.setOnClickListener {
                validateEmailAndPassword(
                    onSuccess = { email, password ->
                        authVM.signInWithEmailAndPassword(email, password)
                    },
                    onEmailNotValid = { message ->
                        showSnackBar(message)
                        authVM.setState(UserState.EmailValid(false))
                    },
                    onPasswordNotValid = { message ->
                        showSnackBar(message)
                        authVM.setState(UserState.PasswordValid(false))
                    }
                )
            }

            tvResetPassword.setOnClickListener {
                ConfirmDialog.newInstance(
                    onLaunch = { dialog, binding ->
                        with(binding) {
                            tvEmoji.visibility = View.GONE
                            tvEmoji2.visibility = View.GONE
                            tvMessage.text = getString(R.string.text_email_will_be_sent_to)
                            btnCancel.setOnClickListener {
                                dialog.dismiss()
                            }
                            btnOk.setOnClickListener {
                                val text = etEmail.text.toString()
                                if (text.isItEmail) {
                                    authVM.resetPassword(text)
                                }
                                dialog.dismiss()
                            }
                        }
                    }
                ).show(parentFragmentManager, ConfirmDialog.TAG)
            }

            authVM.loadingState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AuthViewModel.LoadingState.Complete -> {
                        progressBar.visibility = View.GONE
                    }
                    AuthViewModel.LoadingState.Start -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }

            authVM.state.observe(viewLifecycleOwner) { state ->
                state.onExists {
                    etPassword.text?.clear()
                    showSnackBar(getString(R.string.text_such_user_already_exists))
                }
                state.onSignUp { user ->
                    showSnackBar(getString(R.string.text_user_is_registered))
                    userVM.addUserIfNotExists(user)
                    requireContext().saveUserToPref(user)
                    listener?.refreshAuthState(user)
                    parentFragmentManager.popBackStack()
                }
                state.onSignIn { user ->
                    showSnackBar(getString(R.string.text_login_completed))
                    requireContext().saveUserToPref(user)
                    listener?.refreshAuthState(user)
                    parentFragmentManager.popBackStack()
                }
                state.onEmailValid { flag ->
                    if (flag) {
                        etEmail.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_horizontal_oval, null)
                    }
                    else {
                        etEmail.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_horizontal_oval_error, null)
                    }
                }
                state.onPasswordValid { flag ->
                    if (flag) {
                        etPassword.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_horizontal_oval, null)
                    }
                    else {
                        etPassword.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_horizontal_oval_error, null)
                    }
                }
                state.onPasswordReset {
                    etPassword.text?.clear()
                    btnRegistration.visibility = View.GONE
                    showSnackBar(getString(R.string.text_check_email))
                }
                state.onNotRegistered {
                    btnRegistration.visibility = View.VISIBLE
                }
                state.onFailure { exception ->
                    exception.printStackTrace()
                    showSnackBar(exception.message?: getString(R.string.text_unknown_error_message))
                }
            }
        }
    }

    private fun validateEmailAndPassword(
        onSuccess: (String, String) -> Unit,
        onEmailNotValid: (String) -> Unit,
        onPasswordNotValid: (String) -> Unit
    ) {
        with(binding) {
            val email = etEmail.text.toString()
            val result = authVM.isValidEmail(email)
            if (!result) {
                onEmailNotValid.invoke(getString(R.string.text_incorrect_email))
                return
            }
            val password = etPassword.text.toString()
            when {
                password.isEmpty() -> {
                    onPasswordNotValid.invoke(getString(R.string.text_empty_password_message))
                    return
                }
                password.length < 6 -> {
                    onPasswordNotValid.invoke(getString(R.string.text_wrong_password_length))
                    return
                }
                else -> {
                    onSuccess.invoke(email, password)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.toolBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.popBackStack()
                }
            })
    }

}