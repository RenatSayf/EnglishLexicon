package com.myapp.lexicon.viewmodels

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.helpers.toStringDate
import com.myapp.lexicon.models.AppResult
import com.myapp.lexicon.models.currency.RubUsd
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale


@RunWith(AndroidJUnit4ClassRunner::class)
class CurrencyViewModelTest {

    @get:Rule
    var rule = ActivityScenarioRule(TestActivity::class.java)

    private lateinit var scenario: ActivityScenario<TestActivity>
    private lateinit var currencyVM: CurrencyViewModel

    @Before
    fun setUp() {
        scenario = rule.scenario
        scenario.onActivity {
            currencyVM = ViewModelProvider(it)[CurrencyViewModel::class.java]
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun getExchangeRate() {
        var isRunning = true

        scenario.onActivity {

            val locale = Locale("ru", "RU")
            currencyVM.getExchangeRateFromApi(
                locale = locale,
                onSuccess = {
                    Assert.assertTrue(true)
                    isRunning = false
                },
                onFailure = {
                    Assert.assertTrue(false)
                    isRunning = false
                }
            )
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun parsingCurrencyApiResult() {
        var isRunning = true

        scenario.onActivity {

            val jsonStr = """{"data": {"RUB": 87.9501}}"""
            val actualResult = currencyVM.parsingCurrencyApiResult(jsonStr, "RUB")
            val expectedResult = RubUsd(System.currentTimeMillis().toStringDate(), "RUB", 87.9501)
            Assert.assertEquals(expectedResult, actualResult)
            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }

    @Test
    fun fetchExchangeRateFromCloud() {
        var isRunning = true

        scenario.onActivity { activity ->

            val locale = Locale("ru", "RU")
            currencyVM.fetchExchangeRateFromCloud(locale)

            currencyVM.currency.observe(activity) { result ->

                result
                if (result !is AppResult.Init) {
                    isRunning = false
                }
            }
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}