@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.addword

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.dialogs.NewDictDialog
import com.myapp.lexicon.testing.TestActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class NewDictDialogTest {

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
            NewDictDialog.newInstance(object : NewDictDialog.Listener {
                override fun onPositiveClick(dictName: String) {
                    isRunning = false
                }

                override fun onNegativeClick() {
                    isRunning = false
                }
            }).show(activity.supportFragmentManager, NewDictDialog.TAG)
        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}