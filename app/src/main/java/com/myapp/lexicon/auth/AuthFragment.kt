package com.myapp.lexicon.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.auth.account.AccountFragment
import com.myapp.lexicon.auth.agreement.UserAgreementDialog
import com.myapp.lexicon.databinding.FragmentAuthBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.isItEmail
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.models.Tokens
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.settings.getAuthDataFromPref
import com.myapp.lexicon.settings.saveAuthTokens
import com.myapp.lexicon.settings.saveUserToPref

class AuthFragment : Fragment() {

    companion object {

        val TAG = "${AuthFragment::class.java.simpleName}.TAG"
        fun newInstance(): AuthFragment {
            return AuthFragment()
        }
    }

    private lateinit var binding: FragmentAuthBinding
    private val authVM: AuthViewModel by lazy {
        val factory = AuthViewModel.Factory()
        ViewModelProvider(this, factory)[AuthViewModel::class.java]
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

            authVM.screenState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AuthViewModel.ScreenState.Init -> {
                        requireContext().getAuthDataFromPref(
                            onSuccess = {email, password ->
                                etEmail.setText(email)
                                etPassword.setText(password)
                                progressBar.visibility = View.GONE
                            }
                        )
                    }
                    is AuthViewModel.ScreenState.Current -> {
                        etEmail.apply {
                            setText(state.emailText)
                            if (state.emailIsFocused) {
                                requestFocus()
                                setSelection(text?.length?: 0)
                            }
                            background = state.emailBackground
                        }
                        etPassword.apply {
                            setText(state.passwordText)
                            if (state.passwordIsFocused) {
                                requestFocus()
                                setSelection(text?.length?: 0)
                            }
                            background = state.passwordBackground
                        }
                        btnSignIn.apply {
                            isEnabled = state.btnSignInEnable
                        }
                        btnRegistration.apply {
                            isEnabled = state.btnSignUpEnable
                        }
                    }
                }
            }

            btnRegistration.setOnClickListener {
                validateEmailAndPassword(
                    onSuccess = { email, password ->
                        UserAgreementDialog.newInstance(
                            onPositiveClick = {
                                authVM.registerForNewUser(email, password)
                            }
                        ).show(parentFragmentManager, UserAgreementDialog.TAG)

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
                if (!text.isNullOrBlank()) {
                    val result = authVM.isValidEmail(text.toString())
                    if (result) {
                        authVM.setState(UserState.EmailValid(true))
                    }
                    else authVM.setState(UserState.EmailValid(false))
                }
            }
            etPassword.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrBlank()) {
                    if (text.toString().length >= 6) {
                        authVM.setState(UserState.PasswordValid(true))
                    }
                    else {
                        authVM.setState(UserState.PasswordValid(false))
                    }
                }
            }

            btnSignIn.setOnClickListener {
                validateEmailAndPassword(
                    onSuccess = { email, password ->
                        authVM.logInWithEmailAndPassword(email, password)
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
                val isValidEmail = authVM.isValidEmail(etEmail.text.toString())
                if (isValidEmail) {
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
                else {
                    etEmail.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_horizontal_oval_error, null)
                }
            }

            authVM.loadingState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AuthViewModel.LoadingState.Complete -> {
                        progressBar.visibility = View.GONE
                    }
                    AuthViewModel.LoadingState.Start -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    else -> {}
                }
            }

            authVM.state.observe(viewLifecycleOwner) { state ->
                state.onExists {
                    etPassword.text?.clear()
                    showSnackBar(getString(R.string.text_such_user_already_exists))
                }
                state.onUnAuthorized {
                    showSnackBar(getString(R.string.text_password_incorrect))
                }
                state.onSignUp { user ->
                    showSnackBar(getString(R.string.text_user_is_registered))
                    handleAuthorization(user)
                }
                state.onSignIn { user ->
                    showSnackBar(getString(R.string.text_login_completed))
                    handleAuthorization(user)
                }
                state.onLogUp { tokens: Tokens ->
                    showSnackBar(getString(R.string.text_user_is_registered))
                    requireContext().saveAuthTokens(tokens)
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
                        showSnackBar(getString(R.string.text_short_password))
                    }
                }
                state.onPasswordReset {
                    etPassword.text?.clear()
                    btnRegistration.visibility = View.GONE
                    showSnackBar(getString(R.string.text_check_email))
                }
                state.onNotRegistered {
                    etPassword.text?.clear()
                    btnRegistration.visibility = View.VISIBLE
                    showSnackBar(getString(R.string.text_user_not_found))
                }
                state.onFailure { exception ->
                    exception.printStackTrace()
                    showSnackBar(exception.message?: getString(R.string.text_unknown_error_message))
                }
                state.onHttpFailure { message: String? ->
                    showSnackBar(message?: getString(R.string.text_unknown_error_message))
                }
                state.onTokensUpdated { tokens: Tokens ->
                    requireContext().saveAuthTokens(tokens)
                }
            }
        }
    }

    private fun handleAuthorization(user: User) {
        with(binding) {

            val password = etPassword.text.toString()
            requireContext().saveUserToPref(user.apply {
                this.password = password
            })

            val accountFragment = AccountFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_to_page_fragm, accountFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun validateEmailAndPassword(
        onSuccess: (String, String) -> Unit,
        onEmailNotValid: (String) -> Unit,
        onPasswordNotValid: (String) -> Unit = {}
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

    override fun onPause() {

        with(binding) {
            authVM.setScreenState(
                AuthViewModel.ScreenState.Current(
                    emailText = etEmail.text.toString(),
                    emailIsFocused = etEmail.isFocused,
                    emailBackground = etEmail.background,
                    passwordText = etPassword.text.toString(),
                    passwordIsFocused = etPassword.isFocused,
                    passwordBackground = etPassword.background,
                    btnSignInEnable = btnSignIn.isEnabled,
                    btnSignUpEnable = btnRegistration.isEnabled
                )
            )
        }
        super.onPause()
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