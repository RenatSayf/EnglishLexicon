@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.myapp.lexicon.R
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.helpers.toLongDate
import com.myapp.lexicon.helpers.toStringDate
import com.myapp.lexicon.models.currency.Currencies
import com.myapp.lexicon.models.currency.Currency
import com.myapp.lexicon.settings.askForPermission
import com.myapp.lexicon.settings.saveExchangeRateToPref
import com.myapp.lexicon.viewmodels.CurrencyViewModel

class MainFragment : Fragment() {

    companion object {

        private var instance: MainFragment? = null
        private var listener: Listener? = null

        fun getInstance(listener: Listener): MainFragment {

            this.listener = listener
            return if (instance == null) {
                instance = MainFragment()
                instance!!
            }
            else {
                instance!!
            }
        }
    }

    private val currencyVM: CurrencyViewModel by viewModels()
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    interface Listener {
        fun refreshMainScreen(isAdShow: Boolean)
        fun onVisibleMainScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(getString(R.string.KEY_NEED_REFRESH), listener = { requestKey, bundle ->
            if (requestKey == getString(R.string.KEY_NEED_REFRESH)) {
                listener?.refreshMainScreen(false)
            }
        })

        val launcher = this.registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askForPermission(
                Manifest.permission.POST_NOTIFICATIONS,
                onInit = {
                    ConfirmDialog.newInstance(
                        onLaunch = {dialog, binding ->
                            with(binding) {
                                val message = getString(R.string.text_notification_permission)
                                tvMessage.text = message
                                btnOk.setOnClickListener {
                                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    dialog.dismiss()
                                }
                                btnCancel.setOnClickListener {
                                    dialog.dismiss()
                                }
                            }
                        }
                    ).show(parentFragmentManager, ConfirmDialog.TAG)
                }
            )
        }

        currencyVM.fetchExchangeRateFromCloud()

        currencyVM.currency.observe(this) { result ->
            result.onSuccess<Currency> { currency ->
                val cloudTime = currency.date.toLongDate()
                val currentTime = System.currentTimeMillis().toStringDate().toLongDate()
                if (currentTime > cloudTime) {
                    currencyVM.getExchangeRateFromApi(
                        onSuccess = { rate ->
                            val date = System.currentTimeMillis().toStringDate()
                            val id = auth.currentUser?.uid
                            if (rate != 1.0) {
                                currencyVM.saveExchangeRateToCloud(
                                    currency = Currency(date, currency.name, rate),
                                    userId = id?: ""
                                )
                            } else {
                                currencyVM.saveExchangeRateToCloud(
                                    currency = Currency(date, Currencies.USD.name, 1.0),
                                    userId = id?: ""
                                )
                            }
                        },
                        onFailure = {}
                    )
                }
            }
            result.onError {}
        }

        currencyVM.state.observe(this) { state ->
            when(state) {
                is CurrencyViewModel.State.Updated -> {
                    requireContext().saveExchangeRateToPref(state.currency)
                }
                else -> {}
            }
        }


    }

    override fun onResume() {
        super.onResume()

        listener?.onVisibleMainScreen()
    }

}