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
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.settings.saveEmailPasswordToPref

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

            tvSignIn.setOnClickListener {
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
                            tvMessage.text = "На указанный e-mail будет отправлена ссылка для сброса пароля. "
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
                    showSnackBar("Пользователь с таким e-mail уже существует")
                }
                state.onSignUp { user ->
                    showSnackBar("Пользователь зарегистрирован")
                    requireContext().saveEmailPasswordToPref(user.email, user.password)
                    listener?.refreshAuthState(user)
                }
                state.onSignIn { user ->
                    showSnackBar("Вход выполнен")
                    listener?.refreshAuthState(user)
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
                    showSnackBar("Проверьте вашу почту")
                }
                state.onNotRegistered {

                }
                state.onFailure { exception ->
                    exception.printStackTrace()
                    showSnackBar(exception.message?: "Что то пошло не так...")
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
                onEmailNotValid.invoke("Неправильный e-mail")
                return
            }
            val password = etPassword.text.toString()
            when {
                password.isEmpty() -> {
                    onPasswordNotValid.invoke("Пароль не должен быть пустым")
                    return
                }
                password.length < 6 -> {
                    onPasswordNotValid.invoke("Длина пароля должна быть не менее 6 символов")
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