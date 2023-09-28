@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.auth.AuthViewModel
import com.myapp.lexicon.databinding.FragmentAccountBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.LuhnAlgorithm
import com.myapp.lexicon.helpers.setBackground
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.helpers.toStringDate
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.UserState
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.settings.clearEmailPasswordInPref
import com.myapp.lexicon.settings.getAuthDataFromPref
import com.myapp.lexicon.settings.saveUserToPref
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class AccountFragment : Fragment() {

    companion object {

        private var userId: String? = null
        private var password: String? = null
        fun newInstance(userId: String, password: String): AccountFragment {

            this.userId = userId
            this.password = password
            return AccountFragment()
        }
    }

    private lateinit var binding: FragmentAccountBinding
    private val accountVM: AccountViewModel by viewModels()
    private val authVM: AuthViewModel by activityViewModels()
    private val userVM by viewModels<UserViewModel>()

    private val paymentThreshold: Double = Firebase.remoteConfig.getDouble("payment_threshold")
    private val paymentDays: Int = Firebase.remoteConfig.getDouble("payment_days").toInt()
    private val explainMessage: String by lazy {
        Firebase.remoteConfig.getString("reward_explain_message")
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

            userVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    UserViewModel.State.Init -> {}
                    is UserViewModel.State.PersonalDataUpdated -> {
                        handleUserData(state.user)
                        showSnackBar(getString(R.string.data_is_saved))
                    }
                    is UserViewModel.State.PaymentRequestSent -> {
                        showConfirmDialog()
                    }
                    is UserViewModel.State.Error -> {
                        showSnackBar(state.message)
                    }
                    is UserViewModel.State.ReceivedUserData -> {
                        requireContext().getAuthDataFromPref(
                            onNotRegistered = {
                                showInfoDialog()
                                requireContext().saveUserToPref(state.user.apply {
                                    password = AccountFragment.password?: ""
                                })
                            }
                        )
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
                        accountVM.setState(AccountViewModel.State.OnValid())
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
                        val userMap = mapOf<String, Any>(
                            User.KEY_PHONE to tvPhoneValue.text.toString(),
                            User.KEY_BANK_CARD to tvCardNumber.text.toString(),
                            User.KEY_FIRST_NAME to tvFirstNameValue.text.toString(),
                            User.KEY_LAST_NAME to tvLastNameValue.text.toString()
                        )
                        userVM.addUserToStorage(user.id, userMap, isNew = false)
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
                    if (isPhoneNumber) {
                        val newState = state.copy(phone = true)
                        accountVM.setState(newState)
                    }
                    else accountVM.setState(state.copy(phone = false))
                }

            }
            tvCardNumber.doOnTextChanged { text, start, before, count ->
                val state = accountVM.state.value
                if (state is AccountViewModel.State.OnValid) {
                    val number = tvCardNumber.text.toString()
                    val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                    if (isValidNumber) {
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
                    val isValidNumber = LuhnAlgorithm.isLuhnChecksumValid(number)
                    if (!isValidNumber) {
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
                        paymentDate = System.currentTimeMillis().toStringDate()
                    }
                    user.reservePayment(
                        threshold = (paymentThreshold * user.currencyRate).roundToInt(),
                        onReserve = { map ->
                            userVM.addUserToStorage(user.id, map, isNew = false)
                            showConfirmDialog()
                        },
                        onNotEnough = {
                            showSnackBar(getString(R.string.text_not_money))
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

            val rewardThreshold = (paymentThreshold * user.currencyRate).toInt()
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
            tvCardNumber.setText(user.bankCard)
            tvFirstNameValue.setText(user.firstName)
            tvLastNameValue.setText(user.lastName)
        }
    }

    private fun showConfirmDialog() {
        ConfirmDialog.newInstance(onLaunch = {dialog, binding ->
            with(binding) {
                dialog.isCancelable = false
                val message = "${getString(R.string.text_payment_request_sent_1)} $paymentDays ${getString(R.string.text_payment_request_sent_2)}"
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
                val message = explainMessage
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
                    val auth = FirebaseAuth.getInstance()
                    auth.signOut()
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
                }
                true
            }
            toolBar.setNavigationOnClickListener {
                (requireActivity() as MainActivity).buildRewardText(userVM.user.value)
                parentFragmentManager.beginTransaction().detach(this@AccountFragment).commit()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (requireActivity() as MainActivity).buildRewardText(userVM.user.value)
                parentFragmentManager.beginTransaction().detach(this@AccountFragment).commit()
            }
        })
    }


}