@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.auth.AuthFragment
import com.myapp.lexicon.auth.AuthViewModel
import com.myapp.lexicon.auth.agreement.UserAgreementDialog
import com.myapp.lexicon.databinding.FragmentAccountBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.LuhnAlgorithm
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.helpers.showToastIfDebug
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

        private lateinit var authVMClass: Class<out ViewModel>
        private lateinit var accountVMClass: Class<out ViewModel>
        private lateinit var userVMClass: Class<out ViewModel>

        fun newInstance(
            authVMClass: Class<out ViewModel> = AuthViewModel::class.java,
            accountVMClass: Class<out ViewModel> = AccountViewModel::class.java,
            userVMClass: Class<out ViewModel> = UserViewModel::class.java
        ): AccountFragment {
            this.authVMClass = authVMClass
            this.accountVMClass = accountVMClass
            this.userVMClass = userVMClass
            return AccountFragment()
        }
    }

    private lateinit var binding: FragmentAccountBinding

    private val accountVM: AccountViewModel by lazy {
        ViewModelProvider(this)[accountVMClass] as AccountViewModel
    }

    private val authVM: AuthViewModel by lazy {
        ViewModelProvider(requireActivity())[authVMClass] as AuthViewModel
    }

    private val userVM: UserViewModel by lazy {
        ViewModelProvider(requireActivity())[userVMClass] as UserViewModel
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

            val editTextList = mutableListOf(
                tvEmailValue,
                tvPhoneValue,
                tvBankNameValue,
                tvFirstNameValue,
                tvLastNameValue
            ).apply {
                if (accountVM.isBankCardRequired) {
                    add(tvCardNumber)
                }
            }

            accountVM.screenState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AccountScreenState.Init -> {
                        userVM.getUserFromCloud()
                    }
                    is AccountScreenState.Current -> {
                        tvRewardValue.text = state.reward.text
                        groupToPayout.apply {
                            visibility = state.groupPayout.visibility
                            tvReservedValue.text = state.groupPayout.text
                        }
                        tvMessage.apply {
                            text = state.messageForUser.text
                            visibility = state.messageForUser.visibility
                        }
                        tvEmailValue.apply {
                            setText(state.emailState.text)
                            background = state.emailState.background
                            if (state.emailState.isFocused) {
                                requestFocus()
                                setSelection(this.text?.length?: 0)
                            }
                        }
                        tvPhoneValue.apply {
                            setText(state.phoneState.text)
                            background = state.phoneState.background
                            if (state.phoneState.isFocused) {
                                requestFocus()
                                setSelection(this.text?.length?: 0)
                            }
                        }
                        tvBankNameValue.apply {
                            setText(state.bankName.text)
                            background = state.bankName.background
                            if (state.bankName.isFocused) {
                                requestFocus()
                                setSelection(this.text?.length?: 0)
                            }
                        }
                        tvCardNumber.apply {
                            setText(state.cardNumber.text)
                            background = state.cardNumber.background
                            if (state.cardNumber.isFocused) {
                                requestFocus()
                                setSelection(this.text?.length?: 0)
                            }
                        }
                        tvFirstNameValue.apply {
                            setText(state.firstName.text)
                            background = state.firstName.background
                            if (state.firstName.isFocused) {
                                requestFocus()
                                setSelection(this.text?.length?: 0)
                            }
                        }
                        tvLastNameValue.apply {
                            setText(state.lastName.text)
                            background = state.lastName.background
                            if (state.lastName.isFocused) {
                                requestFocus()
                                setSelection(this.text?.length?: 0)
                            }
                        }
                        btnSave.apply {
                            visibility = state.btnSave.visibility
                        }
                        btnGetReward.apply {
                            isEnabled = state.btnGetReward.isEnabled
                        }
                        tvRewardCondition.apply {
                            text = state.rewardCondition.text
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
                            userVM.getUserFromCloud().observe(viewLifecycleOwner) { result ->
                                result.onSuccess { value: User ->
                                    accountVM.sendPaymentInfoToTGChannel(
                                        message = buildMessageAboutPayment(value),
                                        onStart = {
                                            requireActivity().orientationLock()
                                        },
                                        onSuccess = {
                                            showSnackBar(getString(R.string.text_request_sented))
                                        }
                                    ) { exception ->
                                        exception?.printStackTraceIfDebug()
                                        requireActivity().orientationUnLock()
                                    }
                                }
                                result.onFailure { exception ->
                                    exception.printStackTraceIfDebug()
                                    showToastIfDebug(exception.message)
                                }
                            }
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
                        userVM.setState(UserViewModel.State.Init)
                    }
                    else -> {}
                }
            }

            accountVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AccountViewModel.State.Editing -> {
                        editTextList.forEach {
                            it.isEnabled = true
                            (it.parent as LinearLayoutCompat).visibility = View.VISIBLE
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
                            if (it.text.isEmpty() && it.id != R.id.tvEmailValue) {
                                (it.parent as LinearLayoutCompat).visibility = View.GONE
                            }
                            else {
                                (it.parent as LinearLayoutCompat).visibility = View.VISIBLE
                            }
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
                    accountVM.setState(AccountViewModel.State.Editing)
                    val email = tvEmailValue.text.toString()
                    if (email.isEmpty() || !authVM.isValidEmail(email)) {
                        tvEmailValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
                        return@setOnClickListener
                    }

                    if (tvPhoneValue.text.isNullOrEmpty() || (tvPhoneValue.text?.length?: 0) < 11) {
                        tvPhoneValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
                        return@setOnClickListener
                    }

                    if (accountVM.isBankCardRequired) {
                        val number = tvCardNumber.text.toString()
                        if (number.isNotEmpty()) {
                            val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                            if (!isValidNumber) {
                                tvCardNumber.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
                                return@setOnClickListener
                            }
                        }
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
                            requireActivity().orientationLock()
                        },
                        onSuccess = {payout: Int, remainder: Double ->
                            userVM.setState(UserViewModel.State.PaymentRequestSent(user, payout, remainder))
                        },
                        onNotEnough = {
                            showSnackBar(getString(R.string.text_not_money))
                        },
                        onInvalidToken = {oldToken ->
                            showSnackBar(getString(R.string.text_session_has_expired))
                            val authFragment = AuthFragment.newInstance()
                            parentFragmentManager.beginTransaction().replace(R.id.frame_to_page_fragm, authFragment).commit()
                        },
                        onComplete = {exception ->
                            userVM.setLoadingState(UserViewModel.LoadingState.Complete)
                            accountVM.setState(AccountViewModel.State.ReadOnly)
                            if (exception != null) {
                                if (BuildConfig.DEBUG) exception.printStackTrace()
                                showSnackBar(exception.message?: getString(R.string.text_unknown_error_message))
                            }
                            requireActivity().orientationUnLock()
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
                        when (accountVM.state.value) {
                            is AccountViewModel.State.ReadOnly -> {
                                accountVM.setState(AccountViewModel.State.Editing)
                            }
                            is AccountViewModel.State.Editing -> {
                                accountVM.setState(AccountViewModel.State.ReadOnly)
                            }
                            else -> {}
                        }
                    }
                    R.id.menu_user_agreement -> {
                        val dialog = UserAgreementDialog.newInstance(
                            isCancelable = false,
                            onPositiveClick = {})
                        dialog.show(parentFragmentManager, UserAgreementDialog.TAG)
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
                groupToPayout.visibility = View.VISIBLE
                val payoutToDisplay = "${user.reservedPayment} ${user.currencySymbol}"
                tvReservedValue.text = payoutToDisplay
            }
            else {
                groupToPayout.visibility = View.GONE
            }

            val rewardThreshold = (accountVM.paymentThreshold * user.currencyRate).toInt()
            val textCondition = "${getString(R.string.text_reward_conditions)} $rewardThreshold ${user.currencySymbol}"
            tvRewardCondition.text = textCondition
            if (user.userReward <= 0.0) {
                tvRewardCondition.visibility = View.GONE
            }
            else tvRewardCondition.visibility = View.VISIBLE
            btnGetReward.isEnabled = user.userReward > rewardThreshold && accountVM.paymentCode == BuildConfig.PAYMENT_CODE

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

            if (user.phone.isNotEmpty()) {
                tvPhoneValue.setText(user.phone)
                layoutPhone.visibility = View.VISIBLE
            }
            else layoutPhone.visibility = View.GONE

            if (user.bankName.isNotEmpty()) {
                tvBankNameValue.setText(user.bankName)
                layoutBankName.visibility = View.VISIBLE
            } else {
                layoutBankName.visibility = View.GONE
            }

            if (accountVM.isBankCardRequired && user.bankCard.isNotEmpty()) {
                tvCardNumber.setText(user.bankCard)
                layoutBankCard.visibility = View.VISIBLE
            } else {
                layoutBankCard.visibility = View.GONE
            }
            if (user.firstName.isNotEmpty()) {
                tvFirstNameValue.setText(user.firstName)
                layoutFirstName.visibility = View.VISIBLE
            } else {
                layoutFirstName.visibility = View.GONE
            }
            if (user.lastName.isNotEmpty()) {
                tvLastNameValue.setText(user.lastName)
                layoutLastName.visibility = View.VISIBLE
            } else {
                layoutLastName.visibility = View.GONE
            }
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
                AccountScreenState.Current(
                    reward = ViewState(
                        text = tvRewardValue.text.toString(),
                    ),
                    groupPayout = ViewState(
                        text = tvReservedValue.text.toString(),
                        visibility = groupToPayout.visibility
                    ),
                    messageForUser = ViewState(
                        text = tvMessage.text.toString(),
                        visibility = tvMessage.visibility
                    ),
                    emailState = ViewState(
                        text = tvEmailValue.text.toString(),
                        isEnabled = tvEmailValue.isEnabled,
                        isFocused = tvEmailValue.isFocused,
                        background = tvEmailValue.background
                    ),
                    phoneState = ViewState(
                        text = tvPhoneValue.text.toString(),
                        isEnabled = tvPhoneValue.isEnabled,
                        background = tvPhoneValue.background
                    ),
                    bankName = ViewState(
                        text = tvBankNameValue.text.toString(),
                        background = tvBankNameValue.background
                    ),
                    cardNumber = ViewState(
                        text = tvCardNumber.text.toString(),
                        background = tvCardNumber.background
                    ),
                    firstName = ViewState(
                        text = tvFirstNameValue.text.toString(),
                        background = tvFirstNameValue.background
                    ),
                    lastName = ViewState(
                        text = tvLastNameValue.text.toString(),
                        background = tvLastNameValue.background
                    ),
                    btnSave = ViewState(visibility = btnSave.visibility),
                    btnGetReward = ViewState(isEnabled = btnGetReward.isEnabled),
                    rewardCondition = ViewState(text = tvRewardCondition.text.toString())
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
                                requireActivity().orientationLock()
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
                                requireActivity().orientationUnLock()
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
            authVM.setState(UserState.SignIn(it))
            userVM.setState(UserViewModel.State.ReceivedUserData(it))
        }
        parentFragmentManager.beginTransaction().detach(this@AccountFragment).commit()
    }

    private fun buildMessageAboutPayment(user: User): String {
        return "${getString(R.string.text_user)} ${user.firstName} ${user.lastName} ${getString(R.string.text_wishes_to_get_reward)}. " +
                "${getString(R.string.text_amount)}: ${user.reservedPayment} ${user.currencySymbol}, " +
                "${getString(R.string.title_phone)}: ${user.phone}, " +
                "${getString(R.string.title_e_mail)}: ${user.email}, " +
                "${getString(R.string.text_bank_card)}: ${user.bankCard}, " +
                "${getString(R.string.text_bank_name)}: ${user.bankName}."
    }


}