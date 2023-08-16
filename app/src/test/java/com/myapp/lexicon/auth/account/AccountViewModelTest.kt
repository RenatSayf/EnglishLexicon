package com.myapp.lexicon.auth.account

import com.myapp.lexicon.models.User
import org.junit.Assert
import org.junit.Test


class AccountViewModelTest {

    @Test
    fun createPayClaimsBody_Success() {

        val user = User("SSSSSSSSSSSSSSS").apply {
            currency = "RUB"
            bankCard = "1111"
            reservedPayment = 100
            firstName = "Xxxx"
            lastName = "Yyyy"
        }
        val viewModel = AccountViewModel.Factory("XXXXX").create(AccountViewModel::class.java)

    }

    @Test
    fun createPayClaimsBody_WrongInputData() {

        val user = User("SSSSSSSSSSSSSSS")
        val viewModel = AccountViewModel.Factory("XXXXX").create(AccountViewModel::class.java)

    }

}