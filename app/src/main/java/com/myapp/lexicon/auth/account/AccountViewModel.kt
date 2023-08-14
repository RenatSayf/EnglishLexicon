package com.myapp.lexicon.auth.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.models.User
import com.myapp.lexicon.models.payment.common.Amount
import com.myapp.lexicon.models.payment.common.Metadata
import com.myapp.lexicon.models.payment.request.PayClaims
import com.myapp.lexicon.models.payment.request.PayoutDestinationData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class AccountViewModel : ViewModel() {

    companion object {
        const val WRONG_AMOUNT = "the amount is less than zero"
        const val WRONG_CURRENCY = "wrong_currency"
        const val WRONG_WALLET_NUMBER = "wallet_number"
    }
    sealed class State {
        object ReadOnly: State()
        object Editing: State()
        data class OnSave(val user: User): State()
        data class OnValid(
            var phone: Boolean = true,
            var card: Boolean = true,
            var firstName: Boolean = true,
            var lastName: Boolean = true
        ): State()
    }

    private var _state = MutableLiveData<State>(State.ReadOnly)
    val state: LiveData<State> = _state

    fun setState(state: State) {
        _state.value = state
    }

    val url = "https://play.google.com/store/apps/details?id=com.myapp.lexicon"

    fun createPayClaimsBodyJson(
        user: User,
        onSuccess: (String) -> Unit,
        onWrongInputData: (Exception) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (user.reservedPayment <= 0) {
            onWrongInputData.invoke(Exception(WRONG_AMOUNT))
            return
        }
        if (user.currency.isNullOrEmpty()) {
            onWrongInputData.invoke(Exception(WRONG_CURRENCY))
            return
        }
        if (user.bankCard.isEmpty()) {
            onWrongInputData.invoke(Exception(WRONG_WALLET_NUMBER))
            return
        }
        val payClaims = PayClaims(
            Amount(
                user.currency!!,
                user.reservedPayment.toString()
            ),
            "Выплата по заказу ${user.firstName} ${user.lastName}",
            Metadata(user.id),
            PayoutDestinationData(user.bankCard, "yoo_money")
        )
        try {
            val json = Json.encodeToString(payClaims)
            onSuccess.invoke(json)
        }
        catch (e: IllegalArgumentException) {
            onWrongInputData.invoke(e)
        }
        catch (e: Exception) {
            onFailure.invoke(e)
        }
    }


}