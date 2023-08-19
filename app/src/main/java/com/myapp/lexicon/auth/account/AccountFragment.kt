@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.auth.account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentAccountBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.setBackground
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.helpers.toStringDate
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.settings.getExchangeRateFromPref
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    companion object {

        private var userId: String? = null
        fun newInstance(userId: String): AccountFragment {

            this.userId = userId
            return AccountFragment()
        }
    }

    private lateinit var binding: FragmentAccountBinding

    private val accountVM: AccountViewModel by lazy {
        val yooApiKey = getString(R.string.YOO_API_KEY)
        val factory = AccountViewModel.Factory(apiKey = yooApiKey)
        ViewModelProvider(this, factory)[AccountViewModel::class.java]
    }
    private val userVM by viewModels<UserViewModel>()

    private val paymentThreshold: Double = Firebase.remoteConfig.getDouble("payment_threshold")

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

            accountVM.loadingState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AccountViewModel.LoadingState.Complete -> progressBar.visibility = View.GONE
                    AccountViewModel.LoadingState.Start -> progressBar.visibility = View.VISIBLE
                }
            }

            tvIoMoneyRef.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://yoomoney.ru/")
                startActivity(intent)
            }

            userVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    UserViewModel.State.Init -> {}
                    is UserViewModel.State.PersonalDataUpdated -> {
                        val user = state.user
                        if (user.paymentRequired) {
                            accountVM.sendPayoutRequest(user)
                        }
                        else {
                            handleUserData(state.user)
                            showSnackBar(getString(R.string.data_is_saved))
                        }
                    }
                    is UserViewModel.State.PaymentRequestSent -> {

                    }
                    is UserViewModel.State.Error -> {
                        showSnackBar(state.message)
                    }
                    is UserViewModel.State.ReceivedUserData -> {
                        handleUserData(state.user)
                    }
                    is UserViewModel.State.UserAdded -> {

                    }
                    is UserViewModel.State.RevenueUpdated -> {}
                }
            }

            accountVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    AccountViewModel.State.Editing -> {
                        tvPhoneValue.isEnabled = true
                        tvPhoneValue.requestFocus()
                        tvCardNumber.isEnabled = true
                        tvFirstNameValue.isEnabled = true
                        tvLastNameValue.isEnabled = true
                        btnSave.visibility = View.VISIBLE
                    }
                    AccountViewModel.State.ReadOnly -> {
                        tvPhoneValue.isEnabled = false
                        tvCardNumber.isEnabled = false
                        tvFirstNameValue.isEnabled = false
                        tvLastNameValue.isEnabled = false
                        btnSave.visibility = View.GONE
                    }
                    is AccountViewModel.State.OnSave -> {
                        val user = state.user
                        user.apply {
                            phone = tvPhoneValue.text.toString()
                            bankCard = tvCardNumber.text.toString()
                            firstName = tvFirstNameValue.text.toString()
                            lastName = tvLastNameValue.text.toString()
                        }
                        userVM.updatePersonalData(user)
                        accountVM.setState(AccountViewModel.State.ReadOnly)
                    }
                    is AccountViewModel.State.OnValid -> {
                        if (state.phone) {
                            tvPhoneValue.setBackground(R.drawable.bg_horizontal_oval)
                        }
                        else {
                            tvPhoneValue.setBackground(R.drawable.bg_horizontal_oval_error)
                            tvPhoneValue.requestFocus()
                        }

                        if (state.card) {
                            tvCardNumber.setBackground(R.drawable.bg_horizontal_oval)
                        }
                        else {
                            tvCardNumber.setBackground(R.drawable.bg_horizontal_oval_error)
                            tvCardNumber.requestFocus()
                        }

                        if (state.firstName) {
                            tvFirstNameValue.setBackground(R.drawable.bg_horizontal_oval)
                        }
                        else {
                            tvFirstNameValue.setBackground(R.drawable.bg_horizontal_oval_error)
                            tvFirstNameValue.requestFocus()
                        }

                        if (state.lastName) {
                            tvLastNameValue.setBackground(R.drawable.bg_horizontal_oval)
                        }
                        else {
                            tvLastNameValue.setBackground(R.drawable.bg_horizontal_oval_error)
                            tvLastNameValue.requestFocus()
                        }
                    }
                }
            }

            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    accountVM.payoutState.collect { state ->
                        when(state) {
                            AccountViewModel.PayoutState.Init -> {}
                            is AccountViewModel.PayoutState.Failure -> {
                                val originalUser = accountVM.originalUser
                                if (originalUser is User) {
                                    userVM.updatePersonalData(originalUser)
                                }
                                else throw NullPointerException("******* originalUser is null **********")
                            }
                            is AccountViewModel.PayoutState.Success -> {
                                val paymentObj = state.data
                                if (paymentObj.status == "succeeded" || paymentObj.status == "pending") {
                                    val message = "${getString(R.string.text_payout_succeeded)} ${paymentObj.amount.value} ${paymentObj.amount.currency}"
                                    showSnackBar(message)
                                    val user = userVM.user.value
                                    if (user != null) {
                                        user.apply {
                                            paymentRequired = false
                                            paymentDate = ""
                                            reservedPayment = 0
                                        }
                                        userVM.updatePersonalData(user)
                                    }
                                }
                                if (paymentObj.status == "canceled") {
                                    val message = getString(R.string.text_payout_error)
                                    showSnackBar(message)
                                    val originalUser = accountVM.originalUser
                                    if (originalUser is User) {
                                        userVM.updatePersonalData(originalUser)
                                    }
                                    else throw NullPointerException("******* originalUser is null **********")
                                }
                            }
                            AccountViewModel.PayoutState.Timeout -> {
                                val message = getString(R.string.text_internet_unavailable)
                                showSnackBar(message)
                                val originalUser = accountVM.originalUser
                                originalUser?.let { handleUserData(it) }
                            }
                        }
                    }
                }
            }

            tvPhoneValue.doOnTextChanged { text, start, before, count ->
                if (!text.isNullOrEmpty() && text.length > 10) {
                    val state = accountVM.state.value
                    if (state is AccountViewModel.State.OnValid) {
                        val newState = state.copy(phone = true)
                        accountVM.setState(newState)
                    }
                }
            }
            tvCardNumber.doOnTextChanged { text, start, before, count ->
                val state = accountVM.state.value
                if (state is AccountViewModel.State.OnValid) {
                    if (!text.isNullOrEmpty() && text.length >= 14) {
                        accountVM.setState(state.copy(card = true))
                    }
                    else accountVM.setState(state.copy(card = false))
                }
            }
            tvFirstNameValue.doOnTextChanged { text, start, before, count ->
                val state = accountVM.state.value
                if (state is AccountViewModel.State.OnValid) {
                    if (!text.isNullOrEmpty()) {
                        accountVM.setState(state.copy(firstName = true))
                    }
                    else accountVM.setState(state.copy(firstName = false))
                }
            }
            tvLastNameValue.doOnTextChanged { text, start, before, count ->
                val state = accountVM.state.value
                if (state is AccountViewModel.State.OnValid) {
                    if (!text.isNullOrEmpty()) {
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
                    if (number.length < 14) {
                        accountVM.setState(AccountViewModel.State.OnValid(card = false))
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

                    user.apply {
                        phone = tvPhoneValue.text.toString()
                        bankCard = tvCardNumber.text.toString()
                        firstName = tvFirstNameValue.text.toString()
                        lastName = tvLastNameValue.text.toString()
                        paymentRequired = true
                        paymentDate = System.currentTimeMillis().toStringDate()
                    }
                    requireContext().getExchangeRateFromPref(
                        onInit = {},
                        onSuccess = {date, symbol, rate ->
                            accountVM.saveOriginalUser(user)
                            user.reservePayment(
                                threshold = paymentThreshold,
                                currencyRate = rate,
                                onReserve = { u ->
                                    showConfirmDialog(u)
                                },
                                onNotEnough = {
                                    showSnackBar(getString(R.string.text_not_money))
                                }
                            )
                        }
                    )

                }
            }
        }
    }

    private fun handleUserData(user: User) {

        with(binding) {

            requireContext().getExchangeRateFromPref(
                onInit = {},
                onSuccess = { date, symbol, rate ->
                    var rewardToDisplay = "${user.defaultCurrencyReward} $symbol"
                    tvRewardValue.text = rewardToDisplay

                    if (user.reservedPayment > 0) {
                        tvReservedTitle.visibility = View.VISIBLE
                        tvReservedValue.visibility = View.VISIBLE
                        val paymentToDisplay = "${user.reservedPayment} $symbol"
                        tvReservedValue.text = paymentToDisplay

                        rewardToDisplay = "${user.defaultCurrencyReward} $symbol"
                        tvRewardValue.text = rewardToDisplay
                    }
                    else {
                        tvReservedTitle.visibility = View.GONE
                        tvReservedValue.visibility = View.GONE
                    }

                    val rewardThreshold = (paymentThreshold * rate).toInt()
                    val textCondition = "${getString(R.string.text_reward_conditions)} $rewardThreshold $symbol"
                    tvRewardCondition.text = textCondition
                    btnGetReward.isEnabled = user.defaultCurrencyReward > rewardThreshold
                    if (user.userReward > rewardThreshold) {
                        tvRewardCondition.visibility = View.GONE
                    }
                    else tvRewardCondition.visibility = View.VISIBLE
                },
                onFailure = {
                    if (BuildConfig.DEBUG) it.printStackTrace()
                }
            )

            tvEmailValue.text = user.email
            tvPhoneValue.setText(user.phone)
            tvCardNumber.setText(user.bankCard)
            tvFirstNameValue.setText(user.firstName)
            tvLastNameValue.setText(user.lastName)


        }
    }

    private fun showConfirmDialog(user: User) {
        ConfirmDialog.newInstance(onLaunch = {dialog, binding ->
            with(binding) {
                dialog.isCancelable = false
                val message = "${getString(R.string.text_payment_request_sent_1)} ${user.reservedPayment} ${user.currencySymbol}"
                tvMessage.text = message
                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }
                btnOk.setOnClickListener {
                    accountVM.setState(AccountViewModel.State.ReadOnly)
                    userVM.updatePersonalData(user)
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
                }
                true
            }
            toolBar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
    }


}