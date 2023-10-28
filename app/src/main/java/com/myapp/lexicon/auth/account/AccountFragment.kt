@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.auth.AuthListener
import com.myapp.lexicon.auth.AuthViewModel
import com.myapp.lexicon.databinding.FragmentAccountBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.helpers.LuhnAlgorithm
import com.myapp.lexicon.helpers.getViewModel
import com.myapp.lexicon.helpers.setBackground
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.settings.clearEmailPasswordInPref
import com.myapp.lexicon.settings.isFirstLogin
import com.parse.ParseException
import com.parse.ParseUser
import kotlinx.coroutines.launch


class AccountFragment : Fragment() {

    companion object {

        private var userId: String? = null

        private var listener: AuthListener? = null
        fun newInstance(userId: String, listener: AuthListener?): AccountFragment {

            this.userId = userId
            this.listener = listener
            return AccountFragment()
        }
    }

    private lateinit var binding: FragmentAccountBinding

    private val accountVM: AccountViewModel by lazy {
        this.getViewModel(AccountViewModel::class.java) as AccountViewModel
    }

    private val authVM: AuthViewModel by lazy {
        this.getViewModel(AuthViewModel::class.java) as AuthViewModel
    }

    private val userVM: UserViewModel by lazy {
        this.getViewModel(UserViewModel::class.java) as UserViewModel
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

        with(binding) {

            if (savedInstanceState == null && !userId.isNullOrEmpty()) {
                userVM.getUserFromCloud(userId!!)
            }

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

            accountVM.fetchBankList().observe(viewLifecycleOwner) { result ->
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
                            userVM.getUserFromCloud(currentUser.objectId)
                        }
                    }
                    is UserViewModel.State.PaymentRequestSent -> {
                        showConfirmDialog()
                        if (currentUser != null) {
                            userVM.getUserFromCloud(currentUser.objectId)
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
                    is UserViewModel.State.UserAdded -> {}
                    is UserViewModel.State.RevenueUpdated -> {}
                }
            }

            accountVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AccountViewModel.State.Editing -> {
                        tvPhoneValue.isEnabled = true
                        tvPhoneValue.requestFocus()
                        tvBankNameValue.isEnabled = true
                        tvCardNumber.isEnabled = true
                        tvFirstNameValue.isEnabled = true
                        tvLastNameValue.isEnabled = true
                        btnSave.visibility = View.VISIBLE
                        accountVM.setState(AccountViewModel.State.OnValid())
                    }
                    AccountViewModel.State.ReadOnly -> {
                        tvPhoneValue.isEnabled = false
                        tvBankNameValue.isEnabled = false
                        tvCardNumber.isEnabled = false
                        tvFirstNameValue.isEnabled = false
                        tvLastNameValue.isEnabled = false
                        btnSave.visibility = View.GONE
                    }
                    is AccountViewModel.State.OnSave -> {
                        val user = state.user
                        val userMap = mapOf<String, Any>(
                            User.KEY_PHONE to tvPhoneValue.text.toString(),
                            User.KEY_BANK_NAME to tvBankNameValue.text.toString(),
                            User.KEY_BANK_CARD to tvCardNumber.text.toString(),
                            User.KEY_FIRST_NAME to tvFirstNameValue.text.toString(),
                            User.KEY_LAST_NAME to tvLastNameValue.text.toString()
                        )
                        userVM.updateUserDataIntoCloud(userMap).observe(viewLifecycleOwner) { result ->
                            result.onSuccess {
                                userVM.setState(UserViewModel.State.PersonalDataUpdated(user))
                            }
                            result.onFailure { exception ->
                                if ((exception as ParseException).code == ParseException.OBJECT_NOT_FOUND) {
                                    showSnackBar(getString(R.string.text_user_not_found))
                                }
                                else showSnackBar(exception.message?: getString(R.string.text_unknown_error_message))
                            }
                        }
                        accountVM.setState(AccountViewModel.State.ReadOnly)
                    }
                    is AccountViewModel.State.OnValid -> {
                        if (state.phone) {
                            tvPhoneValue.setBackground(R.drawable.bg_horizontal_oval)
                        }
                        else {
                            tvPhoneValue.apply {
                                setBackground(R.drawable.bg_horizontal_oval_error)
                                isEnabled = true
                                requestFocus()
                            }
                        }

                        if (state.bankName) {
                            tvBankNameValue.setBackground(R.drawable.bg_horizontal_oval)
                        }
                        else {
                            tvBankNameValue.apply {
                                setBackground(R.drawable.bg_horizontal_oval_error)
                                isEnabled = true
                                requestFocus()
                            }
                        }

                        if (state.card) {
                            tvCardNumber.setBackground(R.drawable.bg_horizontal_oval)
                        }
                        else {
                            tvCardNumber.apply {
                                setBackground(R.drawable.bg_horizontal_oval_error)
                                isEnabled = true
                                requestFocus()
                            }
                        }

                        if (state.firstName) {
                            tvFirstNameValue.setBackground(R.drawable.bg_horizontal_oval)
                        }
                        else {
                            tvFirstNameValue.apply {
                                setBackground(R.drawable.bg_horizontal_oval_error)
                                isEnabled = true
                                requestFocus()
                            }
                        }

                        if (state.lastName) {
                            tvLastNameValue.setBackground(R.drawable.bg_horizontal_oval)
                        }
                        else {
                            tvLastNameValue.apply {
                                setBackground(R.drawable.bg_horizontal_oval_error)
                                isEnabled = true
                                requestFocus()
                            }
                        }
                    }
                }
            }

            tvPhoneValue.doOnTextChanged { text, start, before, count ->
                val state = accountVM.state.value
                if (state is AccountViewModel.State.OnValid) {
                    val digits = text?.filter {
                        it.isDigit()
                    }
                    val isPhoneNumber = Regex("^[+]?[0-9]{10,13}$").matches(digits?: "")
                    if (isPhoneNumber || digits.isNullOrEmpty()) {
                        val newState = state.copy(phone = true)
                        accountVM.setState(newState)
                    }
                    else accountVM.setState(state.copy(phone = false))
                }
            }
            tvBankNameValue.doOnTextChanged { text, start, before, count ->
                val state = accountVM.state.value
                if (state is AccountViewModel.State.OnValid) {
                    if (text.toString().isEmpty()) {
                        accountVM.setState(state.copy(bankName = true))
                    }
                    else if (!text.isNullOrEmpty() && text.toString().length > 1) {
                        accountVM.setState(state.copy(bankName = true))
                    }
                    else accountVM.setState(state.copy(bankName = false))
                }
            }
            tvCardNumber.doOnTextChanged { text, start, before, count ->
                val state = accountVM.state.value
                if (state is AccountViewModel.State.OnValid) {
                    val number = tvCardNumber.text.toString()
                    val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                    if (isValidNumber || number.isEmpty()) {
                        accountVM.setState(state.copy(card = true))
                    }
                    else accountVM.setState(state.copy(card = false))
                }
            }
            tvFirstNameValue.doOnTextChanged { text, start, before, count ->
                val state = accountVM.state.value
                if (state is AccountViewModel.State.OnValid) {
                    if (text.isNullOrEmpty()) {
                        accountVM.setState(state.copy(firstName = true))
                    }
                    else if (text.isNotEmpty() && text.toString().length > 1) {
                        accountVM.setState(state.copy(firstName = true))
                    }
                    else accountVM.setState(state.copy(firstName = false))
                }
            }
            tvLastNameValue.doOnTextChanged { text, start, before, count ->
                val state = accountVM.state.value
                if (state is AccountViewModel.State.OnValid) {
                    if (text.isNullOrEmpty()) {
                        accountVM.setState(state.copy(firstName = true))
                    }
                    else if (text.isNotEmpty() && text.toString().length > 1) {
                        accountVM.setState(state.copy(lastName = true))
                    }
                    else accountVM.setState(state.copy(lastName = false))
                }
            }

            btnSave.setOnClickListener {
                val user = userVM.user.value
                if (user != null) {
                    accountVM.setState(AccountViewModel.State.OnSave(user))
                }
            }

            btnGetReward.setOnClickListener {
                val user = userVM.user.value
                if (user != null) {
                    if (tvPhoneValue.text.isNullOrEmpty() || (tvPhoneValue.text?.length?: 0) < 11) {
                        accountVM.setState(AccountViewModel.State.OnValid(phone = false))
                        return@setOnClickListener
                    }

                    val number = tvCardNumber.text.toString()
                    val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                    if (!isValidNumber) {
                        accountVM.setState(AccountViewModel.State.OnValid(card = false))
                        return@setOnClickListener
                    }
                    if (tvBankNameValue.text.isNullOrEmpty()) {
                        accountVM.setState(AccountViewModel.State.OnValid(bankName = false))
                        return@setOnClickListener
                    }
                    if (tvFirstNameValue.text.isNullOrEmpty()) {
                        accountVM.setState(AccountViewModel.State.OnValid(firstName = false))
                        return@setOnClickListener
                    }
                    if (tvLastNameValue.text.isNullOrEmpty()) {
                        accountVM.setState(AccountViewModel.State.OnValid(lastName = false))
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

            tvEmailValue.text = user.email
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
    override fun onResume() {
        super.onResume()

        with(binding) {
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