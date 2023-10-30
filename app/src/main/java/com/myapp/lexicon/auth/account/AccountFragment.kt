@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.auth.AuthListener
import com.myapp.lexicon.auth.AuthViewModel
import com.myapp.lexicon.auth.MockAuthViewModel
import com.myapp.lexicon.databinding.FragmentAccountBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.LuhnAlgorithm
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.main.viewmodels.MockUserViewModel
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.models.ViewState
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.settings.clearEmailPasswordInPref
import com.myapp.lexicon.settings.isFirstLogin
import com.parse.ParseUser
import kotlinx.coroutines.launch


class AccountFragment : Fragment() {

    companion object {

        private lateinit var viewModelClasses: List<Class<out ViewModel>>

        private var listener: AuthListener? = null
        fun newInstance(
            viewModelClasses: List<Class<out ViewModel>> = listOf(
                AccountViewModel::class.java,
                AuthViewModel::class.java,
                UserViewModel::class.java
            )
        ): AccountFragment {

            this.viewModelClasses = viewModelClasses
            return AccountFragment()
        }
    }

    private lateinit var binding: FragmentAccountBinding

    private val accountVM: AccountViewModel by lazy {
        val clazz = viewModelClasses.first {
            it == AccountViewModel::class.java || it == MockAccountViewModel::class.java
        }
        val modelLazy = if (clazz == AccountViewModel::class.java) {
            viewModels<AccountViewModel>()
        } else {
            viewModels<MockAccountViewModel>()
        }
        modelLazy.value
    }

    private val authVM: AuthViewModel by lazy {
        val clazz = viewModelClasses.first {
            it == AuthViewModel::class.java || it == MockAuthViewModel::class.java
        }
        val modelLazy = if (clazz == AuthViewModel::class.java) {
            activityViewModels<AuthViewModel>()
        } else {
            activityViewModels<MockAuthViewModel>()
        }
        modelLazy.value
    }

    private val userVM: UserViewModel by lazy {
        val clazz = viewModelClasses.first {
            it == UserViewModel::class.java || it == MockUserViewModel::class.java
        }
        val modelLazy = if (clazz == UserViewModel::class.java) {
            activityViewModels<UserViewModel>()
        } else {
            activityViewModels<MockUserViewModel>()
        }
        modelLazy.value
    }

    private val screenOrientation: LockOrientation by lazy {
        LockOrientation(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = requireActivity() as MainActivity

        with(binding) {

            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    userVM.loadingState.collect { state ->
                        when(state) {
                            UserViewModel.LoadingState.Complete -> {
                                progressBar.visibility = View.GONE
                            }
                            UserViewModel.LoadingState.Start -> {
                                progressBar.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            val editTextList = listOf(
                tvEmailValue,
                tvPhoneValue,
                tvBankNameValue,
                tvCardNumber,
                tvFirstNameValue,
                tvLastNameValue
            )

            accountVM.screenState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AccountScreenModel.Init -> {
                        userVM.getUserFromCloud()
                    }
                    is AccountScreenModel.Current -> {
                        tvRewardValue.text = state.reward
                        groupReward.visibility = state.groupRewardVisibility
                        tvEmailValue.apply {
                            setText(state.emailState.text)
                            background = state.emailState.background
                            if (state.emailState.isFocused) {
                                requestFocus()
                                setSelection(this.text?.length?: 0)
                            }
                        }
                        tvPhoneValue.apply {
                            setText(state.phone)
                            background = state.phoneBackground
                        }
                        tvBankNameValue.apply {
                            setText(state.bankName)
                            background = state.bankNameBackground
                        }
                        tvCardNumber.apply {
                            setText(state.cardNumber)
                            background = state.cardNumberBackground
                        }
                        tvFirstNameValue.apply {
                            setText(state.firstName)
                            background = state.firstNameBackground
                        }
                        tvLastNameValue.apply {
                            setText(state.lastName)
                            background = state.lastNameBackground
                        }
                        btnSave.apply {
                            visibility = state.btnSaveVisibility
                        }
                        btnGetReward.apply {
                            isEnabled = state.btnRewardEnable
                        }
                        tvRewardCondition.apply {
                            text = state.rewardCondition
                        }
                    }
                }
            }

            accountVM.bankList.observe(viewLifecycleOwner) { result ->
                result.onSuccess { list ->
                    val bankListAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
                    tvBankNameValue.setAdapter(bankListAdapter)
                }
                result.onFailure { exception ->
                    showSnackBar(exception.message?: getString(R.string.text_unknown_error_message))
                }
            }

            val currentUser = ParseUser.getCurrentUser()
            userVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    UserViewModel.State.Init -> {}
                    is UserViewModel.State.PersonalDataUpdated -> {
                        showSnackBar(getString(R.string.data_is_saved))
                        if (currentUser != null) {
                            userVM.getUserFromCloud()
                        }
                        accountVM.setState(AccountViewModel.State.ReadOnly)
                    }
                    is UserViewModel.State.PaymentRequestSent -> {
                        showConfirmDialog()
                        if (currentUser != null) {
                            userVM.getUserFromCloud()
                        }
                    }
                    is UserViewModel.State.Error -> {
                        showSnackBar(state.message)
                    }
                    is UserViewModel.State.ReceivedUserData -> {
                        requireContext().isFirstLogin(
                            onYes = {
                                showInfoDialog()
                            }
                        )
                        handleUserData(state.user)
                        userVM.setState(UserViewModel.State.UserAdded(state.user))
                    }
                    else -> {}
                }
            }

            accountVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AccountViewModel.State.Editing -> {
                        editTextList.forEach {
                            it.isEnabled = true
                        }
                        val editText = editTextList.firstOrNull {
                            it.text.isEmpty()
                        }
                        editText?.requestFocus() ?: run {
                            tvFirstNameValue.apply {
                                requestFocus()
                                setSelection(this.text?.length?: 0)
                            }
                        }
                        btnSave.visibility = View.VISIBLE
                    }
                    AccountViewModel.State.ReadOnly -> {
                        editTextList.forEach {
                            it.isEnabled = false
                        }
                        btnSave.visibility = View.GONE
                    }
                }
            }

