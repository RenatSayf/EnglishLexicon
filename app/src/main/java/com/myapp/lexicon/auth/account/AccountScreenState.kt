package com.myapp.lexicon.auth.account

import com.myapp.lexicon.models.ViewState

sealed class AccountScreenState {
    data object Init: AccountScreenState()
    data class Current(
        val reward: ViewState,
        val today: ViewState,
        val yesterday: ViewState,
        val groupPayout: ViewState,
        val messageForUser: ViewState,
        val emailState: ViewState,
        val walletAddress: ViewState,
        val btnGetReward: ViewState,
        val rewardCondition: ViewState
    ): AccountScreenState()
}
