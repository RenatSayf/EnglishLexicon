package com.myapp.lexicon.helpers

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class ExtensionsKtTest {

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
    fun checkIsActivityShown() {
        var isRunning = true

        scenario.onActivity { act ->
            act.checkIsActivityShown(
                TestActivity::class.java,
                onInvisible = {
                    Assert.assertTrue(false)
                    isRunning = false
                },
                onVisible = {
                    Assert.assertTrue(true)
                    isRunning = false
                }
            )
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}