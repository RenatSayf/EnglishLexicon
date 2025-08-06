@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
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
import com.myapp.lexicon.ads.ext.showNativeAdsIfLoaded
import com.myapp.lexicon.auth.AuthFragment
import com.myapp.lexicon.auth.AuthViewModel
import com.myapp.lexicon.auth.agreement.UserAgreementDialog
import com.myapp.lexicon.common.PAYMENTS_CONDITIONS
import com.myapp.lexicon.common.getPreviousMonthNameFromMillis
import com.myapp.lexicon.databinding.FragmentAccountBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.LuhnAlgorithm
import com.myapp.lexicon.helpers.isItEmail
import com.myapp.lexicon.helpers.orientationLock
import com.myapp.lexicon.helpers.orientationUnLock
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showMultiLineSnackBar
import com.myapp.lexicon.helpers.showToastIfDebug
import com.myapp.lexicon.helpers.timeInMillisMoscowTimeZone
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.Payout
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

            userVM.setState(UserViewModel.State.Init)

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
                tvWalletAddress
            )

            accountVM.screenState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AccountScreenState.Init -> {
                        setReadOnlyState(true)
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
                        layoutWalletAddess.apply {
                            visibility = state.walletAddress.visibility
                        }
                        tvWalletAddress.apply {
                            setText(state.walletAddress.text)
                            background = state.walletAddress.background
                            if (state.walletAddress.isFocused) {
                                requestFocus()
                                setSelection(this.text?.length?: 0)
                            }
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

            val currentUser = ParseUser.getCurrentUser()
            userVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    UserViewModel.State.Init -> {
                        setReadOnlyState(true)
                    }
                    is UserViewModel.State.PersonalDataUpdated -> {
                        showMultiLineSnackBar(getString(R.string.data_is_saved))
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
                                            showMultiLineSnackBar(getString(R.string.text_request_sented))
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
                        showMultiLineSnackBar(state.message)
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
            tvWalletAddress.doOnTextChanged { text, start, before, count ->
                val number = tvWalletAddress.text.toString()
                val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                if (isValidNumber || text.toString().isEmpty()) {
                    setValidFieldState(tvWalletAddress)
                }
                else setNotValidFieldState(tvWalletAddress)
            }

            btnGetReward.setOnClickListener {
                val user = userVM.user.value
                if (user != null) {
                    val email = tvEmailValue.text.toString()
                    if (email.isEmpty() || !authVM.isValidEmail(email)) {
                        setNotValidFieldState(tvEmailValue)
                        return@setOnClickListener
                    }

                    if (accountVM.isBankCardRequired) {
                        val number = tvWalletAddress.text.toString()
                        if (number.isNotEmpty()) {
                            val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                            if (!isValidNumber) {
                                setReadOnlyState(false)
                                setNotValidFieldState(tvWalletAddress)
                                return@setOnClickListener
                            }
                        }
                        else {
                            setReadOnlyState(false)
                            setNotValidFieldState(tvWalletAddress)
                            return@setOnClickListener
                        }
                    }

                    val requisitesMap = mutableMapOf(
                        User.KEY_BANK_CARD to tvWalletAddress.text.toString().trim()
                    )

                    val payoutMap = Payout(
                        reservedSum = 0,
                        payoutSum = user.reservedPayment.toInt(),
                        payoutTime = System.currentTimeMillis(),
                        checkReference = ""
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
                            showMultiLineSnackBar(getString(R.string.text_not_money))
                        },
                        onInvalidToken = {s: String ->
                            showMultiLineSnackBar(getString(R.string.text_session_has_expired))
                            val authFragment = AuthFragment.newInstance()
                            parentFragmentManager.beginTransaction().replace(R.id.frame_to_page_fragm, authFragment).commit()
                        },
                        onComplete = {exception: Exception? ->
                            userVM.setLoadingState(UserViewModel.LoadingState.Complete)
                            setReadOnlyState()
                            if (exception != null) {
                                if (BuildConfig.DEBUG) exception.printStackTrace()
                                showMultiLineSnackBar(exception.message?: getString(R.string.text_unknown_error_message))
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
                                showMultiLineSnackBar(getString(R.string.text_form_incorrect))
                                return@setOnMenuItemClickListener true
                            }
                            val userMap = mapOf<String, Any>(
                                User.KEY_EMAIL to tvEmailValue.text.toString(),
                                User.KEY_BANK_CARD to tvWalletAddress.text.toString()
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

            requireActivity().showNativeAdsIfLoaded(listOf(adNative))

        }
    }

    private fun handleUserData(user: User) {

        with(binding) {

            val rewardToDisplay = "${getString(R.string.text_reward_for)}: ${user.userReward.toInt()} ${getString(R.string.emoji_coin)}"
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
                val yesterdayReward = user.yesterdayUserReward.toInt()
                if (yesterdayReward > 0.0) {
                    dailyRewardRoot.visibility = View.VISIBLE
                    tvTitle.text = getString(R.string.text_yesterday)
                    val valueToDisplay = "+$yesterdayReward ${getString(R.string.emoji_coin)}"
                    tvValue.text = valueToDisplay
                }
                else {
                    dailyRewardRoot.visibility = View.INVISIBLE
                }
            }

            with(includeToday) {
                val todayReward = user.userDailyReward.toInt()
                if (todayReward > 0.0) {
                    dailyRewardRoot.visibility = View.VISIBLE
                    tvTitle.text = getString(R.string.text_today)
                    val valueToDisplay = "+$todayReward ${getString(R.string.emoji_coin)}"
                    tvValue.text = valueToDisplay
                }
                else {
                    dailyRewardRoot.visibility = View.INVISIBLE
                }
            }

            tvEmailValue.setText(user.email)
            if (!user.email.isItEmail) setNotValidFieldState(tvEmailValue)

            val rewardThreshold = (accountVM.paymentThreshold * user.currencyRate).toInt()
            val textCondition = "$PAYMENTS_CONDITIONS $rewardThreshold ${user.currencySymbol}"
            tvRewardCondition.text = textCondition
            if (user.userReward <= 0.0 || PAYMENTS_CONDITIONS.isEmpty()) {
                tvRewardCondition.visibility = View.GONE
            }
            else tvRewardCondition.visibility = View.VISIBLE

            btnGetReward.isEnabled = user.reservedPayment > rewardThreshold && accountVM.paymentCode == BuildConfig.PAYMENT_CODE.trim()

            if (btnGetReward.isEnabled && accountVM.paymentCode == BuildConfig.PAYMENT_CODE.trim()) {
                layoutWalletAddess.visibility = View.VISIBLE
                tvWalletAddress.setText(user.walletAddress)
            }
            else {
                layoutWalletAddess.visibility = View.GONE
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

    private fun setReadOnlyState(flag: Boolean = true) {
        with(binding) {
            toolBar.menu.findItem(R.id.menu_edit)?.isVisible = flag
            toolBar.menu.findItem(R.id.menu_save)?.isVisible = !flag
            tvEmailValue.apply {
                isEnabled = !flag
            }
            if (accountVM.isBankCardRequired){
                layoutWalletAddess.visibility = View.VISIBLE
                tvWalletAddress.isEnabled = !flag
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
                    walletAddress = ViewState(
                        text = tvWalletAddress.text.toString(),
                        background = tvWalletAddress.background,
                        visibility = tvWalletAddress.visibility
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
                                    showMultiLineSnackBar(it.message?: getString(R.string.text_unknown_error_message))
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
                "${getString(R.string.text_bank_card)}: ${user.walletAddress}, " +
                "${getString(R.string.text_bank_name)}: ${user.bankName}. " +
                "${getString(R.string.text_check_ref)}: ${user.checkReference}"
    }


}