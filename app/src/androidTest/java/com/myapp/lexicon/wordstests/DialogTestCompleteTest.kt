@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.wordstests

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class DialogTestCompleteTest {

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

            DialogTestComplete.getInstance(
                4.0,
                10.0,
                object : DialogTestComplete.Listener {
                    override fun onTestCompleteClick() {
                        isRunning = false
                    }

                    override fun onTestRepeatClick() {
                        isRunning = false
                    }

                    override fun onNextTestClick() {
                        isRunning = false
                    }
                }
            ).show(activity.supportFragmentManager, DialogTestComplete.TAG)
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}