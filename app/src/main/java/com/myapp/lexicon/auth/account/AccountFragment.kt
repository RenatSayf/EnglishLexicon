@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentAccountBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.LuhnAlgorithm
import com.myapp.lexicon.helpers.setBackground
import com.myapp.lexicon.helpers.showSnackBar
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.settings.getExchangeRateFromPref
import me.dkzwm.widget.fet.FormattedEditText
import java.math.BigDecimal
import java.math.RoundingMode

class AccountFragment : Fragment() {

    companion object {

        private var userId: String? = null
        fun newInstance(userId: String): AccountFragment {

            this.userId = userId
            return AccountFragment()
        }
    }

    private lateinit var binding: FragmentAccountBinding
    private val accountVM: AccountViewModel by viewModels()
    private val userVM: UserViewModel by viewModels()

    private val paymentThreshold: Double = Firebase.remoteConfig.getDouble("payment_threshold")
    private val paymentDays: Int = Firebase.remoteConfig.getDouble("payment_days").toInt()

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

            userVM.state.observe(viewLifecycleOwner) { state ->
                when(state) {
                    UserViewModel.State.Complete -> {
                        progressBar.visibility = View.GONE
                    }
                    UserViewModel.State.Start -> {
                        progressBar.visibility = View.VISIBLE
                    }
                    is UserViewModel.State.PersonalDataUpdated -> {
                        showSnackBar(getString(R.string.data_is_saved))
                    }
                    is UserViewModel.State.PaymentRequestSent -> {
                        showConfirmDialog()
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
                        tvFirstNameValue.isEnabled = true
                        tvLastNameValue.isEnabled = true
                        btnSave.visibility = View.VISIBLE
                    }
                    AccountViewModel.State.ReadOnly -> {
                        tvPhoneValue.isEnabled = false
                        tvFirstNameValue.isEnabled = false
                        tvLastNameValue.isEnabled = false
                        btnSave.visibility = View.GONE
                    }
                    is AccountViewModel.State.OnSave -> {
                        val user = state.user
                        user.apply {
                            phone = tvPhoneValue.text.toString()
                            firstName = tvFirstNameValue.text.toString()
                            lastName = tvLastNameValue.text.toString()
                        }
                        userVM.updatePersonalData(user)
                        accountVM.setState(AccountViewModel.State.ReadOnly)
                    }
                    is AccountViewModel.State.OnNotValid -> {
                        val user = state.user
                        var isValid = true
                        when {
                            user.phone.length < 11 -> {
                                tvPhoneValue.setBackground(R.drawable.bg_horizontal_oval_error)
                                isValid = false
                            }
                        }
                        if (!isValid) {
                            showSnackBar("Заполните все поля")
                        } else {
                        }
                    }
                }
            }

            tvPhoneValue.doOnTextChanged { text, start, before, count ->
                val formatStyle = tvPhoneValue.formatStyle
                formatStyle
            }

            btnSave.setOnClickListener {
                val user = userVM.user.value
                if (user != null) {
                    accountVM.setState(AccountViewModel.State.OnSave(user))
                }
            }

            btnGetReward.setOnClickListener {
                val user = userVM.user.value
                if (user != null && user.bankCard.isEmpty()) {
                    BankCardDialog.newInstance(onLaunch = { dialog, binding ->
                        with(binding) {
                            val explainText = "${getString(R.string.text_card_number_to_phone)} ${user.phone}"
                            tvBankHint.text = explainText
                            etBankCard.setText(user.bankCard)
                            btnCancel.setOnClickListener {
                                dialog.dismiss()
                            }
                            btnOk.setOnClickListener {
                                val number = etBankCard.text.toString()
                                val isValid = LuhnAlgorithm.isLuhnChecksumValid(number)
                                if (isValid) {
                                    userVM.updatePersonalData(user.apply {
                                        paymentRequired = true
                                        bankCard = number
                                    })
                                    showConfirmDialog()
                                    dialog.dismiss()
                                }
                                else {
                                    showSnackBar(getString(R.string.text_card_number_not_valid))
                                }
                            }
                        }
                    }).show(parentFragmentManager, BankCardDialog.TAG)
                }
            }
        }
    }

    private fun handleUserData(user: User) {

        with(binding) {

            requireContext().getExchangeRateFromPref(
                onInit = {},
                onSuccess = { date, symbol, rate ->
                    val reward = user.userReward * rate
                    val rewardToDisplay = "${BigDecimal(reward).setScale(2, RoundingMode.DOWN)} $symbol"
                    tvRewardValue.text = rewardToDisplay

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