            tvEmailValue.doOnTextChanged { text, start, before, count ->
                val isValid = authVM.isValidEmail(text.toString())
                if (text.isNullOrEmpty() || !isValid) {
                    tvEmailValue.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_horizontal_oval_error, null)
                }
                else {
                    tvEmailValue.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_horizontal_oval, null)
                }
            }
            tvPhoneValue.doOnTextChanged { text, start, before, count ->
                val digits = text?.filter {
                    it.isDigit()
                }
                val isPhoneNumber = Regex("^[+]?[0-9]{10,13}$").matches(digits?: "")
                if (isPhoneNumber || digits.isNullOrEmpty()) {
                    tvPhoneValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval)
                }
                else tvPhoneValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
            }
            tvBankNameValue.doOnTextChanged { text, start, before, count ->
                if (text.toString().isEmpty()) {
                    tvBankNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval)
                }
                else if (!text.isNullOrEmpty() && text.toString().length > 1) {
                    tvBankNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval)
                }
                else tvBankNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
            }
            tvCardNumber.doOnTextChanged { text, start, before, count ->
                val number = tvCardNumber.text.toString()
                val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                if (isValidNumber || number.isEmpty()) {
                    tvCardNumber.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval)
                }
                else tvCardNumber.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
            }
            tvFirstNameValue.doOnTextChanged { text, start, before, count ->
                if (text.isNullOrEmpty()) {
                    tvFirstNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval)
                }
                else if (text.isNotEmpty() && text.toString().length > 1) {
                    tvFirstNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval)
                }
                else tvFirstNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
            }
            tvLastNameValue.doOnTextChanged { text, start, before, count ->
                if (text.isNullOrEmpty()) {
                    tvLastNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval)
                }
                else if (text.isNotEmpty() && text.toString().length > 1) {
                    tvLastNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval)
                }
                else tvLastNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
            }

            btnSave.setOnClickListener {
                val user = userVM.user.value
                if (user != null) {
                    val editText = editTextList.firstOrNull {
                        it.background.constantState == ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.bg_horizontal_oval_error,
                            null
                        )?.constantState
                    }
                    if (editText != null) {
                        showSnackBar(getString(R.string.text_form_incorrect))
                        return@setOnClickListener
                    }
                    val userMap = mapOf<String, Any>(
                        User.KEY_EMAIL to tvEmailValue.text.toString(),
                        User.KEY_PHONE to tvPhoneValue.text.toString(),
                        User.KEY_BANK_NAME to tvBankNameValue.text.toString(),
                        User.KEY_BANK_CARD to tvCardNumber.text.toString(),
                        User.KEY_FIRST_NAME to tvFirstNameValue.text.toString(),
                        User.KEY_LAST_NAME to tvLastNameValue.text.toString()
                    )
                    userVM.updateUserDataIntoCloud(userMap)
                    accountVM.setState(AccountViewModel.State.ReadOnly)
                }
            }

            btnGetReward.setOnClickListener {
                val user = userVM.user.value
                if (user != null) {
                    val email = tvEmailValue.text.toString()
                    if (email.isEmpty() || !authVM.isValidEmail(email)) {
                        tvEmailValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
                        return@setOnClickListener
                    }

                    if (tvPhoneValue.text.isNullOrEmpty() || (tvPhoneValue.text?.length?: 0) < 11) {
                        tvPhoneValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
                        return@setOnClickListener
                    }

                    val number = tvCardNumber.text.toString()
                    val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                    if (!isValidNumber) {
                        tvCardNumber.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
                        return@setOnClickListener
                    }

                    if (tvBankNameValue.text.isNullOrEmpty()) {
                        tvBankNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
                        return@setOnClickListener
                    }

                    if (tvFirstNameValue.text.isNullOrEmpty()) {
                        tvFirstNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
                        return@setOnClickListener
                    }

                    if (tvLastNameValue.text.isNullOrEmpty()) {
                        tvLastNameValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
                        return@setOnClickListener
                    }

                    userVM.updatePayoutDataIntoCloud(
                        threshold = (accountVM.paymentThreshold * user.currencyRate).toInt(),
                        reward = user.userReward,
                        userMap = mapOf(
                            User.KEY_BANK_CARD to tvCardNumber.text.toString(),
                            User.KEY_BANK_NAME to tvBankNameValue.text.toString(),
                            User.KEY_PHONE to tvPhoneValue.text.toString(),
                            User.KEY_FIRST_NAME to tvFirstNameValue.text.toString(),
                            User.KEY_LAST_NAME to tvLastNameValue.text.toString()
                        ),
                        onStart = {
                            userVM.setLoadingState(UserViewModel.LoadingState.Start)
                            screenOrientation.lock()
                        },
                        onSuccess = {payout: Int, remainder: Double ->
                            userVM.setState(UserViewModel.State.PaymentRequestSent)
                        },
                        onNotEnough = {
                            showSnackBar(getString(R.string.text_not_money))
                        },
                        onComplete = {exception ->
                            if (exception != null) {
                                if (BuildConfig.DEBUG) exception.printStackTrace()
                                showSnackBar(exception.message?: getString(R.string.text_unknown_error_message))
                            }
                            userVM.setLoadingState(UserViewModel.LoadingState.Complete)
                            screenOrientation.unLock()
                        }
                    )
                }
            }

            btnLogOut.setOnClickListener {
                showLogoutDialog()
            }

            toolBar.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.menu_edit -> {
                        accountVM.setState(AccountViewModel.State.Editing)
                    }
                    R.id.menu_delete -> {
                        showAccountDeletingDialog()
                    }
                }
                true
            }
            toolBar.setNavigationOnClickListener {
                goBack()
            }
        }
    }

    private fun handleUserData(user: User) {

        with(binding) {

            val rewardToDisplay = "${(user.userReward).to2DigitsScale()} ${user.currencySymbol}"
            tvRewardValue.text = rewardToDisplay

            if (user.reservedPayment > 0) {
                tvReservedTitle.visibility = View.VISIBLE
                tvReservedValue.visibility = View.VISIBLE
                val payoutToDisplay = "${user.reservedPayment} ${user.currencySymbol}"
                tvReservedValue.text = payoutToDisplay
            }
            else {
                tvReservedTitle.visibility = View.GONE
                tvReservedValue.visibility = View.GONE
            }

            val rewardThreshold = (accountVM.paymentThreshold * user.currencyRate).toInt()
            val textCondition = "${getString(R.string.text_reward_conditions)} $rewardThreshold ${user.currencySymbol}"
            tvRewardCondition.text = textCondition
            if (user.userReward <= 0.0) {
                tvRewardCondition.visibility = View.GONE
            }
            else tvRewardCondition.visibility = View.VISIBLE
            btnGetReward.isEnabled = user.userReward > rewardThreshold

            if (user.message.isNotEmpty()) {
                tvMessage.apply {
                    visibility = View.VISIBLE
                    text = user.message
                }
            }
            else {
                tvMessage.apply {
                    visibility = View.GONE
                }
            }

            tvEmailValue.setText(user.email)
            tvPhoneValue.setText(user.phone)
            tvBankNameValue.setText(user.bankName)
            tvCardNumber.setText(user.bankCard)
            tvFirstNameValue.setText(user.firstName)
            tvLastNameValue.setText(user.lastName)
        }
    }

    private fun showConfirmDialog() {
        ConfirmDialog.newInstance(onLaunch = {dialog, binding ->
            with(binding) {
                dialog.isCancelable = false
                val message = "${getString(R.string.text_payment_request_sent_1)} ${accountVM.paymentDays} ${getString(R.string.text_payment_request_sent_2)}"
                tvMessage.text = message
                btnCancel.visibility = View.GONE
                btnOk.setOnClickListener {
                    accountVM.setState(AccountViewModel.State.ReadOnly)
                    dialog.dismiss()
                }
            }
        }).show(parentFragmentManager, ConfirmDialog.TAG)
    }

    private fun showInfoDialog() {
        ConfirmDialog.newInstance(onLaunch = {dialog, binding ->
            with(binding) {
                dialog.isCancelable = true
                ivIcon.visibility = View.INVISIBLE
                tvEmoji2.visibility = View.GONE
                tvEmoji.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.coins_bag)
                }
                val message = accountVM.explainMessage
                tvMessage.text = message
                btnCancel.visibility = View.GONE
                btnOk.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }).show(parentFragmentManager, ConfirmDialog.TAG)
    }

    private fun showLogoutDialog() {
        ConfirmDialog.newInstance(onLaunch = {dialog, binding ->
            with(binding) {
                dialog.isCancelable = true
                ivIcon.visibility = View.VISIBLE
                tvEmoji.visibility = View.GONE
                tvEmoji2.visibility = View.GONE
                val message = getString(R.string.text_signout_message)
                tvMessage.text = message
                btnCancel.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        dialog.dismiss()
                    }
                }
                btnOk.setOnClickListener {
                    val currentUser = ParseUser.getCurrentUser()
                    if (currentUser is ParseUser) {
                        ParseUser.logOut()
                    }
                    requireContext().cacheDir.deleteRecursively()
                    requireContext().clearEmailPasswordInPref()
                    authVM.setState(UserState.SignOut)
                    parentFragmentManager.beginTransaction().detach(this@AccountFragment).commit()
                    dialog.dismiss()
                }
            }
        }).show(parentFragmentManager, ConfirmDialog.TAG)
    }

    override fun onPause() {

        with(binding) {
            accountVM.saveScreenState(
                AccountScreenModel.Current(
                    reward = tvRewardValue.text.toString(),
                    groupRewardVisibility = groupReward.visibility,
                    messageForUser = tvMessage.text.toString(),
                    messageForUserVisibility = tvMessage.visibility,
                    email = tvEmailValue.text.toString(),
                    emailBackground = tvEmailValue.background,
                    phone = tvPhoneValue.text.toString(),
                    phoneBackground = tvPhoneValue.background,
                    bankName = tvBankNameValue.text.toString(),
                    bankNameBackground = tvBankNameValue.background,
                    cardNumber = tvCardNumber.text.toString(),
                    cardNumberBackground = tvCardNumber.background,
                    firstName = tvFirstNameValue.text.toString(),
                    firstNameBackground = tvFirstNameValue.background,
                    lastName = tvLastNameValue.text.toString(),
                    lastNameBackground = tvLastNameValue.background,
                    btnSaveVisibility = btnSave.visibility,
                    btnRewardEnable = btnGetReward.isEnabled,
                    rewardCondition = tvRewardCondition.text.toString(),
                    emailState = ViewState(
                        text = tvEmailValue.text.toString(),
                        isEnabled = tvEmailValue.isEnabled,
                        visibility = tvEmailValue.visibility,
                        isFocused = tvEmailValue.isFocused,
                        background = tvEmailValue.background
                    )
                )
            )
        }
        super.onPause()
    }
    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBack()
            }
        })
    }

    private fun showAccountDeletingDialog() {
        ConfirmDialog.newInstance(onLaunch = {dialog, binding ->
            with(binding) {
                dialog.isCancelable = true
                ivIcon.visibility = View.VISIBLE
                tvEmoji.visibility = View.GONE
                tvEmoji2.visibility = View.GONE
                val message = getString(R.string.text_warning_account_delete)
                tvMessage.text = message
                btnCancel.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        dialog.dismiss()
                    }
                }
                btnOk.apply {
                    text = getString(R.string.btn_text_delete)
                    setOnClickListener {
                        authVM.deleteAccount(
                            onStart = {
                                screenOrientation.lock()
                                authVM.setLoadingState(AuthViewModel.LoadingState.Start)
                            },
                            onSuccess = {
                                requireContext().cacheDir.deleteRecursively()
                                requireContext().clearEmailPasswordInPref()
                                authVM.setState(UserState.AccountDeleted)
                                parentFragmentManager.beginTransaction().detach(this@AccountFragment).commit()
                            },
                            onComplete = { exception ->
                                exception?.let {
                                    it.printStackTrace()
                                    showSnackBar(it.message?: getString(R.string.text_unknown_error_message))
                                }
                                screenOrientation.unLock()
                                authVM.setLoadingState(AuthViewModel.LoadingState.Complete)
                                dialog.dismiss()
                            }
                        )
                    }
                }
            }
        }).show(parentFragmentManager, ConfirmDialog.TAG)
    }

    private fun goBack() {
        val user = userVM.user.value
        user?.let {
            listener?.refreshAuthState(it)
        }
        parentFragmentManager.beginTransaction().detach(this@AccountFragment).commit()
    }

    override fun onDetach() {

        listener = null
        super.onDetach()
    }


}