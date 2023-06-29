package com.myapp.lexicon.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.FragmentAccountBinding
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User

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

            userVM.loadingState.observe(viewLifecycleOwner) { state ->
                when(state) {
                    UserViewModel.LoadingState.Complete -> {
                        progressBar.visibility = View.GONE
                    }
                    UserViewModel.LoadingState.Start -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }

            userVM.user.observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    handleUserData(user)
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
                        userVM.updateUser(0.0, user)
                        accountVM.setState(AccountViewModel.State.ReadOnly)
                    }
                }
            }

            btnSave.setOnClickListener {
                val user = userVM.user.value
                if (user != null) {
                    accountVM.setState(AccountViewModel.State.OnSave(user))
                }
            }
        }
    }

    private fun handleUserData(user: User) {

        with(binding) {

            val reward = user.rewardToDisplay
            tvRewardValue.text = reward

            tvEmailValue.text = user.email
            tvPhoneValue.setText(user.phone)
            tvFirstNameValue.setText(user.firstName)
            tvLastNameValue.setText(user.lastName)

            btnGetReward.isEnabled = user.userReward > 1000.0
            if (user.userReward > 1000.0) {
                tvRewardCondition.visibility = View.GONE
            }
            else tvRewardCondition.visibility = View.VISIBLE
        }
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