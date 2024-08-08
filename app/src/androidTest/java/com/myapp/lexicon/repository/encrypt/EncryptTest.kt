package com.myapp.lexicon.repository.encrypt


import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class EncryptTest {

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
    fun encodedData() {

        var isRunning = true

        val rsaKey = BuildConfig.PUBLIC_RSA_KEY
        val inputData = """{"data":"Test Data"}"""

        val result = Encrypt().encodedData(data = inputData, pubKey = rsaKey)
        result.onSuccess { value: String ->
            Assert.assertTrue(value, true)
            isRunning = false
        }
        result.onFailure { ex: Throwable ->
            Assert.assertTrue(ex.message, false)
            isRunning = false
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}