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
class DialogWarningTest {

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

        scenario.onActivity { act ->

            DialogWarning().apply {
                setListener(object : DialogWarning.Listener {
                    override fun onPositiveClick() {
                        isRunning = false
                    }

                    override fun onNegativeClick() {
                        isRunning = false
                    }
                })
            }.show(
                act.supportFragmentManager.beginTransaction(),
                DialogWarning.DIALOG_TAG
            )
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}