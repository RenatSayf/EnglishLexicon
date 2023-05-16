package com.myapp.lexicon.service

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.myapp.lexicon.testing.TestActivity
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class PhoneUnlockedReceiverTest {

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
    fun onReceive() {

        var isRunning = true

        scenario.onActivity { activity ->

            val receiver = PhoneUnlockedReceiver.getInstance()

//            repeat(10) {
//                receiver.onReceive(activity, Intent(Intent.ACTION_SCREEN_OFF))
//                if (it == 9) {
//                    Assert.assertTrue(true)
//                    isRunning = false
//                }
//            }

        }

        while (isRunning) {
            Thread.sleep(100)
        }
    }
}