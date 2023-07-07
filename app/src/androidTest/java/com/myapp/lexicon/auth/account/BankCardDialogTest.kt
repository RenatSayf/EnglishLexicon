package com.myapp.lexicon.auth.account

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class BankCardDialogTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>

    @Before
    fun setUp() {
        scenario = rule.scenario
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun onCreateDialog() {

        var isRunning = true

        scenario.onActivity { activity ->
            BankCardDialog.newInstance(
                onLaunch = {dialog, binding ->
                    with(binding) {
                        tvBankHint.text = "Номер банковской карты привязанной к номеру телефона +7 900 800 70 50"
                        btnCancel.setOnClickListener {
                            dialog.dismiss()
                            Assert.assertTrue(true)
                            isRunning = false
                        }
                        btnOk.setOnClickListener {
                            val text = etBankCard.text.toString()
                            Assert.assertTrue(true)
                            isRunning = false
                        }
                    }
                }
            ).show(activity.supportFragmentManager, BankCardDialog.TAG)
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}