@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
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
import com.myapp.lexicon.auth.invoice.InstallTaxAppFragment
import com.myapp.lexicon.auth.invoice.PayoutGuideFragment
import com.myapp.lexicon.common.PAYMENTS_CONDITIONS
import com.myapp.lexicon.common.PAYMENT_CHECK_PATTERN
import com.myapp.lexicon.common.SELF_EMPLOYED_PACKAGE
import com.myapp.lexicon.common.SELF_EMPLOYED_THRESHOLD
import com.myapp.lexicon.common.getMonthNameFromMillis
import com.myapp.lexicon.common.getPreviousMonthNameFromMillis
import com.myapp.lexicon.databinding.FragmentAccountBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.LuhnAlgorithm
import com.myapp.lexicon.helpers.checkIfAllDigits
import com.myapp.lexicon.helpers.firstCap
import com.myapp.lexicon.helpers.isItEmail
import com.myapp.lexicon.helpers.isItPhone
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.helpers.showToastIfDebug
import com.myapp.lexicon.helpers.timeInMillisMoscowTimeZone
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.Payout
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.models.ViewState
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.settings.clearEmailPasswordInPref
import com.myapp.lexicon.settings.isAppInstalled
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
                        includeToday.tvValue.text = state.today.text
                        includeYesterday.tvValue.text = state.yesterday.text
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
                        layoutCheckRef.visibility = state.checkRef.visibility
                        tvCheckRefValue.apply {
                            setText(state.checkRef.text)
                            background = state.checkRef.background
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
                    UserViewModel.State.Init -> {
                        setReadOnlyState(true)
                    }
                    is UserViewModel.State.PersonalDataUpdated -> {
                        showSnackBar(getString(R.string.data_is_saved))
                        if (currentUser != null) {
                            userVM.getUserFromCloud()
                        }
                    }
                    is UserViewModel.State.PaymentRequestSent -> {
                        showConfirmDialog()
                        if (currentUser != null) {
                            userVM.getUserFromCloud().observe(viewLifecycleOwner) { result ->
                                result.onSuccess { value: User ->
                                    handleUserData(value)
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
                    }
                    else -> {}
                }
            }

            tvEmailValue.doOnTextChanged { text, start, before, count ->
                val isValid = authVM.isValidEmail(text.toString())
                if (text.isNullOrEmpty() || !isValid) {
                    setNotValidFieldState(tvEmailValue)
                }
                else {
                    setValidFieldState(tvEmailValue)
                }
            }
            tvPhoneValue.doOnTextChanged { text, start, before, count ->
                val digits = text?.filter {
                    it.isDigit()
                }
                val isPhoneNumber = Regex("^[+]?[0-9]{10,13}$").matches(digits?: "")
                if (isPhoneNumber) {
                    setValidFieldState(tvPhoneValue)
                }
                else setNotValidFieldState(tvPhoneValue)
            }
            tvBankNameValue.doOnTextChanged { text, start, before, count ->
                if (text.toString().isEmpty()) setNotValidFieldState(tvBankNameValue)
                accountVM.bankList.value?.onSuccess { list: List<String> ->
                    if (list.contains(text.toString())) {
                        setValidFieldState(tvBankNameValue)
                    }
                    else {
                        setNotValidFieldState(tvBankNameValue)
                    }
                }
            }
            tvCardNumber.doOnTextChanged { text, start, before, count ->
                val number = tvCardNumber.text.toString()
                val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                if (isValidNumber || text.toString().isEmpty()) {
                    setValidFieldState(tvCardNumber)
                }
                else setNotValidFieldState(tvCardNumber)
            }
            tvFirstNameValue.doOnTextChanged { text, start, before, count ->
                if (text.toString().length > 1) {
                    setValidFieldState(tvFirstNameValue)
                }
                else {
                    setNotValidFieldState(tvFirstNameValue)
                }
            }
            tvLastNameValue.doOnTextChanged { text, start, before, count ->
                if (text.toString().length > 1) {
                    setValidFieldState(tvLastNameValue)
                }
                else {
                    setNotValidFieldState(tvLastNameValue)
                }
            }
            tvCheckRefValue.doOnTextChanged { text, start, before, count ->
                val reservedPayment = userVM.user.value?.reservedPayment ?: 0.0
                if (reservedPayment > SELF_EMPLOYED_THRESHOLD) {
                    val isMatches = tvCheckRefValue.text?.matches(Regex(PAYMENT_CHECK_PATTERN))
                    if (isMatches == true) {
                        setValidFieldState(tvCheckRefValue)
                        tvMessage.apply {
                            setText("")
                            visibility = View.GONE
                        }
                        btnGetReward.isEnabled = true
                    }
                    else {
                        setInvoiceRequiredState()
                    }
                }
            }

            btnGetReward.setOnClickListener {
                val user = userVM.user.value
                if (user != null) {
                    val email = tvEmailValue.text.toString()
                    if (email.isEmpty() || !authVM.isValidEmail(email)) {
                        setNotValidFieldState(tvEmailValue)
                        return@setOnClickListener
                    }

                    val phone = tvPhoneValue.text.toString()
                    if (phone.isEmpty() || phone.length < 11 && !phone.startsWith("+79")) {
                        setReadOnlyState(false)
                        setNotValidFieldState(tvPhoneValue)
                        return@setOnClickListener
                    }

                    if (accountVM.isBankCardRequired) {
                        val number = tvCardNumber.text.toString()
                        if (number.isNotEmpty()) {
                            val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                            if (!isValidNumber) {
                                setReadOnlyState(false)
                                setNotValidFieldState(tvCardNumber)
                                return@setOnClickListener
                            }
                        }
                        else {
                            setReadOnlyState(false)
                            setNotValidFieldState(tvCardNumber)
                            return@setOnClickListener
                        }
                    }

                    val bankName = tvBankNameValue.text.toString()
                    if (bankName.isEmpty()) {
                        setReadOnlyState(false)
                        setNotValidFieldState(tvBankNameValue)
                        return@setOnClickListener
                    }

                    val firstName = tvFirstNameValue.text.toString()
                    if (firstName.isEmpty()) {
                        setReadOnlyState(false)
                        setNotValidFieldState(tvFirstNameValue)
                        return@setOnClickListener
                    }

                    val lastName = tvLastNameValue.text.toString()
                    if (lastName.isEmpty()) {
                        setReadOnlyState(false)
                        setNotValidFieldState(tvLastNameValue)
                        return@setOnClickListener
                    }

                    val reservedPayment = userVM.user.value?.reservedPayment ?: 0.0
                    if (reservedPayment > SELF_EMPLOYED_THRESHOLD) {
                        val isMatches = tvCheckRefValue.text?.matches(Regex(PAYMENT_CHECK_PATTERN))
                        if (isMatches == false) {
                            setReadOnlyState(false)
                            setInvoiceRequiredState()
                            return@setOnClickListener
                        }
                    }

                    val requisitesMap = mutableMapOf(
                        User.KEY_PHONE to tvPhoneValue.text.toString().trim(),
                        User.KEY_BANK_NAME to tvBankNameValue.text.toString().trim(),
                        User.KEY_BANK_CARD to tvCardNumber.text.toString().trim(),
                        User.KEY_FIRST_NAME to tvFirstNameValue.text.toString().trim().firstCap(),
                        User.KEY_LAST_NAME to tvLastNameValue.text.toString().trim().firstCap()
                    )

                    val payoutMap = Payout(
                        reservedSum = 0,
                        payoutSum = user.reservedPayment.toInt(),
                        payoutTime = System.currentTimeMillis(),
                        checkReference = tvCheckRefValue.text.toString()
                    ).toMap().toMutableMap()

                    payoutMap.putAll(requisitesMap)

                    accountVM.demandPayment(
                        threshold = (accountVM.paymentThreshold * user.currencyRate).toInt(),
                        reward = user.reservedPayment.toInt(),
                        userMap = payoutMap,
                        onStart = {
                            userVM.setLoadingState(UserViewModel.LoadingState.Start)
                            requireActivity().orientationLock()
                        },
                        onSuccess = {
                            userVM.setState(UserViewModel.State.PaymentRequestSent(user, 0, 0.0))
                        },
                        onNotEnough = {
                            showSnackBar(getString(R.string.text_not_money))
                        },
                        onInvalidToken = {s: String ->
                            showSnackBar(getString(R.string.text_session_has_expired))
                            val authFragment = AuthFragment.newInstance()
                            parentFragmentManager.beginTransaction().replace(R.id.frame_to_page_fragm, authFragment).commit()
                        },
                        onComplete = {exception: Exception? ->
                            userVM.setLoadingState(UserViewModel.LoadingState.Complete)
                            setReadOnlyState()
                            if (exception != null) {
                                if (BuildConfig.DEBUG) exception.printStackTrace()
                                showSnackBar(exception.message?: getString(R.string.text_unknown_error_message))
                            }
                            requireActivity().orientationUnLock()
                        }
                    )
                }
            }

            btnCreateCheck.setOnClickListener {
                userVM.user.value?.let { usr -> checkIfSelfEmployedAppInstalled(usr) }
            }

            btnLogOut.setOnClickListener {
                showLogoutDialog()
            }

            toolBar.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.menu_edit -> {
                        setReadOnlyState(flag = false)
                    }
                    R.id.menu_save -> {
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
                                return@setOnMenuItemClickListener true
                            }
                            val userMap = mapOf<String, Any>(
                                User.KEY_EMAIL to tvEmailValue.text.toString(),
                                User.KEY_PHONE to tvPhoneValue.text.toString(),
                                User.KEY_BANK_NAME to tvBankNameValue.text.toString(),
                                User.KEY_BANK_CARD to tvCardNumber.text.toString(),
                                User.KEY_FIRST_NAME to tvFirstNameValue.text.toString().firstCap(),
                                User.KEY_LAST_NAME to tvLastNameValue.text.toString().firstCap()
                            )
                            userVM.updateUserDataIntoCloud(userMap)
                            setReadOnlyState()
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

            val currentMonth = timeInMillisMoscowTimeZone.getMonthNameFromMillis()
            val rewardToDisplay = "${getString(R.string.text_reward_for)} $currentMonth: ${(user.userReward).to2DigitsScale()} ${user.currencySymbol}"
            tvRewardValue.text = rewardToDisplay

            if (user.reservedPayment > 0) {
                groupToPayout.visibility = View.VISIBLE
                val previousMonth = timeInMillisMoscowTimeZone.getPreviousMonthNameFromMillis()
                val payoutToDisplay = "${getString(R.string.text_to_payment)} $previousMonth: ${user.reservedPayment} ${user.currencySymbol}"
                tvReservedValue.text = payoutToDisplay
            }
            else if (user.requiresPayment > 0 && user.reservedPayment == 0.0) {
                groupToPayout.visibility = View.VISIBLE
                val payoutToDisplay = "${getString(R.string.text_prepare_to_payment)}: ${user.requiresPayment} ${user.currencySymbol}"
                tvReservedValue.text = payoutToDisplay
            }
            else {
                groupToPayout.visibility = View.GONE
            }

            with(includeYesterday) {
                val yesterdayReward = user.yesterdayUserReward.to2DigitsScale()
                if (yesterdayReward > 0.0) {
                    dailyRewardRoot.visibility = View.VISIBLE
                    tvTitle.text = getString(R.string.text_yesterday)
                    val valueToDisplay = "+$yesterdayReward ${user.currencySymbol}"
                    tvValue.text = valueToDisplay
                }
                else {
                    dailyRewardRoot.visibility = View.INVISIBLE
                }
            }

            with(includeToday) {
                val todayReward = user.userDailyReward.to2DigitsScale()
                if (todayReward > 0.0) {
                    dailyRewardRoot.visibility = View.VISIBLE
                    tvTitle.text = getString(R.string.text_today)
                    val valueToDisplay = "+$todayReward ${user.currencySymbol}"
                    tvValue.text = valueToDisplay
                }
                else {
                    dailyRewardRoot.visibility = View.INVISIBLE
                }
            }

            tvEmailValue.setText(user.email)
            if (!user.email.isItEmail) setNotValidFieldState(tvEmailValue)

            if (user.phone.isItPhone) {
                layoutPhone.visibility = View.VISIBLE
                tvPhoneValue.setText(user.phone)
            }

            if (user.bankName.isNotEmpty()) {
                layoutBankName.visibility = View.VISIBLE
                tvBankNameValue.setText(user.bankName)
            }

            if (accountVM.isBankCardRequired && user.bankCard.checkIfAllDigits()) {
                layoutBankCard.visibility = View.VISIBLE
                tvCardNumber.setText(user.bankCard)
            }

            if (user.firstName.isNotEmpty()) {
                layoutFirstName.visibility = View.VISIBLE
                tvFirstNameValue.setText(user.firstName)
            }

            if (user.lastName.isNotEmpty()) {
                layoutLastName.visibility = View.VISIBLE
                tvLastNameValue.setText(user.lastName)
            }

            val rewardThreshold = (accountVM.paymentThreshold * user.currencyRate).toInt()
            val textCondition = "$PAYMENTS_CONDITIONS $rewardThreshold ${user.currencySymbol}"
            tvRewardCondition.text = textCondition
            if (user.userReward <= 0.0 || PAYMENTS_CONDITIONS.isEmpty()) {
                tvRewardCondition.visibility = View.GONE
            }
            else tvRewardCondition.visibility = View.VISIBLE

            btnGetReward.isEnabled = user.reservedPayment > rewardThreshold && accountVM.paymentCode == BuildConfig.PAYMENT_CODE.trim()
            if (user.reservedPayment > SELF_EMPLOYED_THRESHOLD) {
                setInvoiceRequiredState()
            }
        }
    }

    private fun setNotValidFieldState(view: View) {
        view.apply {
            isEnabled = true
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
        }
    }

    private fun setValidFieldState(view: View) {
        view.apply {
            background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval)
        }
    }

    private fun setInvoiceRequiredState() {
        with(binding) {
            root.children.forEach { view: View ->
                if (view is EditText) {
                    view.isEnabled = true
                }
            }
            layoutPhone.visibility = View.VISIBLE
            layoutBankName.visibility = View.VISIBLE
            layoutFirstName.visibility = View.VISIBLE
            layoutLastName.visibility = View.VISIBLE
            layoutCheckRef.visibility = View.VISIBLE
            tvCheckRefValue.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_horizontal_oval_error)
            btnGetReward.isEnabled = false
            val messageToUser = getString(R.string.text_message_create_invoice)
            tvMessage.apply {
                text = messageToUser
                visibility = View.VISIBLE
            }
        }
    }

    private fun setReadOnlyState(flag: Boolean = true) {
        with(binding) {
            toolBar.menu.findItem(R.id.menu_edit)?.isVisible = flag
            toolBar.menu.findItem(R.id.menu_save)?.isVisible = !flag
            tvEmailValue.apply {
                isEnabled = !flag
            }
            layoutPhone.visibility = View.VISIBLE
            tvPhoneValue.isEnabled = !flag
            layoutBankName.visibility = View.VISIBLE
            tvBankNameValue.isEnabled = !flag
            if (accountVM.isBankCardRequired){
                layoutBankCard.visibility = View.VISIBLE
                tvCardNumber.isEnabled = !flag
            }
            layoutFirstName.visibility = View.VISIBLE
            tvFirstNameValue.isEnabled = !flag
            layoutLastName.visibility = View.VISIBLE
            tvLastNameValue.isEnabled = !flag
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
                    setReadOnlyState()
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
                    today = ViewState(
                        text = includeToday.tvValue.text.toString()
                    ),
                    yesterday = ViewState(
                        text = includeYesterday.tvValue.text.toString()
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
                        background = tvPhoneValue.background,
                        visibility = tvPhoneValue.visibility
                    ),
                    bankName = ViewState(
                        text = tvBankNameValue.text.toString(),
                        background = tvBankNameValue.background,
                        visibility = tvBankNameValue.visibility
                    ),
                    cardNumber = ViewState(
                        text = tvCardNumber.text.toString(),
                        background = tvCardNumber.background,
                        visibility = tvCardNumber.visibility
                    ),
                    firstName = ViewState(
                        text = tvFirstNameValue.text.toString(),
                        background = tvFirstNameValue.background,
                        visibility = tvFirstNameValue.visibility
                    ),
                    lastName = ViewState(
                        text = tvLastNameValue.text.toString(),
                        background = tvLastNameValue.background,
                        visibility = tvLastNameValue.visibility
                    ),
                    checkRef = ViewState(
                        text = tvCheckRefValue.text.toString(),
                        background = tvCheckRefValue.background,
                        visibility = layoutCheckRef.visibility
                    ),
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
                "${getString(R.string.text_amount)}: ${user.requiresPayment} ${user.currencySymbol}, " +
                "${getString(R.string.title_phone)}: ${user.phone}, " +
                "${getString(R.string.title_e_mail)}: ${user.email}, " +
                "${getString(R.string.text_bank_card)}: ${user.bankCard}, " +
                "${getString(R.string.text_bank_name)}: ${user.bankName}. " +
                "${getString(R.string.text_check_ref)}: ${user.checkReference}"
    }

    private fun checkIfSelfEmployedAppInstalled(user: User) {
        val isInstalled = requireContext().isAppInstalled(SELF_EMPLOYED_PACKAGE)
        if (isInstalled) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_to_page_fragm, PayoutGuideFragment.newInstance(user))
                .addToBackStack(null)
                .commit()
        } else {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_to_page_fragm, InstallTaxAppFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }


}