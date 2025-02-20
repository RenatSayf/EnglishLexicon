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
        val phoneState: ViewState,
        val bankName: ViewState,
        val cardNumber: ViewState,
        val firstName: ViewState,
        val lastName: ViewState,
        val checkRef: ViewState,
        val btnGetReward: ViewState,
        val rewardCondition: ViewState
    ): AccountScreenState()
}
