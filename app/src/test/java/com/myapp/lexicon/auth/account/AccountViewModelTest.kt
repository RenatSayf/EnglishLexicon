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
        val viewModel = AccountViewModel()
        viewModel.createPayClaimsBodyJson(
            user,
            onSuccess = { s: String ->
                val expectedResult = """{"amount":{"currency":"RUB","value":"100"},"description":"Выплата по заказу Xxxx Yyyy","metadata":{"order_id":"SSSSSSSSSSSSSSS"},"payout_destination_data":{"account_number":"1111","type":"yoo_money"}}"""
                Assert.assertEquals(expectedResult, s)
            },
            onWrongInputData = {exception ->
                Assert.assertTrue(exception.message, false)
            },
            onFailure = {exception ->
                Assert.assertTrue(exception.message, false)
            }
        )
    }

    @Test
    fun createPayClaimsBody_WrongInputData() {

        val user = User("SSSSSSSSSSSSSSS")
        val viewModel = AccountViewModel()
        viewModel.createPayClaimsBodyJson(
            user,
            onSuccess = { s: String ->
                Assert.assertTrue(false)
            },
            onWrongInputData = {exception ->
                Assert.assertTrue(exception.message, true)
            },
            onFailure = {exception ->
                Assert.assertTrue(exception.message, false)
            }
        )
    }

}