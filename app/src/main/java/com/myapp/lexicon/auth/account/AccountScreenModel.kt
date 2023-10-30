package com.myapp.lexicon.auth.account

import android.graphics.drawable.Drawable
import com.myapp.lexicon.models.ViewState

sealed class AccountScreenModel {
    object Init: AccountScreenModel()
    data class Current(
        val reward: String,
        val groupRewardVisibility: Int,
        val messageForUser: String,
        val messageForUserVisibility: Int,
        val emailState: ViewState,
        val email: String,
        //val emailValid: Boolean,
        val emailBackground: Drawable,
        val phone: String,
        //val phoneValid: Boolean,
        val phoneBackground: Drawable,
        val bankName: String,
        //val bankNameValid: Boolean,
        val bankNameBackground: Drawable,
        val cardNumber: String,
        //val cardNumberValid: Boolean,
        val cardNumberBackground: Drawable,
        val firstName: String,
        //val firstNameValid: Boolean,
        val firstNameBackground: Drawable,
        val lastName: String,
        //val lastNameValid: Boolean,
        val lastNameBackground: Drawable,
        val btnSaveVisibility: Int,
        val btnRewardEnable: Boolean,
        val rewardCondition: String
    ): AccountScreenModel()
}
